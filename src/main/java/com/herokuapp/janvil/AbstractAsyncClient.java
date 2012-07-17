package com.herokuapp.janvil;

import com.sun.jersey.api.client.AsyncWebResource;
import com.sun.jersey.api.client.Client;

/**
 * @author Ryan Brainard
 */
abstract class AbstractAsyncClient {

    protected final AsyncWebResource base;

    AbstractAsyncClient(Client client, Config config, String host) {
        final String baseUrl = config.getProtocol().scheme + "://" + host;
        final UserAgentFilter userAgentFilter = new UserAgentFilter(config.getConsumersUserAgent());

        base = client.asyncResource(baseUrl);
        base.addFilter(userAgentFilter);
    }
}
