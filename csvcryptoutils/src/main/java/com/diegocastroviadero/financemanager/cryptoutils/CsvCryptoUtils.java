package com.diegocastroviadero.financemanager.cryptoutils;

import com.diegocastroviadero.financemanager.cryptoutils.exception.CsvCryptoIOException;
import com.diegocastroviadero.financemanager.cryptoutils.exception.WrongEncryptionPasswordException;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Slf4j
public class CsvCryptoUtils {
    private static final String ENCRYPT_ALGO = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128;
    private static final int IV_LENGTH_BYTE = 12;
    private static final int SALT_LENGTH_BYTE = 16;

    public static void encryptToCsvFile(final List<String[]> elementsToPersist, final char[] encryptionPassword, final File csvFile) throws CsvCryptoIOException {
        final File csvMetaFile = getCsvMetaFile(csvFile);
        
        FileOutputStream fos;
        CipherOutputStream cos;

        try {
            final byte[] salt = CryptoUtils.getRandomNonce(SALT_LENGTH_BYTE);

            final SecretKey secretKey = CryptoUtils.getAESKeyFromPassword(encryptionPassword, salt);
            final byte[] iv = CryptoUtils.getRandomNonce(IV_LENGTH_BYTE);

            fos = new FileOutputStream(csvFile);

            persistSaltAndIv(salt, iv, csvMetaFile);

            Cipher cipher = Cipher.getInstance(ENCRYPT_ALGO);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH_BIT, iv));
            cos = new CipherOutputStream(fos, cipher);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | FileNotFoundException e) {
            // this exception should never be thrown because all crypto parameters are correct
            throw new RuntimeException("This exception should not have been thrown", e);
        } catch (IOException e) {
            throw new CsvCryptoIOException(String.format("Error while persisting salt and iv to meta file '%s'", csvMetaFile), e);
        }

        try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(cos))) {
            for (String[] element : elementsToPersist) {
                writer.writeNext(element);
            }

            log.debug("Persisted all elements to file '{}'", csvFile.getName());
        } catch (IOException e) {
            throw new CsvCryptoIOException(String.format("Error while persisting elements to encrypted file '%s'", csvFile), e);
        }

        final File csvHashFile = getCsvHashFile(csvFile);

        try {
            final String hash = HashUtils.getHash(elementsToPersist);

            persistHash(hash, csvHashFile);
        } catch (IOException e) {
            throw new CsvCryptoIOException(String.format("Error while persisting hash of elements to file '%s'", csvHashFile), e);
        }
    }

    public static List<String[]> decryptFromCsvFile(final char[] encryptionPassword, final File csvFile) throws CsvCryptoIOException {
        return decryptFromCsvFile(encryptionPassword, csvFile, false);
    }

    public static List<String[]> decryptFromCsvFile(final char[] encryptionPassword, final File csvFile, final boolean insecure) throws CsvCryptoIOException {
        final File csvMetaFile = getCsvMetaFile(csvFile);

        FileInputStream fis;
        CipherInputStream cis;

        try {
            fis = new FileInputStream(csvFile);

            final SaltAndIv saltAndIv = readSaltAndIv(csvMetaFile);

            final SecretKey secretKey = CryptoUtils.getAESKeyFromPassword(encryptionPassword, saltAndIv.getSalt());

            final Cipher cipher = Cipher.getInstance(ENCRYPT_ALGO);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH_BIT, saltAndIv.getIv()));
            cis = new CipherInputStream(fis, cipher);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | FileNotFoundException e) {
            // this exception should never be thrown because all crypto parameters are correct
            throw new RuntimeException("This exception should not have been thrown", e);
        } catch (IOException e) {
            throw new CsvCryptoIOException(String.format("Error while reading salt and iv from meta file '%s'", csvMetaFile), e);
        }

        List<String[]> readElements;

        try (CSVReader reader = new CSVReader(new InputStreamReader(cis))) {
            readElements = reader.readAll();

            log.debug("Read {} elements from file '{}'", readElements.size(), csvFile.getName());
        } catch (IOException | CsvException e) {
            throw new CsvCryptoIOException(String.format("Error while reading elements from encrypted file '%s'", csvFile.getName()), e);
        }

        if (!insecure) {
            final File csvHashFile = getCsvHashFile(csvFile);

            String readHash;
            try {
                readHash = readHash(csvHashFile);
            } catch (IOException e) {
                throw new CsvCryptoIOException(String.format("Error while reading hash of elements from file '%s'", csvHashFile), e);
            }

            String readElementsHash;
            try {
                readElementsHash = HashUtils.getHash(readElements);
            } catch (IOException e) {
                throw new CsvCryptoIOException("Error while getting hash of read elements", e);
            }

            if (!StringUtils.equals(readHash, readElementsHash)) {
                throw new WrongEncryptionPasswordException("Elements could not be decrypted because wrong password has been provided");
            }
        }

        return readElements;
    }

    public static void deleteEncryptedCsvFile(final File csvFile) {
        csvFile.delete();

        final File metaFile = getCsvMetaFile(csvFile);

        if (metaFile.exists()) {
            metaFile.delete();
        }

        final File hashFile = getCsvHashFile(csvFile);

        if (hashFile.exists()) {
            hashFile.delete();
        }
    }

    private static File getCsvMetaFile(final File csvFile) {
        final Path csvFilePath = csvFile.toPath();
        final String filenameWithoutExtension = FilenameUtils.removeExtension(csvFile.getName());

        return csvFilePath.resolveSibling(String.format("%s.meta", filenameWithoutExtension)).toFile();
    }

    private static File getCsvHashFile(final File csvFile) {
        final Path csvFilePath = csvFile.toPath();
        final String filenameWithoutExtension = FilenameUtils.removeExtension(csvFile.getName());

        return csvFilePath.resolveSibling(String.format("%s.hash", filenameWithoutExtension)).toFile();
    }

    private static void persistSaltAndIv(final byte[] salt, final byte[] iv, final File csvMetaFile) throws IOException {
        try (final FileOutputStream fos = new FileOutputStream(csvMetaFile)) {
            fos.write(salt);
            fos.write(iv);
        } catch (FileNotFoundException e) {
            // this exception should never be thrown because there should not be any problem with file existence
            throw new RuntimeException("This exception should not have been thrown", e);
        }
    }

    private static void persistHash(final String hash, final File csvHashFile) throws IOException {
        try {
            Files.writeString(csvHashFile.toPath(), hash, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (FileNotFoundException e) {
            // this exception should never be thrown because there should not be any problem with file existence
            throw new RuntimeException("This exception should not have been thrown", e);
        }
    }

    private static SaltAndIv readSaltAndIv(final File csvMetaFile) throws IOException {
        try (final FileInputStream fis = new FileInputStream(csvMetaFile)) {
            final byte[] salt = new byte[SALT_LENGTH_BYTE];
            final byte[] iv = new byte[IV_LENGTH_BYTE];

            fis.read(salt);
            fis.read(iv);

            log.debug("Read salt ({}) and iv ({})", CryptoUtils.hex(salt), CryptoUtils.hex(iv));

            return SaltAndIv.builder()
                    .salt(salt)
                    .iv(iv)
                    .build();
        } catch (FileNotFoundException e) {
            // this exception should never be thrown because there should not be any problem with file existence
            throw new RuntimeException("This exception should not have been thrown", e);
        }
    }

    private static String readHash(final File csvHashFile) throws IOException {
        try {
            final String readHash = Files.readString(csvHashFile.toPath(), StandardCharsets.UTF_8);

            log.debug("Read hash ({})", readHash);

            return readHash;
        } catch (FileNotFoundException e) {
            // this exception should never be thrown because there should not be any problem with file existence
            throw new RuntimeException("This exception should not have been thrown", e);
        }
    }

    @Builder
    @Getter
    private static class SaltAndIv {
        private final byte[] salt;
        private final byte[] iv;
    }
}
