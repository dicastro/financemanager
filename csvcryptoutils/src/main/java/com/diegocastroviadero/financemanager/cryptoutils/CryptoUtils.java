package com.diegocastroviadero.financemanager.cryptoutils;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.List;

public class CryptoUtils {
    private static final String AES_ALGORITHM = "AES";
    private static final int KEY_LENGTH_BIT = 256;

    public static byte[] getRandomNonce(final int numBytes) {
        byte[] nonce = new byte[numBytes];

        new SecureRandom().nextBytes(nonce);

        return nonce;
    }

    /**
     * Gets AES key derived from a password
     */
    public static SecretKey getAESKeyFromPassword(char[] password, byte[] salt) {
        try {
            final SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            // iterationCount = 65536
            // keyLength = 256
            final KeySpec spec = new PBEKeySpec(password, salt, 65536, KEY_LENGTH_BIT);

            return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), AES_ALGORITHM);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            // These exceptions should never be thrown because:
            //   - AES is a valid algorithm
            //   - The KeySpec is right
            throw new RuntimeException("This exception should not have been thrown", e);
        }
    }

    /**
     * Returns hex representation of bytes
     * @param bytes given bytes
     * @return string containing hex representation of given bytes
     */
    public static String hex(byte[] bytes) {
        final StringBuilder result = new StringBuilder();

        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }

        return result.toString();
    }

    /**
     * Returns hex representation with block size split
     * @param bytes given bytes
     * @param blockSize block size to split hex representation
     * @return string containing hex split representation of given bytes
     */
    public static String hexWithBlockSize(byte[] bytes, int blockSize) {
        final String hex = hex(bytes);

        // one hex = 2 chars
        blockSize = blockSize * 2;

        // better idea how to print this?
        final List<String> result = new ArrayList<>();

        int index = 0;
        while (index < hex.length()) {
            result.add(hex.substring(index, Math.min(index + blockSize, hex.length())));
            index += blockSize;
        }

        return result.toString();
    }
}
