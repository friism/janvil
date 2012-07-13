package com.herokuapp.janvil;

import com.sun.jersey.api.client.AsyncWebResource;
import com.sun.jersey.api.client.WebResource;

/**
 * @author Ryan Brainard
 */
abstract class AbstractApiClient {

    protected final WebResource baseResource;
    protected final AsyncWebResource asyncBaseResource;

    AbstractApiClient(Janvil.Config config, String host) {
        final String baseUrl = config.protocol.scheme + "://" + host;
        final UserAgentFilter userAgentFilter = new UserAgentFilter(config.consumersUserAgent);

        baseResource = Janvil.client.resource(baseUrl);
        baseResource.addFilter(userAgentFilter);

        asyncBaseResource = Janvil.client.asyncResource(baseUrl);
        asyncBaseResource.addFilter(userAgentFilter);
    }
}
