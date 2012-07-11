package com.herokuapp.janvil;

import com.google.common.io.Files;
import com.sun.jersey.api.client.UniformInterfaceException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.*;

import static org.testng.Assert.*;

/**
 * @author Ryan Brainard
 */
public class AnvilApiTest {

    private File dir;
    private File emptyFile;
    private File staticContentsFile;
    private File randomContentsFile;
    private File subdir;
    private File subdirFile;
    private AnvilApi anvil;

    @BeforeMethod
    protected void setUp(Method method) throws Exception {
        dir = Files.createTempDir();

        emptyFile = new File(dir, "empty.file");
        assertTrue(emptyFile.createNewFile());

        staticContentsFile = new File(dir, "static.file");
        assertTrue(staticContentsFile.createNewFile());
        final PrintWriter staticWriter = new PrintWriter(staticContentsFile);
        staticWriter.append("STATIC");
        staticWriter.close();

        randomContentsFile = new File(dir, "random.file");
        assertTrue(randomContentsFile.createNewFile());
        final PrintWriter randomWriter = new PrintWriter(randomContentsFile);
        randomWriter.append(UUID.randomUUID().toString());
        randomWriter.close();

        subdir = new File(dir, "subdir");
        assertTrue(subdir.mkdir());

        subdirFile = new File(subdir, "subdir.file");
        assertTrue(subdirFile.createNewFile());

        anvil = new AnvilApi.Builder()
                .setScheme("http")
                .setPort(80)
                .setConsumersUserAgent(getClass().getSimpleName() + "." + method.getName())
                .build();
    }

    @Test
    public void testPostManifest() throws Exception {
        final Manifest manifest = createManifest();
        assertNotNull(anvil.post(manifest).getEntity(Map.class).get("id"));
    }

    @Test
    public void testDiffManifest() throws Exception {
        final Manifest manifest = createManifest();
        final Collection diff = anvil.diff(manifest).getEntity(Collection.class);
        assertEquals(diff, Collections.singleton(Manifest.hash(randomContentsFile)));
    }

    @Test
    public void testBuildManifest() throws Exception {
        final Manifest manifest = createManifest();
        final String response = anvil.build(manifest, new HashMap<String, String>()).getEntity(String.class);
        assertTrue(response.contains("Success, slug is "), response);
    }

    @Test
    public void testPostFile() throws Exception {
        try {
            anvil.get(randomContentsFile);
            fail();
        } catch (UniformInterfaceException e) {
            // expected
        }

        anvil.post(randomContentsFile);
        assertEquals(Files.toString(anvil.get(randomContentsFile).getEntity(File.class), Charset.defaultCharset()),
                Files.toString(randomContentsFile, Charset.defaultCharset()));
    }

    @Test
    public void testGetFile() throws Exception {
        assertEquals(Files.toString(anvil.get(staticContentsFile).getEntity(File.class), Charset.defaultCharset()),
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
