package com.herokuapp.janvil;

import com.google.common.io.Files;
import org.testng.annotations.Test;

import java.io.File;
import java.io.PrintWriter;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Ryan Brainard
 */
public class ManifestTest {

    @Test
    public void testHash() throws Exception {
        final File dir = Files.createTempDir();
        final File file = new File(dir, "test.txt");
        final PrintWriter writer = new PrintWriter(file);
        writer.append("HELLO");
        writer.close();

        assertEquals(Manifest.hash(file), "3733cd977ff8eb18b987357e22ced99f46097f31ecb239e878ae63760e83e4d5");
    }

    @Test
    public void testMode() throws Exception {
        final File dir = Files.createTempDir();
        final File file = new File(dir, "test.txt");
        assertTrue(file.createNewFile());

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

