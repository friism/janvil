package com.herokuapp.janvil;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.herokuapp.janvil.Janvil.Event.*;

/**
 * @author Ryan Brainard
 */
public class Janvil {

    static final Client client;

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
        RELEASE_END
    }

    private final AnvilApi anvil;
    private final ReleasesApi releases;
    private boolean writeMetadata;
    private boolean readMetadata;
    private final EventSubscription<Event> events;

    public Janvil(String apiKey) {
        this(new Config(apiKey));
    }

    public Janvil(Config config) {
        anvil = new AnvilApi(client, config);
        releases = new ReleasesApi(client, config);
        writeMetadata = config.getWriteMetadata();
        readMetadata = config.getReadMetadata();
        events = config.getEventSubscription();
    }

    public String build(Manifest manifest) throws IOException, ExecutionException, InterruptedException {
        return build(manifest, new HashMap<String, String>(), "");
    }

    public String build(Manifest manifest, HashMap<String, String> env, String buildpack) throws InterruptedException, ExecutionException, IOException {
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
        events.announce(UPLOADS_END);

        events.announce(BUILD_START);
        final String existingCacheUrl = readMetadata ? manifest.readCacheUrl() : "";
        final ClientResponse buildResponse = anvil.build(manifest, env, buildpack, existingCacheUrl).get();

        BufferedReader buildOutput = null;
        try {
            buildOutput = new BufferedReader(new InputStreamReader(buildResponse.getEntity(InputStream.class)));
            String line;
            while ((line = buildOutput.readLine()) != null) {
                events.announce(BUILD_OUTPUT_LINE, line);
            }
        } finally {
            if (buildOutput != null) {
                buildOutput.close();
            }
        }

        final String slugUrl = buildResponse.getHeaders().get("X-Slug-Url").get(0);
        final String cacheUrl = buildResponse.getHeaders().get("X-Cache-Url").get(0);

        if (writeMetadata) {
            manifest.writeSlugUrl(slugUrl);
            manifest.writeCacheUrl(cacheUrl);
        }

        events.announce(BUILD_END, slugUrl);

        return slugUrl;
    }

    public void realese(String appName, Manifest manifest) throws ExecutionException, InterruptedException {
        release(appName, manifest.readSlugUrl());
    }

    public void release(String appName, String slugUrl) throws InterruptedException, ExecutionException {
        events.announce(RELEASE_START, slugUrl);
        final ClientResponse releaseResponse = releases.release(appName, slugUrl, "Janvil").get();
        if (releaseResponse.getStatus() != HttpURLConnection.HTTP_OK) {
            throw new UniformInterfaceException(releaseResponse);
        }
        events.announce(RELEASE_END, releaseResponse.getEntity(Map.class).get("release"));
    }

}
