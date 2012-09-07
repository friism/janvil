package com.heroku.janvil;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

/**
 * @author Ryan Brainard
 */
class HerokuMetaMetricsFilter extends ClientFilter {

    private final String herokuUser;
    private final String herokuApp;

    HerokuMetaMetricsFilter(String herokuUser, String herokuApp) {
        this.herokuUser = herokuUser;
        this.herokuApp = herokuApp;
    }

    @Override
    public ClientResponse handle(ClientRequest cr) throws ClientHandlerException {
        if (herokuUser != null) {
            cr.getHeaders().add("X-Heroku-User", herokuUser);
        }

        if (herokuApp != null) {
            cr.getHeaders().add("X-Heroku-App", herokuApp);
        }

        return getNext().handle(cr);
    }
}
