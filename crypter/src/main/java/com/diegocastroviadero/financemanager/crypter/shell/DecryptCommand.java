package com.diegocastroviadero.financemanager.crypter.shell;

import com.diegocastroviadero.financemanager.crypter.model.CrypterContext;
import com.diegocastroviadero.financemanager.crypter.utils.Constants.Extensions;
import com.diegocastroviadero.financemanager.crypter.utils.Utils;
import com.diegocastroviadero.financemanager.cryptoutils.CsvCryptoUtils;
import com.diegocastroviadero.financemanager.cryptoutils.CsvUtils;
import com.diegocastroviadero.financemanager.cryptoutils.exception.CsvCryptoIOException;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

@ShellComponent
public class DecryptCommand {

    @ShellMethod("Decrypts encrypted csv files present in workdir")
    public String decrypt(
            @ShellOption(arity = 0) boolean insecure,
            @ShellOption(arity = 0) boolean whatif
    ) {
        final File[] csvEncryptedFiles = CrypterContext.getWorkdir().toFile().listFiles(IS_CSV_ENCRYPTED_FILE);

        if (null != csvEncryptedFiles && csvEncryptedFiles.length > 0) {
            System.out.printf("Following files are being decrypted:%n%n");

            Stream.of(csvEncryptedFiles).forEach(file -> System.out.printf("  - %s%n", file.getName()));

            if (whatif) {
                System.out.printf("%nWhatIf activated, no files will be decrypted%n");
            } else {
                final AtomicReference<String> encryptionPassword = new AtomicReference<>();

                try {
                    encryptionPassword.set(CrypterContext.getEncryptionPassword());
                } catch (Exception ignore) {
                }

                if (null == encryptionPassword.get()) {
                    System.out.printf("%nDecryption could not be done, there was an error reading encryption password from file");
                } else {
                    System.out.printf("%nDecrypting ...%n");

                    Stream.of(csvEncryptedFiles).forEach(file -> {
                        List<String[]> decryptedCsvElements = null;

                        try {
                            decryptedCsvElements = CsvCryptoUtils.decryptFromCsvFile(encryptionPassword.get().toCharArray(), file, insecure);
                        } catch (CsvCryptoIOException e) {
                            System.out.printf("  - Not decrypted: %s (error while decrypting)%n", file.getName());
                        }

                        if (null != decryptedCsvElements) {
                            if (decryptedCsvElements.isEmpty()) {
                                CsvCryptoUtils.deleteEncryptedCsvFile(file);

                                System.out.printf("  - Deleted: %s (empty csv)%n", file.getName());
                            } else {
                                final File csvTmpFile = getCsvTmpFile(file);

                                try {
                                    CsvUtils.persistToCsvFile(decryptedCsvElements, csvTmpFile);

                                    CsvCryptoUtils.deleteEncryptedCsvFile(file);

                                    commitCsvTmpFile(csvTmpFile);

                                    System.out.printf("  - Decrypted: %s%n", file.getName());
                                } catch (IOException e) {
                                    System.out.printf("  - Not decrypted: %s (error while persisting)%n", file.getName());

                                    rollbackCsvTmpFile(csvTmpFile);
                                }
                            }
                        }
                    });
                }
            }

            return "";
        } else {
            return "No encrypted csv files to decrypt";
        }
    }

    private final FilenameFilter IS_CSV_ENCRYPTED_FILE = (dir, name) -> name.endsWith(Extensions.CSV_ENCRYPTED_EXTENSION);

    private File getCsvTmpFile(final File file) {
        return Utils.getSiblingFile(file, Extensions.TEMP_CSV_EXTENSION);
    }

    private void commitCsvTmpFile(final File csvTmpFile) {
        csvTmpFile.renameTo(Utils.getSiblingFile(csvTmpFile, Extensions.CSV_DECRYPTED_EXTENSION));
    }

    private void rollbackCsvTmpFile(final File csvTmpFile) {
        if (csvTmpFile.exists()) {
            csvTmpFile.delete();
        }
    }
}