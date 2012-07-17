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

    private final AnvilAsyncClient anvil;
    private final ReleasesAsyncClient releases;

    public Janvil(String apiKey) {
        this(new Config(apiKey));
    }

    public Janvil(Config config) {
        anvil = new AnvilAsyncClient(client, config);
        releases = new ReleasesAsyncClient(client, config);
    }

    public void deploy(DeployRequest request) throws IOException, ExecutionException, InterruptedException {
        request.eventSubscription().announce(EventSubscription.Event.DEPLOY_START);

        request.eventSubscription().announce(EventSubscription.Event.DIFF_START, request.manifest().getEntries().size());
        final Collection filesToUpload = anvil.diff(request.manifest()).get().getEntity(Collection.class);
        request.eventSubscription().announce(EventSubscription.Event.DIFF_END, filesToUpload.size());

        request.eventSubscription().announce(EventSubscription.Event.UPLOADS_START, filesToUpload.size());
        final Map<File, Future<ClientResponse>> uploads = new HashMap<File, Future<ClientResponse>>(filesToUpload.size());
        for (Object hash : filesToUpload) {
            final File file = request.manifest().fromHash(hash.toString());
            request.eventSubscription().announce(EventSubscription.Event.UPLOAD_FILE_START, file);
            uploads.put(file, anvil.post(file));
        }

        for (Map.Entry<File, Future<ClientResponse>> upload : uploads.entrySet()) {
            try {
                upload.getValue().get();
                request.eventSubscription().announce(EventSubscription.Event.UPLOAD_FILE_END, upload.getKey());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        request.eventSubscription().announce(EventSubscription.Event.UPLOADS_END);

        request.eventSubscription().announce(EventSubscription.Event.BUILD_START);
        final ClientResponse buildResponse = anvil.build(request.manifest(), request.env(), request.buildpack()).get();
        final String slugUrl = buildResponse.getHeaders().get("X-Slug-Url").get(0);

        BufferedReader buildOutput = null;
        try {
            buildOutput = new BufferedReader(new InputStreamReader(buildResponse.getEntity(InputStream.class)));
            String line;
            while ((line = buildOutput.readLine()) != null) {
                request.eventSubscription().announce(EventSubscription.Event.BUILD_OUTPUT_LINE, line);
            }
        } finally {
            if (buildOutput != null) {
                buildOutput.close();
            }
        }
        request.eventSubscription().announce(EventSubscription.Event.BUILD_END, slugUrl);

        request.eventSubscription().announce(EventSubscription.Event.RELEASE_START, slugUrl);
        final ClientResponse releaseResponse = releases.release(request.appName(), slugUrl, "Janvil").get();
        if (releaseResponse.getStatus() != HttpURLConnection.HTTP_OK) {
            throw new UniformInterfaceException(releaseResponse);
        }
        request.eventSubscription().announce(EventSubscription.Event.RELEASE_END, releaseResponse.getEntity(Map.class).get("release"));

        request.eventSubscription().announce(EventSubscription.Event.DEPLOY_END);
    }

}
