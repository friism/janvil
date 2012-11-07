package com.heroku.janvil;

import com.sun.jersey.api.client.AsyncWebResource;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

import javax.ws.rs.core.MediaType;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author Ryan Brainard
 */
public class CisaurusApi extends AbstractApi {

    private final AsyncWebResource v;

    CisaurusApi(Client client, Config config) {
        super(client, config, getEnvOrElse("CISAURUS_HOST", "cisaurus.herokuapp.com"));
        base.addFilter(new HTTPBasicAuthFilter("", config.getApiKey()));
        v = base.path("/v1");
    }

    public Future<ClientResponse> downstreams(String appName) {
        return v.path("/apps/" + appName + "/pipeline/downstreams")
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(ClientResponse.class);
    }

    public Future<ClientResponse> addDownstream(String appName, String downstreamName) {
        return v.path("/apps/" + appName + "/pipeline/downstreams/" + downstreamName)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(ClientResponse.class);
    }

    public Future<ClientResponse> removeDownstream(String appName, String downstreamName) {
        return v.path("/apps/" + appName + "/pipeline/downstreams/" + downstreamName)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .delete(ClientResponse.class);
    }

    public Future<ClientResponse> diffDownstream(String appName) {
        return v.path("/apps/" + appName + "/pipeline/diff")
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(ClientResponse.class);
    }

    public Future<ClientResponse> promote(String appName) {
        return v.path("/apps/" + appName + "/pipeline/promote")
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(ClientResponse.class);
    }

    public Future<ClientResponse> copy(String sourceApp, String targetApp, String description) {
        return v.path("/apps/" + sourceApp + "/copy/" + targetApp)
                .queryParam("description", description)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(ClientResponse.class);
    }

    public Future<ClientResponse> release(String appName, final String slugUrl, final String description) {
        return release(appName, slugUrl, description, null);
    }

    public Future<ClientResponse> release(String appName, final String slugUrl, final String description, final String commitHead) {
        final Map<String, String> form = new HashMap<String, String>();
        form.put("description", description);
        form.put("slug_url", slugUrl);
        if (commitHead != null) form.put("head", commitHead);

        return v.path("/apps/" + appName + "/release")
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .entity(form)
                .post(ClientResponse.class);
    }

    public ClientResponse poll(ClientResponse response, Runnable listener) throws ExecutionException, InterruptedException {
        while (response.getStatus() == HttpURLConnection.HTTP_ACCEPTED) {
            final String pollingUrl = response.getHeaders().getFirst("Location");
            response = base.path(pollingUrl)
                           .accept(MediaType.APPLICATION_JSON_TYPE)
                           .get(ClientResponse.class).get();
            listener.run();
        }
        return response;
    }
}
