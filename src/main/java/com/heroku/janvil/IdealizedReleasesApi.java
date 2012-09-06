package com.heroku.janvil;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

import javax.ws.rs.core.MediaType;
import java.util.concurrent.Future;

/**
 * @author Ryan Brainard
 */
class IdealizedReleasesApi extends AbstractApi {

    IdealizedReleasesApi(Client client, Config config) {
        super(client, config, "releases-production.herokuapp.com");
        base.addFilter(new HTTPBasicAuthFilter("", config.getApiKey()));
    }

    public Future<ClientResponse> release(String appName, String slugUrl, String description) {
        return base
                .path("/apps/" + appName + "/release")
                .queryParam("cloud", herokuHost())
                .queryParam("description", description)
                .queryParam("slug_url", slugUrl)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(ClientResponse.class);
    }
}