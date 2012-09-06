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

    private IdealizedReleasesApi ideal;
    private CoreReleasesApi core;

    @BeforeMethod
    protected void setUp(Method method) throws Exception {
        super.setUp(method);
        config.setProtocol(Config.Protocol.HTTPS);
        ideal = new IdealizedReleasesApi(Janvil.client, config);
        core = new CoreReleasesApi(Janvil.client, config);
    }

    @Test
    public void testRelease() throws Exception {
        final ClientResponse response = ideal.release(
                appName,
                "https://anvil-production.herokuapp.com/slugs/c51d5b81-d042-11e1-8327-2fad2fa1628b.tgz",
                "hello").get();
        assertEquals(response.getStatus(), 200, response.getEntity(String.class));
    }

    @Test
    public void testGetSlugUrl() throws Exception {
        assertTrue(core.getReleasesSlug(appName).get().getEntity(Map.class).containsKey("slug_url"));
    }

    @Test
    public void testGetReleaseCommit() throws Exception {
        assertTrue(core.getRelease(appName, "v1").get().getEntity(Map.class).containsKey("commit"));
    }
}
