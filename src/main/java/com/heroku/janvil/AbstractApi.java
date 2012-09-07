package com.heroku.janvil;

import com.sun.jersey.api.client.AsyncWebResource;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.LoggingFilter;

/**
 * @author Ryan Brainard
 */
abstract class AbstractApi {

    protected final AsyncWebResource base;

    AbstractApi(Client client, Config config, String host) {
        final String baseUrl = config.getProtocol().scheme + "://" + host;

        base = client.asyncResource(baseUrl);

        // filters get applied in reverse, so this should always go first
        if (config.getEventSubscription().getSubscribedEvents().contains(Janvil.Event.HTTP_LOGGING_BYTE)) {
            base.addFilter(new LoggingFilter(new EventAnnouncingPrintStream(config.getEventSubscription())));
        }

        base.addFilter(new UserAgentFilter(config.getConsumersUserAgent()));
        base.addFilter(new HerokuMetaMetricsFilter(config.getHerokuUser(), config.getHerokuApp()));
    }

    protected static String herokuHost() {
        final String custom = System.getenv("HEROKU_HOST");
        return custom != null ? custom : "heroku.com";
    }
}
