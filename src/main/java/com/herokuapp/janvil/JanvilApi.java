package com.herokuapp.janvil;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import static com.herokuapp.janvil.CurlFormDataContentDisposition.curlize;

/**
 * @author Ryan Brainard
 */
public class JanvilApi {

    public static final String DEFAULT_SCHEME = "https";
    public static final String DEFAULT_HOST = "anvil.herokuapp.com";
    public static final int DEFAULT_PORT = 443;

    private static final Client universalClient;

    static {
        final ClientConfig config = new DefaultClientConfig();
        config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        config.getProperties().put(ClientConfig.PROPERTY_CHUNKED_ENCODING_SIZE, -1 /* default chunk size */);

        universalClient = com.sun.jersey.api.client.Client.create(config);
    }

    public static final class Builder {

        private String scheme = DEFAULT_SCHEME;
        private String host = DEFAULT_HOST;
        private int port = DEFAULT_PORT;

        private String consumersUserAgent;

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

        public Builder setConsumersUserAgent(String consumersUserAgent) {
            this.consumersUserAgent = consumersUserAgent;
            return this;
        }

        public JanvilApi build() {
            return new JanvilApi(this);
        }
    }

    private final WebResource baseResource;

    private JanvilApi(Builder builder) {
        baseResource = universalClient.resource(builder.scheme + "://" + builder.host + ":" + builder.port);
        baseResource.addFilter(new UserAgentFilter(builder.consumersUserAgent));
    }

    public ClientResponse post(Manifest manifest) throws IOException {
        return baseResource
                .path("/manifest")
                .type(MediaType.MULTIPART_FORM_DATA_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(ClientResponse.class, new FormDataMultiPart()
                        .field("manifest", manifest.getEntries(), MediaType.APPLICATION_JSON_TYPE));
    }

    public ClientResponse build(Manifest manifest, Map<String, String> env) throws IOException {
        return build(manifest, env, "");
    }

    public ClientResponse build(Manifest manifest, Map<String,String> env, String buildpack) throws IOException {
        return baseResource
                .path("/manifest/build")
                .type(MediaType.MULTIPART_FORM_DATA_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(ClientResponse.class, new FormDataMultiPart()
                        .field("manifest", manifest.getEntries(), MediaType.APPLICATION_JSON_TYPE)
                        .field("env", env, MediaType.APPLICATION_JSON_TYPE)
                        .field("buildpack", buildpack)
                );
    }

    public ClientResponse diff(Manifest manifest) throws IOException {
        return baseResource
                .path("/manifest/diff")
                .type(MediaType.MULTIPART_FORM_DATA_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(ClientResponse.class, new FormDataMultiPart()
                        .field("manifest", manifest.getEntries(), MediaType.APPLICATION_JSON_TYPE));
    }

    public ClientResponse post(File file) throws IOException {
        return baseResource
            .path("/file/" + Manifest.hash(file))
            .type(MediaType.MULTIPART_FORM_DATA_TYPE)
            .post(ClientResponse.class, new FormDataMultiPart()
                    .bodyPart(curlize(new FileDataBodyPart("data", file))));
    }

    public ClientResponse get(String hash) throws IOException {
        return baseResource
                .path("/file/" + hash)
                .accept(MediaType.APPLICATION_OCTET_STREAM_TYPE)
                .get(ClientResponse.class);
    }

    public ClientResponse get(File file) throws IOException {
        return get(Manifest.hash(file));
    }

}