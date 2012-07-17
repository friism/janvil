package com.herokuapp.janvil;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.lang.reflect.Method;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

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
        Manifest m = new Manifest(dir);
        m.addAll();
        janvil.build(m);
    }

    @Test
    public void testRelease() throws Exception {
        janvil.release(appName, "https://anvil-production.herokuapp.com/slugs/c51d5b81-d042-11e1-8327-2fad2fa1628b.tgz");
    }
}
