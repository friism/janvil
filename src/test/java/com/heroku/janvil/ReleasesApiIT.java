package com.heroku.janvil;

import com.sun.jersey.api.client.ClientResponse;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Ryan Brainard
 */
public class ReleasesApiIT extends BaseIT {

    private CisaurusApi cisaurus;

    @BeforeMethod
    protected void setUp(Method method) throws Exception {
        super.setUp(method);
        cisaurus = new CisaurusApi(Janvil.client, config);
    }

    @Test
    public void testRelease() throws Exception {
        final ClientResponse response = cisaurus.poll(cisaurus.release(
                appName,
                "https://anvil-production.herokuapp.com/slugs/c51d5b81-d042-11e1-8327-2fad2fa1628b.tgz",
                "hello").get(),
                new Runnable() { public void run() {} });
        assertEquals(response.getStatus(), 200, response.getEntity(String.class));
    }
}
