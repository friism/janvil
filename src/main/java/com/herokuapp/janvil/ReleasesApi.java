package com.herokuapp.janvil;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.util.concurrent.Future;

/**
 * @author Ryan Brainard
 */
class ReleasesApi extends AbstractApi {

    ReleasesApi(Client client, Config config) {
        super(client, config, "release-promotion.herokuapp.com");
        base.addFilter(new HTTPBasicAuthFilter("", config.getApiKey()));
    }

    public Future<ClientResponse> release(String appName, String buildUrl, String description) {
        return base
                .path("/apps/" + appName + "/release")
                .queryParam("cloud", "heroku.com")
                .queryParam("description", description)
                .queryParam("build_url", buildUrl)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(ClientResponse.class);
    }

    public Future<ClientResponse> copy(String sourceAppName, String targetAppName) {
        return copy(sourceAppName, targetAppName, null);
    }

    public Future<ClientResponse> copy(String sourceAppName, String targetAppName, String description) {
        final MultivaluedMap<String,String> queryParams = new MultivaluedMapImpl();
        queryParams.add("cloud", "heroku.com");
        if (description != null) {
            queryParams.add("description", description);
        }

        return base
                .path("/apps/" + sourceAppName + "/copy/" + targetAppName)
                .queryParams(queryParams)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(ClientResponse.class);
    }
}