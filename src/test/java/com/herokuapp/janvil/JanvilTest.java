package com.herokuapp.janvil;

import com.google.common.io.Files;
import com.sun.jersey.api.client.UniformInterfaceException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import static org.testng.Assert.*;

/**
 * @author Ryan Brainard
 */
public class JanvilTest {

    private File dir;
    private File emptyFile;
    private File staticContentsFile;
    private File randomContentsFile;
    private File subdir;
    private File subdirFile;
    private Janvil janvil;

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

        janvil = new Janvil.Builder()
                .setScheme("http")
                .setPort(80)
                .setConsumersUserAgent(getClass().getSimpleName() + "." + method.getName())
                .build();
    }

    @Test
    public void testPostManifest() throws Exception {
        Manifest manifest = createManifest();
        assertNotNull(janvil.post(manifest));
    }

    @Test
    public void testDiffManifest() throws Exception {
        Manifest manifest = createManifest();
        final Collection diff = janvil.diff(manifest);
        assertEquals(diff, Collections.singleton(Manifest.hash(randomContentsFile)));
    }

    @Test
    public void testPostFile() throws Exception {
        try {
            janvil.get(randomContentsFile);
            fail();
        } catch (UniformInterfaceException e) {
            // expected
        }

        janvil.post(randomContentsFile);
        assertEquals(Files.toString(janvil.get(randomContentsFile), Charset.defaultCharset()),
                Files.toString(randomContentsFile, Charset.defaultCharset()));
    }

    @Test
    public void testGetFile() throws Exception {
        assertEquals(Files.toString(janvil.get(staticContentsFile), Charset.defaultCharset()),
                     Files.toString(staticContentsFile, Charset.defaultCharset()));
    }

    private Manifest createManifest() throws IOException {
        Manifest manifest = new Manifest(dir);
        manifest.add(emptyFile);
//        manifest.add(staticContentsFile);
        manifest.add(randomContentsFile);
        manifest.add(subdirFile);
        return manifest;
    }
}
