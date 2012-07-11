package com.herokuapp.janvil;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
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
public class ReleasesApi {

    public static final String DEFAULT_SCHEME = "https";
    public static final String DEFAULT_HOST = "releases-test.herokuapp.com";
    public static final int DEFAULT_PORT = 443;

    public static final class Builder {

        private String scheme = DEFAULT_SCHEME;
        private String host = DEFAULT_HOST;
        private int port = DEFAULT_PORT;

        private final String apiKey;
        private String consumersUserAgent;

        public Builder(String apiKey) {
            this.apiKey = apiKey;
        }

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

        public ReleasesApi build() {
            return new ReleasesApi(Janvil.universalClient, this); //TODO: clean up
        }
    }

    private final WebResource baseResource;

    private ReleasesApi(Client universalClient, Builder builder) {
        baseResource = universalClient.resource(builder.scheme + "://" + builder.host + ":" + builder.port);
        baseResource.addFilter(new HTTPBasicAuthFilter("", builder.apiKey));
        baseResource.addFilter(new UserAgentFilter(builder.consumersUserAgent));
    }

    public ClientResponse release(String appName, String buildUrl, String description) {
        return baseResource
                .path("/apps/" + appName + "/release")
                .type(MediaType.MULTIPART_FORM_DATA_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(ClientResponse.class, new FormDataMultiPart()
                        .field("build_url", buildUrl)
                        .field("description", description)
                );
    }

}