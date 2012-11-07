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
        return getEnvOrElse("HEROKU_HOST", "heroku.com");
    }

    protected static String getEnvOrElse(String key, String orElse) {
        final String get = System.getenv(key);
        return get != null ? get : orElse;
    }
}
