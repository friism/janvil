package com.heroku.janvil;

import com.heroku.api.App;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.*;

/**
 * @author Ryan Brainard
 */
public class JanvilIT extends BaseIT {

    private static final String SAMPLE_BUILD_URL = "https://anvil-production.herokuapp.com/slugs/c51d5b81-d042-11e1-8327-2fad2fa1628b.tgz";

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
        assertFalse(slug.exists());
    }

    @Test
    public void testBuildFailure() throws Exception {
        Manifest m = new Manifest(dir);
        m.addAll();
        try {
            janvil.build(m, Collections.<String, String>emptyMap(), "INVALID_BUILDPACK");
            fail();
        } catch (JanvilBuildException e) {
            assertEquals(1, e.getExitStatus());
        }
    }

    @Test
    public void testPipelinesBadApiKey() throws Exception {
        try {
            new Janvil(new Config("BAD_API_KEY").setProtocol(Config.Protocol.HTTP)).downstreams("java");
            fail();
        } catch (JanvilRuntimeException e) {
            assertEquals(e.getMessage(), "No access to app java");
        }
    }

    @Test
    public void testPipelinesNoAppAccess() throws Exception {
        try {
            janvil.downstreams("java");
            fail();
        } catch (JanvilRuntimeException e) {
            assertEquals(e.getMessage(), "No access to app java");
        }
    }

    @Test
    public void testKitchenSink() throws Exception {
        withApps(2, new AppsRunnable() {
            public void run(App[] apps) throws Exception {
                final String upstream = apps[0].getName();
                final String downstream = apps[1].getName();

                final List<String> noDownstreams = janvil.downstreams(upstream);
                assertEquals(noDownstreams.size(), 0);

                janvil.addDownstream(upstream, downstream);

                final List<String> oneDownstream = janvil.downstreams(upstream);
                assertEquals(oneDownstream.size(), 1);
                assertEquals(oneDownstream.get(0), downstream);

                final List<String> noDiff = janvil.diffDownstream(upstream);
                assertEquals(noDiff.size(), 0);

                final String commitHead = "1234567";
                janvil.release(upstream, SAMPLE_BUILD_URL, "Kitchen Sink", commitHead);

                final List<String> oneDiff = janvil.diffDownstream(upstream);
                assertEquals(oneDiff.size(), 1);
                assertEquals(oneDiff.get(0), commitHead);

                janvil.promote(upstream);

                final List<String> noDiffAfterPromote = janvil.diffDownstream(upstream);
                assertEquals(noDiffAfterPromote.size(), 0);

                janvil.removeDownstream(upstream, downstream);

                final List<String> noDownstreamsAgain = janvil.downstreams(upstream);
                assertEquals(noDownstreamsAgain.size(), 0);
            }
        });
    }
}
