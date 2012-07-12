package com.herokuapp.janvil;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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

    public String deploy(File dir, String appName, HashMap<String, String> env) throws IOException {
        final Manifest manifest = new Manifest(dir);
        manifest.addAll();
        return deploy(manifest, appName, env);
    }

    public String deploy(Manifest manifest, String appName, HashMap<String, String> env) throws IOException {
        final Collection filesToUpload = anvil.diff(manifest).getEntity(Collection.class);
        for (Object hash : filesToUpload) {
            anvil.post(manifest.fromHash(hash.toString())); // TODO: multithread
        }

        final ClientResponse buildResponse = anvil.build(manifest, env);
        final String slugUrl = buildResponse.getHeaders().get("X-Slug-Url").get(0);
        System.out.println(buildResponse.getEntity(String.class)); //TODO: listen and stream

        final ClientResponse releaseResponse = releases.release(appName, slugUrl, "Janvil");
        return releaseResponse.getEntity(Map.class).get("release").toString();
    }

}
