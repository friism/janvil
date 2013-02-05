package com.heroku.janvil;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.heroku.janvil.Janvil.Event.*;
import static javax.ws.rs.core.MediaType.*;

/**
 * @author Ryan Brainard
 */
public class Janvil {

    protected static enum ClientType {
        FIXED_LENGTH,
        CHUNKED
    }

    protected static final ConcurrentHashMap<ClientType, Client> clients = new ConcurrentHashMap<ClientType, Client>(2);

    static Client getClient(ClientType clientType) {
        if (clients.containsKey(clientType)) {
            return clients.get(clientType);
        }

        final ClientConfig config = new DefaultClientConfig();
        config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        if (clientType == ClientType.CHUNKED) {
            config.getProperties().put(ClientConfig.PROPERTY_CHUNKED_ENCODING_SIZE, -1 /* default chunk size */);
        }

        clients.putIfAbsent(clientType, Client.create(config));
        return clients.get(clientType);
    }

    public static enum Event {
        DIFF_START,
        DIFF_END,
        UPLOAD_FILE_START,
        UPLOAD_FILE_END,
        UPLOADS_START,
        UPLOADS_END,
        BUILD_START,
        BUILD_END,
        BUILD_OUTPUT_LINE,
        PROMOTE_START,
        PROMOTE_END,
        RELEASE_START,
        RELEASE_END,
        COPY_START,
        COPY_END,
        POLLING,
        HTTP_LOGGING_BYTE
    }

    protected final AnvilApi anvil;
    protected final CisaurusApi cisaurusApi;
    protected final boolean writeSlugUrl;
    protected final boolean writeCacheUrl;
    protected final boolean readCacheUrl;
    protected final EventSubscription<Event> events;
    protected final boolean parallelUploads;

    public Janvil(String apiKey) {
        this(new Config(apiKey));
    }

    public Janvil(Config config) {
        anvil = new AnvilApi(getClient(ClientType.CHUNKED), config);
        cisaurusApi = new CisaurusApi(getClient(ClientType.FIXED_LENGTH), config);
        writeSlugUrl = config.getWriteSlugUrl();
        writeCacheUrl = config.getWriteCacheUrl();
        readCacheUrl = config.getReadCacheUrl();
        parallelUploads = config.isParallelUploads();
        events = config.getEventSubscription();
    }

    public String build(Manifest manifest) {
        return build(manifest, new HashMap<String, String>(), "");
    }

    public String build(Manifest manifest, Map<String, String> env, String buildpack) {
        try {
            return _build(manifest, env, buildpack);
        } catch (InterruptedException e) {
            throw new JanvilRuntimeException(e);
        } catch (ExecutionException e) {
            throw new JanvilRuntimeException(e);
        } catch (IOException e) {
            throw new JanvilRuntimeException(e);
        }
    }

    private void handleUploadResponse(File file, Future<ClientResponse> uploadFuture, AtomicInteger uploadCounter) throws InterruptedException, ExecutionException {
        uploadFuture.get();
        events.announce(UPLOAD_FILE_END, file);
        uploadCounter.incrementAndGet();
    }

    protected String _build(Manifest manifest, Map<String, String> env, String buildpack) throws InterruptedException, ExecutionException, IOException {
        events.announce(DIFF_START, manifest.getEntries().size());
        final Collection filesToUpload = anvil.diff(manifest).get().getEntity(Collection.class);
        events.announce(DIFF_END, filesToUpload.size());

        events.announce(UPLOADS_START, filesToUpload.size());
        final AtomicInteger uploadCounter = new AtomicInteger();
        final Map<File, Future<ClientResponse>> uploadFutures = new HashMap<File, Future<ClientResponse>>(filesToUpload.size());
        for (Object hash : filesToUpload) {
            final File file = manifest.fromHash(hash.toString());
            events.announce(UPLOAD_FILE_START, file);
            final Future<ClientResponse> upload = anvil.post(file);

            if (parallelUploads) {
                uploadFutures.put(file, upload);
            } else {
                handleUploadResponse(file, upload, uploadCounter);
            }
        }
        if (parallelUploads) {
            for (Map.Entry<File, Future<ClientResponse>> upload : uploadFutures.entrySet()) {
                handleUploadResponse(upload.getKey(), upload.getValue(), uploadCounter);
            }
        }
        events.announce(UPLOADS_END, uploadCounter.get());

        events.announce(BUILD_START);
        final String existingCacheUrl = readCacheUrl ? manifest.readCacheUrl() : "";
        final ClientResponse buildResponse = anvil.build(manifest, env, buildpack, existingCacheUrl).get();

        final String manifestId = buildResponse.getHeaders().get("X-Manifest-Id").get(0);
        final String slugUrl = buildResponse.getHeaders().get("X-Slug-Url").get(0);
        final String cacheUrl = buildResponse.getHeaders().get("X-Cache-Url").get(0);

        final ExecutorService slurpeeMachine = Executors.newSingleThreadExecutor();
        Future<Void> buildSlurpee = slurpeeMachine.submit(slurpBuildOutputCallable(buildResponse));
        slurpeeMachine.shutdown();
        try {
            buildSlurpee.get(15, TimeUnit.MINUTES);
        } catch (TimeoutException e) {
            throw new JanvilRuntimeException("Timed out waiting for build results", e);
        }

        final int exitStatus = Integer.parseInt(anvil.exitStatus(manifestId).get().getEntity(String.class).trim());
        if (exitStatus != 0) {
            throw new JanvilBuildException(exitStatus);
        }

        if (writeCacheUrl) {
            manifest.writeCacheUrl(cacheUrl);
        }

        if (writeSlugUrl) {
            manifest.writeSlugUrl(slugUrl);
        }

        events.announce(BUILD_END, slugUrl);

        return slugUrl;
    }

