package com.diegocastroviadero.financemanager.cryptoutils;

import com.diegocastroviadero.financemanager.cryptoutils.exception.WrongEncryptionPasswordException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CsvCryptoUtilsTest {

    private List<File> tempFiles;

    @BeforeEach
    void initializeTempFiles() {
        tempFiles = new ArrayList<>();
    }

    @AfterEach
    void deleteTempFiles() {
        tempFiles.forEach(File::delete);
        tempFiles.clear();
    }

    File getTempFile() throws IOException {
        final File file = File.createTempFile("CsvCryptoUtilsTest-", ".tmp");

        tempFiles.add(file);

        return file;
    }

    @Test
    void givenEncryptedData_whenDecryptedWithWrongPassword_thenExceptionIsThrown() throws IOException {
        // Given
        final String[] row1 = new String[] {"1A", "1B", "1C", "1D", "1E"};
        final String[] row2 = new String[] {"2A", "2B", "2C", "2D", "2E"};

        final List<String[]> elementsToEncrypt = Arrays.asList(row1, row2);

        final File tempFile = getTempFile();

        final char[] password = "123".toCharArray();
        final char[] wrongPassword = "asdf".toCharArray();

        CsvCryptoUtils.encryptToCsvFile(elementsToEncrypt, password, tempFile);

        // When
        Assertions.assertThrows(WrongEncryptionPasswordException.class, () -> CsvCryptoUtils.decryptFromCsvFile(wrongPassword, tempFile));

        // Then
        // Noting to do
    }

    @Test
    void givenEncryptedData_whenDecryptedWithCorrectPassword_thenDataIsDecryptedSuccessfully() throws IOException {
        // Given
        final String[] row1 = new String[] {"1A", "1B", "1C", "1D", "1E"};
        final String[] row2 = new String[] {"2A", "2B", "2C", "2D", "2E"};

        final List<String[]> elementsToEncrypt = Arrays.asList(row1, row2);

        final File tempFile = getTempFile();

        final char[] password = "123".toCharArray();

        CsvCryptoUtils.encryptToCsvFile(elementsToEncrypt, password, tempFile);

        // When
        final List<String[]> decryptedElements = CsvCryptoUtils.decryptFromCsvFile(password, tempFile);

        // Then
        Assertions.assertEquals(elementsToEncrypt.size(), decryptedElements.size());
        for (int i = 0; i < elementsToEncrypt.size(); i++) {
            Assertions.assertArrayEquals(elementsToEncrypt.get(i), decryptedElements.get(i));
        }
    }
}
