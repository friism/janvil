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
        return getPropOrEnvOrElse("heroku.host", "HEROKU_HOST", "heroku.com");
    }

    protected static String getPropOrEnv(String prop, String env) {
        if (System.getProperties().containsKey(prop)) {
            return System.getProperty(prop);
        } else if (System.getenv().containsKey(env)) {
            return System.getenv(env);
        } else {
            throw new IllegalArgumentException("System property [" + prop + "] and env [" + env + "] not set");
        }
    }

    protected static String getPropOrEnvOrElse(String prop, String env, String orElse) {
        try {
            return getPropOrEnv(prop, env);
        } catch (IllegalArgumentException e) {
            return orElse;
        }
    }
}
