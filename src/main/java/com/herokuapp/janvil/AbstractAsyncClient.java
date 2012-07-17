package com.herokuapp.janvil;

import com.sun.jersey.api.client.AsyncWebResource;

/**
 * @author Ryan Brainard
 */
abstract class AbstractAsyncClient {

    protected final AsyncWebResource base;

    AbstractAsyncClient(Janvil.Config config, String host) {
        final String baseUrl = config.protocol.scheme + "://" + host;
        final UserAgentFilter userAgentFilter = new UserAgentFilter(config.consumersUserAgent);

        base = Janvil.client.asyncResource(baseUrl);
        base.addFilter(userAgentFilter);
    }
}
