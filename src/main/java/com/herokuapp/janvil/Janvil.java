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

    public static final class Config {

        final String apiKey;
        Protocol protocol = Protocol.HTTPS;
        String consumersUserAgent = null;

        public Config(String apiKey) {
            this.apiKey = apiKey;
        }

        public Config setProtocol(Protocol protocol) {
            this.protocol = protocol;
            return this;
        }

        public Config setConsumersUserAgent(String consumersUserAgent) {
            this.consumersUserAgent = consumersUserAgent;
            return this;
        }
    }

    public static enum Protocol {
        HTTP("http"),
        HTTPS("https");

        final String scheme;

        Protocol(String scheme) {
            this.scheme = scheme;
        }
    }

    final AnvilApiClient anvil;
    final ReleasesApiClient releases;

    public Janvil(String apiKey) {
        this(new Config(apiKey));
    }

    public Janvil(Config config) {
        anvil = new AnvilApiClient(config);
        releases = new ReleasesApiClient(config);
    }

    public void deploy(DeployRequest request) throws IOException {
        request.eventSubscription.announce(EventSubscription.Event.DEPLOY_START);

        request.eventSubscription.announce(EventSubscription.Event.DIFF_START);
        final Collection filesToUpload = anvil.diff(request.manifest).getEntity(Collection.class);
        request.eventSubscription.announce(EventSubscription.Event.DIFF_END);

        request.eventSubscription.announce(EventSubscription.Event.UPLOADS_START);
        final Map<File, Future<ClientResponse>> uploads = new HashMap<File, Future<ClientResponse>>(filesToUpload.size());
        for (Object hash : filesToUpload) {
            final File file = request.manifest.fromHash(hash.toString());
            request.eventSubscription.announce(EventSubscription.Event.UPLOAD_FILE_START, file);
            uploads.put(file, anvil.postAsync(file));
        }

        for (Map.Entry<File, Future<ClientResponse>> upload : uploads.entrySet()) {
            try {
                upload.getValue().get();
                request.eventSubscription.announce(EventSubscription.Event.UPLOAD_FILE_END, upload.getKey());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        request.eventSubscription.announce(EventSubscription.Event.UPLOADS_END);

        request.eventSubscription.announce(EventSubscription.Event.BUILD_START);
        final ClientResponse buildResponse = anvil.build(request.manifest, request.env);
        final String slugUrl = buildResponse.getHeaders().get("X-Slug-Url").get(0);

        BufferedReader buildOutput = null;
        try {
            buildOutput = new BufferedReader(new InputStreamReader(buildResponse.getEntity(InputStream.class)));
            String line;
            while ((line = buildOutput.readLine()) != null) {
                request.eventSubscription.announce(EventSubscription.Event.BUILD_OUTPUT_LINE, line);
            }
        } finally {
            if (buildOutput != null) {
                buildOutput.close();
            }
        }
        request.eventSubscription.announce(EventSubscription.Event.BUILD_END, slugUrl);

        request.eventSubscription.announce(EventSubscription.Event.RELEASE_START, slugUrl);
        final ClientResponse releaseResponse = releases.release(request.appName, slugUrl, "Janvil");
        if (releaseResponse.getStatus() != HttpURLConnection.HTTP_OK) {
            throw new UniformInterfaceException(releaseResponse);
        }
        request.eventSubscription.announce(EventSubscription.Event.RELEASE_END, releaseResponse.getEntity(Map.class).get("release"));

        request.eventSubscription.announce(EventSubscription.Event.DEPLOY_END);
    }

    static final class DeployRequest {
        private Manifest manifest;
        private String appName;
        private HashMap<String, String> env;
        private EventSubscription eventSubscription;

        DeployRequest(Manifest manifest, String appName) {
            this.manifest = manifest;
            this.appName = appName;
            this.env = new HashMap<String, String>();
            this.eventSubscription = new EventSubscription();
        }

        public DeployRequest setEnv(HashMap<String, String> env) {
            this.env = env;
            return this;
        }

        public DeployRequest setEventSubscription(EventSubscription eventSubscription) {
            this.eventSubscription = eventSubscription;
            return this;
        }
    }
}
