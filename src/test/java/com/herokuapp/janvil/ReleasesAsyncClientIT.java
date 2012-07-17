package com.herokuapp.janvil;

import com.sun.jersey.api.client.ClientResponse;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

import static org.testng.Assert.assertEquals;

/**
 * @author Ryan Brainard
 */
public class ReleasesAsyncClientIT extends BaseIT {

    private ReleasesAsyncClient releases;

    @BeforeMethod
    protected void setUp(Method method) throws Exception {
        super.setUp(method);
        releases = new ReleasesAsyncClient(Janvil.client, config);
    }

    @Test
    public void testRelease() throws Exception {
        final ClientResponse response = releases.release(
                appName,
                "https://anvil.herokuapp.com/slugs/b9861180-cc65-11e1-92f9-db6ecc4b02c6.img",
                "hello").get();
        assertEquals(response.getStatus(), 200, response.getEntity(String.class));
    }

}
