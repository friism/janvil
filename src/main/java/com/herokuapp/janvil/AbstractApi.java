package com.herokuapp.janvil;

import com.sun.jersey.api.client.AsyncWebResource;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.LoggingFilter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

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
        base.addFilter(new LoggingFilter(new EventAnnouncingPrintStream(config.getEventSubscription())));
    }

    protected static class EventAnnouncingPrintStream extends PrintStream {
        public EventAnnouncingPrintStream(final EventSubscription<Janvil.Event> eventSubscription) {
            super(new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                    eventSubscription.announce(Janvil.Event.HTTP_LOGGING_BYTE, b);
                }
            });
        }
    }
}
