package com.heroku.janvil;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

import static org.testng.Assert.*;

/**
 * @author Ryan Brainard
 */
public class JanvilIT extends BaseIT {

    private Janvil janvil;

    @BeforeMethod
    protected void setUp(Method method) throws Exception {
        super.setUp(method);
        janvil = new Janvil(config);
    }

    @Test
    public void testBuild() throws Exception {
        final File cache = new File(dir, ".anvil/cache");
        final File slug = new File(dir, ".anvil/slug");

        assertFalse(cache.exists());
        assertFalse(slug.exists());

        Manifest m = new Manifest(dir);
        m.addAll();
        janvil.build(m);

        assertTrue(cache.exists());
        assertTrue(slug.exists());
    }

    @Test
    public void testBuildFailure() throws Exception {
        Manifest m = new Manifest(dir);
        m.addAll();
        try {
            janvil.build(m, Collections.<String,String>emptyMap(), "INVALID_BUILDPACK");
            fail();
        } catch (JanvilBuildException e) {
            assertEquals(1, e.getExitStatus());
        }
    }

    @Test
    public void testRelease() throws Exception {
        janvil.release(appName, "https://anvil-production.herokuapp.com/slugs/c51d5b81-d042-11e1-8327-2fad2fa1628b.tgz", "Janvil");
    }
}
