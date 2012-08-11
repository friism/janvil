package com.herokuapp.janvil;

import com.sun.jersey.api.client.ClientResponse;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

import static org.testng.Assert.assertEquals;

/**
 * @author Ryan Brainard
 */
public class ReleasesApiIT extends BaseIT {

    private ReleasesApi releases;

    @BeforeMethod
    protected void setUp(Method method) throws Exception {
        super.setUp(method);
        releases = new ReleasesApi(Janvil.client, config);
    }

    @Test
    public void testRelease() throws Exception {
        final ClientResponse response = releases.release(
                appName,
                "https://anvil-production.herokuapp.com/slugs/c51d5b81-d042-11e1-8327-2fad2fa1628b.tgz",
                "hello").get();
        assertEquals(response.getStatus(), 200, response.getEntity(String.class));
    }

    @Test
    public void testCopy() throws Exception {
        final ClientResponse response = releases.copy(appName, appName).get();
        assertEquals(response.getStatus(), 200, response.getEntity(String.class));
    }

}
