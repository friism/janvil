package com.herokuapp.janvil;

import com.google.common.io.Files;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Ryan Brainard
 */
public class JanvilTest {

    private File dir;
    private File file;
    private File subdir;
    private File subdirFile;
    private Janvil janvil;

    @BeforeMethod
    protected void setUp() throws Exception {
        dir = Files.createTempDir();

        file = new File(dir, "file.txt");
        assertTrue(file.createNewFile());

        subdir = new File(dir, "subdir");
        assertTrue(subdir.mkdir());

        subdirFile = new File(subdir, "subdirFile.txt");
        assertTrue(subdirFile.createNewFile());

        janvil = new Janvil.Builder()
                .setScheme("http")
                .setPort(80)
                .setConsumersUserAgent(getClass().getSimpleName())
                .build();
    }

    @Test
    public void testPostManifest() throws Exception {
        Manifest manifest = new Manifest(dir);
        manifest.add(file);
        manifest.add(subdirFile);

        assertNotNull(janvil.post(manifest));
    }
}
