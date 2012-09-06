package com.heroku.janvil;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

import javax.ws.rs.core.MediaType;
import java.util.concurrent.Future;

/**
 * @author Ryan Brainard
 */
class CoreReleasesApi extends AbstractApi {

    CoreReleasesApi(Client client, Config config) {
        super(client, config, "api." + herokuHost());
        base.addFilter(new HTTPBasicAuthFilter("", config.getApiKey()));
    }

    public Future<ClientResponse> getRelease(String appName, String releaseName) {
        return base
                .path("/apps/" + appName + "/releases/" + releaseName)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(ClientResponse.class);
    }

    public Future<ClientResponse> getReleasesSlug(String appName) {
        return base
                .path("/apps/" + appName + "/release_slug")
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(ClientResponse.class);
    }
}