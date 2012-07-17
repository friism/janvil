package com.herokuapp.janvil;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.multipart.FormDataMultiPart;

import javax.ws.rs.core.MediaType;
import java.util.concurrent.Future;

/**
 * @author Ryan Brainard
 */
class ReleasesAsyncClient extends AbstractAsyncClient {

    ReleasesAsyncClient(Janvil.Config config) {
        super(config, "releases-test.herokuapp.com");
        base.addFilter(new HTTPBasicAuthFilter("", config.apiKey));
    }

    public Future<ClientResponse> release(String appName, String buildUrl, String description) {
        return base
                .path("/apps/" + appName + "/release")
                .type(MediaType.MULTIPART_FORM_DATA_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(ClientResponse.class, new FormDataMultiPart()
                        .field("description", description)
                        .field("build_url", buildUrl));
    }

}