package com.herokuapp.janvil;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

import javax.ws.rs.core.MediaType;
import java.util.concurrent.Future;

/**
 * @author Ryan Brainard
 */
class ReleasesApi extends AbstractApi {

    ReleasesApi(Client client, Config config) {
        super(client, config, "releases-test.herokuapp.com");
        base.addFilter(new HTTPBasicAuthFilter("", config.getApiKey()));
    }

    public Future<ClientResponse> release(String appName, String buildUrl, String description) {
        return base
                .path("/apps/" + appName + "/release")
                .queryParam("description", description)
                .queryParam("build_url", buildUrl)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(ClientResponse.class);
    }

}