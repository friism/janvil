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
        final UserAgentFilter userAgentFilter = new UserAgentFilter(config.getConsumersUserAgent());

        base = client.asyncResource(baseUrl);
        base.addFilter(userAgentFilter);

        if (config.getEventSubscription().getSubscribedEvents().contains(Janvil.Event.HTTP_LOGGING_BYTE)) {
            base.addFilter(new LoggingFilter(new EventAnnouncingPrintStream(config.getEventSubscription())));
        }
    }

    protected static String herokuHost() {
        final String custom = System.getenv("HEROKU_HOST");
        return custom != null ? custom : "heroku.com";
    }
}