    private Callable<Void> slurpBuildOutputCallable(final ClientResponse buildResponse) {
        return new Callable<Void>() {
            public Void call() throws Exception {
                BufferedReader buildOutput = null;
                try {
                    buildOutput = new BufferedReader(new InputStreamReader(buildResponse.getEntityInputStream(), "UTF-8"));
                    String line;
                    while ((line = buildOutput.readLine()) != null) {
                        // strip null chars from keepalive=1
                        events.announce(BUILD_OUTPUT_LINE, line.replaceAll("\u0000", ""));
                    }
                } finally {
                    if (buildOutput != null) {
                        buildOutput.close();
                    }
                }
                return null;
            }
        };
    }

    public void release(String appName, String slugUrl, String description) {
        _release(appName, slugUrl, description, null);
    }

    public void release(String appName, String slugUrl, String description, String commitHead) {
        _release(appName, slugUrl, description, commitHead);
    }

    protected void _release(String appName, String slugUrl, String description, String commitHead) {
        events.announce(RELEASE_START);
        final String release = handleAsyncRelease(cisaurusApi.release(appName, slugUrl, description, commitHead));
        events.announce(RELEASE_END, release);
    }

    public void copy(String sourceAppName, String targetAppName, String description) {
        events.announce(COPY_START);
        final String release = handleAsyncRelease(cisaurusApi.copy(sourceAppName, targetAppName, description));
        events.announce(COPY_END, release);
    }

    public void promote(String appName) {
        events.announce(PROMOTE_START);
        final String release = handleAsyncRelease(cisaurusApi.promote(appName));
        events.announce(PROMOTE_END, release);
    }

    public List<String> downstreams(String appName) {
        //noinspection unchecked
        return handleAs(cisaurusApi.downstreams(appName), List.class);
    }

    public void addDownstream(String appName, String downstreamAppName) {
        handleAs(cisaurusApi.addDownstream(appName, downstreamAppName), String.class);
    }

    public void removeDownstream(String appName, String downstreamAppName) {
        handleAs(cisaurusApi.removeDownstream(appName, downstreamAppName), String.class);
    }

    public List<String> diffDownstream(String appName) {
        //noinspection unchecked
        return handleAs(cisaurusApi.diffDownstream(appName), List.class);
    }

    protected <T> T handleAs(Future<ClientResponse> responseFuture, Class<T> as) {
        final ClientResponse response;
        try {
            response = responseFuture.get();
        } catch (InterruptedException e) {
            throw new JanvilRuntimeException(e);
        } catch (ExecutionException e) {
            throw new JanvilRuntimeException(e);
        }

        return handleAs(response, as);
    }

    private <T> T handleAs(ClientResponse response, Class<T> as) {
        if (response.getStatus() == HttpURLConnection.HTTP_OK) {
            return response.getEntity(as);
        }

        final String error;
        if (APPLICATION_JSON_TYPE.isCompatible(response.getType())) {
            final Map errorMap = response.getEntity(Map.class);
            if (errorMap.containsKey("error")) {
                error = errorMap.get("error").toString();
            } else {
                throw new JanvilRuntimeException("UNKNOWN ERROR");
            }
        } else {
            error = "UNKNOWN ERROR (" + response.getStatus()  + "): " + response.getEntity(String.class);
        }
        throw new JanvilRuntimeException(error);
    }

    protected String handleAsyncRelease(Future<ClientResponse> initialResponse) {
        final ClientResponse releaseResponse;
        try {
            releaseResponse = cisaurusApi.poll(initialResponse.get(), new PollingListener());
        } catch (ExecutionException e) {
            throw new JanvilRuntimeException(e);
        } catch (InterruptedException e) {
            throw new JanvilRuntimeException(e);
        }

        final Map body = handleAs(releaseResponse, Map.class);

        if (!body.containsKey("release")) {
            throw new JanvilRuntimeException("release info not found in result");
        }

        return body.get("release").toString();
    }

    private class PollingListener implements Runnable {
        public void run() {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            events.announce(POLLING);
        }
    }
}
