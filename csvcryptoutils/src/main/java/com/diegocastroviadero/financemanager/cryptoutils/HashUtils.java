package com.diegocastroviadero.financemanager.cryptoutils;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

public class HashUtils {
    public static String getHash(final List<String[]> obj) throws IOException {
        return DigestUtils.sha3_512Hex(toByteArray(obj));
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
