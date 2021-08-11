package com.diegocastroviadero.financemanager.crypter.shell;

import com.diegocastroviadero.financemanager.crypter.model.CrypterContext;
import com.diegocastroviadero.financemanager.crypter.utils.Constants.Extensions;
import com.diegocastroviadero.financemanager.crypter.utils.Utils;
import com.diegocastroviadero.financemanager.cryptoutils.CsvCryptoUtils;
import com.diegocastroviadero.financemanager.cryptoutils.CsvUtils;
import com.diegocastroviadero.financemanager.cryptoutils.exception.CsvCryptoIOException;
import com.diegocastroviadero.financemanager.cryptoutils.exception.CsvIOException;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;
import java.util.stream.Stream;

@ShellComponent
public class EncryptCommand {

    @ShellMethod("Encrypts decrypted csv files present in workdir")
    public String encrypt(
            @ShellOption(arity = 0) boolean whatif
    ) {
        final File[] csvDecryptedFiles = CrypterContext.getWorkdir().toFile().listFiles(IS_CSV_DECRYPTED_FILE);

        if (null != csvDecryptedFiles && csvDecryptedFiles.length > 0) {
            System.out.printf("Following files are being encrypted:%n%n");

            Stream.of(csvDecryptedFiles).forEach(file -> System.out.printf("  - %s%n", file.getName()));

            if (whatif) {
                System.out.printf("%nWhatIf activated, no files will be encrypted%n");
            } else {
                System.out.printf("%nEncrypting ...%n");

                Stream.of(csvDecryptedFiles).forEach(file -> {
                    List<String[]> elements = null;

                    try {
                        elements = CsvUtils.readFromCsvFile(file);
                    } catch (CsvIOException e) {
                        System.out.printf("  - Not encrypted: %s (error while reading file)%n", file.getName());
                    }

                    if (null != elements) {
                        final File csvTmpFile = getCsvTmpFile(file);
                        final File csvEncryptedFile = getCsvEncryptedFile(file);

                        try {
                            backupCsvDecryptedFile(file, csvTmpFile);

                            CsvCryptoUtils.encryptToCsvFile(elements, CrypterContext.getEncryptionPassword().toCharArray(), csvEncryptedFile);

                            rollbackCsvTmpFile(csvTmpFile);

                            System.out.printf("  - Encrypted: %s%n", file.getName());
                        } catch (CsvCryptoIOException e) {
                            System.out.printf("  - Not encrypted: %s (error while encrypting)", file.getName());

                            rollbackCsvTmpFile(csvTmpFile);
                        }
                    }
                });
            }

            return "";
        } else {
            return "No decrypted csv files to encrypt";
        }
    }

    private final FilenameFilter IS_CSV_DECRYPTED_FILE = (dir, name) -> name.endsWith(Extensions.CSV_DECRYPTED_EXTENSION);

    private File getCsvTmpFile(final File csvDecryptedFile) {
        return Utils.getSiblingFile(csvDecryptedFile, Extensions.TEMP_CSV_EXTENSION);
    }

    private File getCsvEncryptedFile(final File csvDecryptedFile) {
        return Utils.getSiblingFile(csvDecryptedFile, Extensions.CSV_ENCRYPTED_EXTENSION);
    }

    private void backupCsvDecryptedFile(final File csvDecryptedFile, final File csvTmpFile) {
        csvDecryptedFile.renameTo(csvTmpFile);
    }

    private void rollbackCsvTmpFile(final File csvTmpFile) {
        if (csvTmpFile.exists()) {
            csvTmpFile.delete();
        }
    }
}