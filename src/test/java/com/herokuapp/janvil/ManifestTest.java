package com.herokuapp.janvil;

import com.google.common.io.Files;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Ryan Brainard
 */
public class ManifestTest {

    private File dir;
    private File file;
    private File subdir;
    private File subdirFile;

    @BeforeMethod
    protected void setUp() throws Exception {
        dir = Files.createTempDir();

        file = new File(dir, "file.txt");
        assertTrue(file.createNewFile());

        subdir = new File(dir, "subdir");
        assertTrue(subdir.mkdir());

        subdirFile = new File(subdir, "subdirFile.txt");
        assertTrue(subdirFile.createNewFile());
    }

    @Test
    public void testAddFile() throws Exception {
        Manifest manifest = new Manifest(dir);
        manifest.add(file);

        assertEquals(manifest.entries.keySet().iterator().next(), file.getName());
    }

    @Test
    public void testRelPath() throws Exception {
        Manifest manifest = new Manifest(dir);
        assertEquals(manifest.relPath(file), file.getName());
        assertEquals(manifest.relPath(subdirFile), subdir.getName() + "/" + subdirFile.getName());
    }

    @Test
    public void testHash() throws Exception {
        assertEquals(Manifest.hash(file), "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
    }

    @Test
    public void testMode() throws Exception {
        assertTrue(file.setReadable(true));
        assertTrue(file.setWritable(false));
        assertTrue(file.setExecutable(false));
        assertEquals(Manifest.mode(file), "0444");

        assertTrue(file.setReadable(true));
        assertTrue(file.setWritable(true));
        assertTrue(file.setExecutable(false));
        assertEquals(Manifest.mode(file), "0666");

        assertTrue(file.setReadable(true));
        assertTrue(file.setWritable(true));
        assertTrue(file.setExecutable(true));
        assertEquals(Manifest.mode(file), "0777");
    }
}

