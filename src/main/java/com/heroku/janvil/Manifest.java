package com.heroku.janvil;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Ryan Brainard
 */
public class Manifest {

    private static final Pattern SYSTEM_FILE_SEPARATOR_PATTERN = Pattern.compile(Pattern.quote(File.separator));
    private static final String UNIX_FILE_SEPARATOR = "/";

    @SuppressWarnings("UnusedDeclaration")
    private static class Entry {

        @JsonProperty private final long mtime;
        @JsonProperty private final String mode;
        @JsonProperty private final String hash;

        Entry(File file) throws IOException {
            this.mtime = file.lastModified();
            this.mode = mode(file);
            this.hash = hash(file);
        }
    }

    static String mode(File file) {
        int perms = 0;

        perms += file.canRead()    ?  4 : 0;
        perms += file.canWrite()   ?  2 : 0;
        perms += file.canExecute() ?  1 : 0;

        // Java 6 does not allow accessing group and other file perms,
        // so just use user perms for user, group, and other.
        final String u = Integer.toString(perms);
        return 0 + u + u + u;
    }

    static String hash(File file) throws IOException {
        final MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);

            final byte[] dataBytes = new byte[1024];

            int nRead;
            while ((nRead = fis.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, nRead);
            }
        } finally {
            if (fis != null) fis.close();
        }

        //convert the byte to hex format method 1
        final StringBuilder sb = new StringBuilder();
        for (byte mdByte : md.digest()) {
            sb.append(Integer.toString((mdByte & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }

    private final File baseDir;
    private final Pattern baseDirPattern;
    private final Map<String, Entry> entries = new HashMap<String, Entry>();
    private final Map<String, File> hashes = new HashMap<String, File>();

    public Manifest(File baseDir) throws IOException {
        if (!baseDir.isDirectory()) {
            throw new IllegalArgumentException("baseDir [" + baseDir + "] must be a directory");
        }
        this.baseDir = baseDir;
        baseDirPattern = Pattern.compile(Pattern.quote(baseDir.getCanonicalPath() + File.separator));
    }

    public File getBaseDir() {
        return baseDir;
    }

    public void add(File file) throws IOException {
        final Entry entry = new Entry(file);
        entries.put(relPath(file), entry);
        hashes.put(entry.hash, file);
    }

    public void remove(File file) throws IOException {
        entries.remove(relPath(file));
    }

    String relPath(File file) throws IOException {
        final String sysRelPath = baseDirPattern.matcher(file.getCanonicalPath()).replaceFirst("");
        return SYSTEM_FILE_SEPARATOR_PATTERN.matcher(sysRelPath).replaceAll(UNIX_FILE_SEPARATOR);
    }

    public Map<String, Entry> getEntries() {
        return Collections.unmodifiableMap(entries);
    }

    public File fromHash(String hash) {
        return hashes.get(hash);
    }

    public void addAll() throws IOException {
        syncRecurse(baseDir);
    }

    // TODO: make this more robust
    private void syncRecurse(File dir) throws IOException {
        for (File child : dir.listFiles()) {
            if (child.isDirectory()) {
                syncRecurse(child);
                continue;
            }

            add(child);
        }
    }

    void writeCacheUrl(String cacheUrl) {
        writeMetadata("cache", cacheUrl);
    }

    void writeSlugUrl(String slugUrl) {
        writeMetadata("slug", slugUrl);
    }

    String readCacheUrl() {
        return readMetadata("cache");
    }

    String readSlugUrl() {
        return readMetadata("slug");
    }

    void deleteCacheUrl(String cacheUrl) {
        deleteMetadata("cache", cacheUrl);
    }

    void deleteSlugUrl(String slugUrl) {
        deleteMetadata("slug", slugUrl);
    }

    private File metadataDir() {
        return new File(baseDir, ".anvil");
    }

    private File createMetadataDir() {
        final File anvilDir = metadataDir();
        if (!anvilDir.exists()) {
            anvilDir.mkdir();
        }
        return anvilDir;
    }

    private void writeMetadata(String filename, String data) {
        final File metadataDir = createMetadataDir();

        final File file = new File(metadataDir, filename);
        if (file.exists()) {
            file.delete();
        }

        PrintWriter writer = null;
        try {
            file.createNewFile();
            writer = new PrintWriter(file);
            writer.append(data);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private String readMetadata(String filename) {
        final File metadataDir = createMetadataDir();
        final File file = new File(metadataDir, filename);
        final StringBuilder buffer = new StringBuilder();

        FileReader reader = null;
        try {
            reader = new FileReader(file);

            int i;
            while ((i = reader.read()) != -1) {
                buffer.append((char) i);
            }
        } catch (FileNotFoundException e) {
            // ignore
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }

        return buffer.toString();
    }

    private void deleteMetadata(String filename, String data) {
        final File metadataDir = metadataDir();
        new File(metadataDir, filename).delete();
    }

}
