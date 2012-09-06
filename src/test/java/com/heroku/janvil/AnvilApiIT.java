package com.heroku.janvil;

import com.google.common.io.Files;
import com.sun.jersey.api.client.ClientResponse;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Ryan Brainard
 */
public class AnvilApiIT extends BaseIT {

    private AnvilApi anvil;

    @BeforeMethod
    protected void setUp(Method method) throws Exception {
        super.setUp(method);
        anvil = new AnvilApi(Janvil.client, config);
    }

    @Test
    public void testPostManifest() throws Exception {
        final Manifest manifest = createManifest();
        assertEquals(anvil.post(manifest).get().getEntity(String.class), "ok");
    }

    @Test
    public void testDiffManifest() throws Exception {
        final Manifest manifest = createManifest();
        final Collection diff = anvil.diff(manifest).get().getEntity(Collection.class);
        assertEquals(diff, Collections.singleton(Manifest.hash(randomContentsFile)));
    }

    @Test
    public void testBuildManifest() throws Exception {
        final Manifest manifest = createManifest();
        final String response = anvil.build(manifest, new HashMap<String, String>(), "", "").get().getEntity(String.class);
        assertTrue(response.contains("Success, slug is "), response);
    }

    @Test
    public void testPostFile() throws Exception {
        final ClientResponse before = anvil.get(randomContentsFile).get();
        assertEquals(before.getStatus(), HttpURLConnection.HTTP_BAD_GATEWAY);

        anvil.post(randomContentsFile).get();
        assertEquals(Files.toString(anvil.get(randomContentsFile).get().getEntity(File.class), Charset.defaultCharset()),
                     Files.toString(randomContentsFile, Charset.defaultCharset()));
    }

    @Test
    public void testGetFile() throws Exception {
        assertEquals(Files.toString(anvil.get(staticContentsFile).get().getEntity(File.class), Charset.defaultCharset()),
                Files.toString(staticContentsFile, Charset.defaultCharset()));
    }

    private Manifest createManifest() throws IOException {
        final Manifest manifest = new Manifest(dir);
        manifest.add(emptyFile);
        manifest.add(staticContentsFile);
        manifest.add(randomContentsFile);
        manifest.add(subdirFile);
        return manifest;
    }
}
