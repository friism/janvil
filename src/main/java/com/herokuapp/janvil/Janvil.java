package com.herokuapp.janvil;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;

/**
 * @author Ryan Brainard
 */
public class Janvil {

    static final Client universalClient;  //TODO: clean up how this is fed in

    static {
        final ClientConfig config = new DefaultClientConfig();
        config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        config.getProperties().put(ClientConfig.PROPERTY_CHUNKED_ENCODING_SIZE, -1 /* default chunk size */);

        universalClient = com.sun.jersey.api.client.Client.create(config);
    }

}
