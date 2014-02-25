package com.heroku.janvil;

import com.heroku.api.App;
import com.heroku.api.Release;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.filter.LoggingFilter;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.*;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.heroku.janvil.Janvil.ClientType.*;
import static org.testng.Assert.*;

/**
 * @author Ryan Brainard
 */
public class JanvilIT extends BaseIT {

    private static final String SLUG_NO_PROCFILE = "https://api.anvilworks.org/slugs/c51d5b81-d042-11e1-8327-2fad2fa1628b.tgz";
    private static final String CODON_DEPLOYED_WITH_PROCFILE = "janvil-test-codon-proc";
    private static final String CODON_DEPLOYED_WITHOUT_PROCFILE = "janvil-test-codon-noproc";
    private static final String WAR_DEPLOYED_APP = "janvil-test-war-deployed";

    private Janvil janvil;

    @BeforeMethod
    protected void setUp(Method method) throws Exception {
        super.setUp(method);
        janvil = new Janvil(config);
    }

    @DataProvider
    public Object[][] binary() {
        return new Object[][] { { Boolean.TRUE }, { Boolean.FALSE } };
    }

    @Test(dataProvider = "binary")
    public void testBuild(boolean parallelUploads) throws Exception {
        final File cache = new File(dir, ".anvil/cache");
        final File slug = new File(dir, ".anvil/slug");

        assertFalse(cache.exists());
        assertFalse(slug.exists());

        Manifest m = new Manifest(dir);
        m.addAll();

        final int[] uploadCounterHolder = new int[]{0};
        config.setParallelUploads(parallelUploads);
        config.getEventSubscription().subscribe(Janvil.Event.UPLOADS_END, new EventSubscription.Subscriber<Janvil.Event>() {
            public void handle(Janvil.Event event, Object data) {
                uploadCounterHolder[0] = Integer.valueOf(data.toString());
            }
        });
        new Janvil(config).build(m);

        assertTrue(cache.exists());
        assertFalse(slug.exists());
        assertEquals(uploadCounterHolder[0], sampleFileIterations);
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
    public void testPipelinesNoKey() throws Exception {
        try {
            new Janvil(new Config("").setProtocol(Config.Protocol.HTTP)).downstreams("java");
            fail();
        } catch (JanvilRuntimeException e) {
            assertEquals(e.getMessage(), "Heroku API key not found or invalid");
        }
    }

    @Test
    public void testPipelinesBadApiKey() throws Exception {
        try {
            new Janvil(new Config("BAD_API_KEY").setProtocol(Config.Protocol.HTTP)).downstreams("java");
            fail();
        } catch (JanvilRuntimeException e) {
            assertEquals(e.getMessage(), "Heroku API key not found or invalid");
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
    public void testPipelinesNoAppAccess_Async() throws Exception {
        try {
            janvil.promote("java");
            fail();
        } catch (JanvilRuntimeException e) {
            assertEquals(e.getMessage(), "No access to app java");
        }
    }

    @Test
    public void testPipelinesNoAppAccess_WithBody() throws Exception {
        try {
            janvil.copy("java", "java", "no access with body");
            fail();
        } catch (JanvilRuntimeException e) {
            assertEquals(e.getMessage(), "No access to app java");
        }
    }

    @Test
    public void testCopyNoAccessToSourceApp() throws Exception {
        withApp(new AppRunnable() {
            public void run(App app) throws Exception {
                try {
                    janvil.copy("java", app.getName(), "no access to source");
                    fail();
                } catch (JanvilRuntimeException e) {
                    assertEquals(e.getMessage(), "No access to app java");
                }
            }
        });
    }

    @Test
    public void testCopyNoAccessToTargetApp() throws Exception {
        withApp(new AppRunnable() {
            public void run(App app) throws Exception {
                try {
                    janvil.copy(app.getName(), "java", "no access to source");
                    fail();
                } catch (JanvilRuntimeException e) {
                    assertEquals(e.getMessage(), "No access to app java");
                }
            }
        });
    }

    @Test
    public void testStandardNonOkHandlePipelinesErrors() throws Exception {
        withApp(new AppRunnable() {
            public void run(App app) throws Exception {
                try {
                    janvil.addDownstream(app.getName(), app.getName());
                    fail();
                } catch (JanvilRuntimeException e) {
                    assertEquals(e.getMessage(), "Downstream app cannot be recursive");
                }
            }
        });
    }

    @Test
    public void testCopyOfCodonDeployedAppWithProcfile() throws Exception {
        assertCopyOf(CODON_DEPLOYED_WITH_PROCFILE);
    }

    @Test
    public void testCopyOfCodonDeployedAppWithoutProcfile() throws Exception {
        assertCopyOf(CODON_DEPLOYED_WITHOUT_PROCFILE);
    }

    @Test
    public void testCopyWarDeployedApp() throws Exception {
        assertCopyOf(WAR_DEPLOYED_APP);
    }

    private void assertCopyOf(final String sourceAppName) throws Exception {
        withApp(new AppRunnable() {
            public void run(App target) throws Exception {
                final Client testClient = Janvil.getClient(FIXED_LENGTH);
                testClient.addFilter(new LoggingFilter(System.out));
                final App source = herokuApi.getApp(sourceAppName);
                final List<Release> sourceReleases = herokuApi.listReleases(source.getName());
                final Release sourceLastRelease = sourceReleases.get(sourceReleases.size() - 1);
                final String sourceCommitHead = sourceLastRelease.getCommit();
                final Map<String, String> sourcePs = sourceLastRelease.getPSTable();
                final String sourceContent = getWebContent(testClient, source);

                final String description = "copy";
                janvil.copy(source.getName(), target.getName(), description);

                target = herokuApi.getApp(target.getName()); // refresh app info
                assertEquals(target.getBuildpackProvidedDescription(), source.getBuildpackProvidedDescription());

                final List<Release> targetReleases = herokuApi.listReleases(target.getName());
                final Release targetLastRelease = targetReleases.get(targetReleases.size() - 1);
                assertEquals(targetLastRelease.getDescription(), description);
                assertEquals(targetLastRelease.getCommit(), sourceCommitHead);
                assertEquals(targetLastRelease.getPSTable(), sourcePs);
                assertEquals(getWebContent(testClient, target), sourceContent);
            }

            private String getWebContent(Client testClient, App target) throws IOException, InterruptedException {
                final int maxRetries = 5;
                int retries = 0;
                while (true) {
                    try {
                        return testClient.resource(target.getWebUrl()).get(String.class);
                    } catch (UniformInterfaceException e) {
                        System.err.println("Call to " + target.getWebUrl() +
                                           " failed with status " + e.getResponse().getStatus() + ". " +
                                           (maxRetries - retries) + " remaining.");

                        if (retries++ <= maxRetries) {
                            Thread.sleep(5000);
                            continue;
                        }

                        throw e;
                    }
                }
            }
        });
    }

    @Test
    public void testPipelinePromotion() throws Exception {
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
                janvil.release(upstream, SLUG_NO_PROCFILE, "testPipelinePromotion", commitHead);

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
