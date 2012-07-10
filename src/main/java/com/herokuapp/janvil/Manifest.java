package com.herokuapp.janvil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Ryan Brainard
 */
class Manifest {

    static class Entry {
        private final long mtime;
        private final String mode;
        private final String hash;

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
}
