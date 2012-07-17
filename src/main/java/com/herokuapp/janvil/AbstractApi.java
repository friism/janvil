package com.herokuapp.janvil;

import com.sun.jersey.api.client.AsyncWebResource;
import com.sun.jersey.api.client.Client;

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
    }
}
