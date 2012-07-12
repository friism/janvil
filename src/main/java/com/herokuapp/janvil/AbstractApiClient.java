package com.herokuapp.janvil;

import com.sun.jersey.api.client.WebResource;

/**
 * @author Ryan Brainard
 */
abstract class AbstractApiClient {

    protected final WebResource baseResource;

    AbstractApiClient(Janvil.Config config, String host) {
        baseResource = Janvil.client.resource(config.protocol.scheme + "://" + host);
        baseResource.addFilter(new UserAgentFilter(config.consumersUserAgent));
    }

}
