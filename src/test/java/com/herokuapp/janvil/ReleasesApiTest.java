package com.herokuapp.janvil;

import com.sun.jersey.api.client.ClientResponse;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author Ryan Brainard
 */
public class ReleasesApiTest {

    @Test
    public void testRelease() throws Exception {
        final ReleasesApi releases = new ReleasesApi.Builder(System.getenv("HEROKU_API_KEY")).setScheme("http").setPort(80).build();
        final ClientResponse response = releases.release(System.getenv("HEROKU_APP_NAME"), "https://anvil.herokuapp.com/slugs/1612fd00-cb9e-11e1-a836-b51f097dae2c.img", "hello");
        assertEquals(response.getStatus(), 200);
    }

}
