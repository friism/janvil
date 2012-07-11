package com.herokuapp.janvil;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.codehaus.jackson.map.ObjectMapper;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.util.Map;

/**
 * @author Ryan Brainard
 */
public class Janvil {

    public static final String DEFAULT_SCHEME = "https";
    public static final String DEFAULT_HOST = "anvil.herokuapp.com";
    public static final int DEFAULT_PORT = 443;

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    private static final Client universalClient;

    static {
        final ClientConfig config = new DefaultClientConfig();
        config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        config.getProperties().put(ClientConfig.PROPERTY_CHUNKED_ENCODING_SIZE, -1 /* default chunk size */);
        // TODO: user agent
        universalClient = com.sun.jersey.api.client.Client.create(config);
    }

    public static final class Builder {

        private String scheme = DEFAULT_SCHEME;
        private String host = DEFAULT_HOST;
        private int port = DEFAULT_PORT;

        public Builder setScheme(String scheme) {
            this.scheme = scheme;
            return this;
        }

        public Builder setHost(String host) {
            this.host = host;
            return this;
        }

        public Builder setPort(int port) {
            this.port = port;
            return this;
        }

        public Janvil build() {
            return new Janvil(this);
        }
    }

    private final WebResource baseResource;

    private Janvil(Builder builder) {
        baseResource = universalClient.resource(builder.scheme + "://" + builder.host + ":" + builder.port);
    }

    public String post(Manifest manifest) throws IOException {
        MultivaluedMap<String,String> body = new MultivaluedMapImpl();
        body.add("manifest", JSON_MAPPER.writeValueAsString(manifest.entries));

        final Map res = baseResource
                .path("/manifest")
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Map.class, body);

        return res.get("id").toString();
    }
}