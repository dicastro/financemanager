package com.diegocastroviadero.financemanager.cryptoutils;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class HashUtils {
    public static String getHash(final List<String[]> obj) throws IOException {
        return DigestUtils.sha3_512Hex(toByteArray(obj));
    }

    public static String getFileChecksum(final File file) throws IOException {
        MessageDigest md;

        try {
            md = MessageDigest.getInstance("SHA3-512");
        } catch (NoSuchAlgorithmException e) {
            // this exception should never be thrown because algorithm is correct
            throw new RuntimeException(e);
        }

        try (final InputStream is = new FileInputStream(file); final DigestInputStream dis = new DigestInputStream(is, md)) {
            while (dis.read() != -1); //empty loop to clear the data
            md = dis.getMessageDigest();
        }

        return bytesToHex(md.digest());
    }

    public static String bytesToHex(byte[] bytes) {
        final StringBuilder sb = new StringBuilder();

        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }

    private static byte[] toByteArray(final List<String[]> data) throws IOException {
        try (final ByteArrayOutputStream bos = new ByteArrayOutputStream(); final OutputStreamWriter out = new OutputStreamWriter(bos)) {
            for (String[] row : data) {
                for (String column : row) {
                    if (null != column) {
                        out.write(column);
                    }
                }
            }
            out.flush();

            return bos.toByteArray();
        }
    }
}
