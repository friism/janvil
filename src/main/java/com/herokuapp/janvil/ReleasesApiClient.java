package com.herokuapp.janvil;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.multipart.FormDataMultiPart;

import javax.ws.rs.core.MediaType;

/**
 * @author Ryan Brainard
 */
class ReleasesApiClient extends AbstractApiClient {

    ReleasesApiClient(Janvil.Config config) {
        super(config, "releases-test.herokuapp.com");
        baseResource.addFilter(new HTTPBasicAuthFilter("", config.apiKey));
    }

    public ClientResponse release(String appName, String buildUrl, String description) {
        return baseResource
                .path("/apps/" + appName + "/release")
                .type(MediaType.MULTIPART_FORM_DATA_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(ClientResponse.class, new FormDataMultiPart()
                        .field("description", description)
                        .field("build_url", buildUrl));
    }

}