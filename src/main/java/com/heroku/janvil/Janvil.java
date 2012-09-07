package com.heroku.janvil;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
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
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.heroku.janvil.Janvil.Event.*;

/**
 * @author Ryan Brainard
 */
public class Janvil {

    protected static final Client client;

    static {
        final ClientConfig config = new DefaultClientConfig();
        config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        config.getProperties().put(ClientConfig.PROPERTY_CHUNKED_ENCODING_SIZE, -1 /* default chunk size */);

        client = com.sun.jersey.api.client.Client.create(config);
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
        RELEASE_START,
        RELEASE_END,
        HTTP_LOGGING_BYTE
    }

    protected final AnvilApi anvil;
    protected final CoreReleasesApi coreReleases;
    protected final IdealizedReleasesApi idealReleases;
    protected boolean writeMetadata;
    protected boolean readMetadata;
    protected final EventSubscription<Event> events;

    public Janvil(String apiKey) {
        this(new Config(apiKey));
    }

    public Janvil(Config config) {
        anvil = new AnvilApi(client, config);
        coreReleases = new CoreReleasesApi(client, config);
        idealReleases = new IdealizedReleasesApi(client, config);
        writeMetadata = config.getWriteMetadata();
        readMetadata = config.getReadMetadata();
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

    protected String _build(Manifest manifest, Map<String, String> env, String buildpack) throws InterruptedException, ExecutionException, IOException {
        events.announce(DIFF_START, manifest.getEntries().size());
        final Collection filesToUpload = anvil.diff(manifest).get().getEntity(Collection.class);
        events.announce(DIFF_END, filesToUpload.size());

        events.announce(UPLOADS_START, filesToUpload.size());
        final Map<File, Future<ClientResponse>> uploads = new HashMap<File, Future<ClientResponse>>(filesToUpload.size());
        for (Object hash : filesToUpload) {
            final File file = manifest.fromHash(hash.toString());
            events.announce(UPLOAD_FILE_START, file);
            uploads.put(file, anvil.post(file));
        }

        for (Map.Entry<File, Future<ClientResponse>> upload : uploads.entrySet()) {
            upload.getValue().get();
            events.announce(UPLOAD_FILE_END, upload.getKey());
        }
        events.announce(UPLOADS_END, uploads.size());

        events.announce(BUILD_START);
        final String existingCacheUrl = readMetadata ? manifest.readCacheUrl() : "";
        final ClientResponse buildResponse = anvil.build(manifest, env, buildpack, existingCacheUrl).get();

        final String manifestId = buildResponse.getHeaders().get("X-Manifest-Id").get(0);
        final String slugUrl = buildResponse.getHeaders().get("X-Slug-Url").get(0);
        final String cacheUrl = buildResponse.getHeaders().get("X-Cache-Url").get(0);

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

        final int exitStatus = Integer.parseInt(anvil.exitStatus(manifestId).get().getEntity(String.class).trim());
        if (exitStatus != 0) {
            throw new JanvilBuildException(exitStatus);
        }

        if (writeMetadata) {
            manifest.writeSlugUrl(slugUrl);
            manifest.writeCacheUrl(cacheUrl);
        }

        events.announce(BUILD_END, slugUrl);

        return slugUrl;
    }

    public void release(String appName, String slugUrl, String description) {
        try {
            _release(appName, slugUrl, description);
        } catch (InterruptedException e) {
            throw new JanvilRuntimeException(e);
        } catch (ExecutionException e) {
            throw new JanvilRuntimeException(e);
        }
    }

    protected void _release(String appName, String slugUrl, String description) throws InterruptedException, ExecutionException {
        events.announce(RELEASE_START, slugUrl);
        final ClientResponse releaseResponse = idealReleases.release(appName, slugUrl, description).get();
        if (releaseResponse.getStatus() != HttpURLConnection.HTTP_OK) {
            throw new UniformInterfaceException(releaseResponse);
        }
        events.announce(RELEASE_END, releaseResponse.getEntity(Map.class).get("release"));
    }

    public void copy(String sourceAppName, String targetAppName, ReleaseDescriptionBuilder releaseDescriptionBuilder) {
        try {
            _copy(sourceAppName, targetAppName, releaseDescriptionBuilder);
        } catch (ExecutionException e) {
            throw new JanvilRuntimeException(e);
        } catch (InterruptedException e) {
            throw new JanvilRuntimeException(e);
        }
    }

    protected void _copy(String sourceAppName, String targetAppName, ReleaseDescriptionBuilder releaseDescriptionBuilder) throws ExecutionException, InterruptedException {
        final Map source = coreReleases.getReleasesSlug(sourceAppName).get().getEntity(Map.class);
        final String sourceSlugUrl = source.get("slug_url").toString();
        final String sourceReleaseName = source.get("name").toString();
        final String sourceCommit = coreReleases.getRelease(sourceAppName, sourceReleaseName).get().getEntity(Map.class).get("commit").toString();
        final String description = releaseDescriptionBuilder.buildDescription(sourceAppName, sourceReleaseName, sourceCommit, targetAppName);

        _release(targetAppName, sourceSlugUrl, description);
    }

    public static interface ReleaseDescriptionBuilder {
        String buildDescription(String sourceAppName, String sourceReleaseName, String sourceCommit, String targetAppName);
    }

}
