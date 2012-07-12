package com.herokuapp.janvil;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Ryan Brainard
 */
public class JanvilIT extends BaseIT {

    Janvil janvil;

    @BeforeMethod
    protected void setUp(Method method) throws Exception {
        super.setUp(method);
        janvil = new Janvil(config);
    }

    @Test
    public void testDeploy() throws Exception {
        Manifest m = new Manifest(new File("/Users/brainard/Development/devcenter-scala"));
        m.addAll();

        assertTrue(janvil.deploy(m, appName, new HashMap<String, String>(0)).startsWith("v"));
    }
}
