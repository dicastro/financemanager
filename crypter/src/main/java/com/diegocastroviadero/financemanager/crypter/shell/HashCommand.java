package com.diegocastroviadero.financemanager.crypter.shell;

import com.diegocastroviadero.financemanager.crypter.model.CrypterContext;
import com.diegocastroviadero.financemanager.crypter.utils.Constants.Extensions;
import com.diegocastroviadero.financemanager.crypter.utils.Utils;
import com.diegocastroviadero.financemanager.cryptoutils.CsvUtils;
import com.diegocastroviadero.financemanager.cryptoutils.HashUtils;
import com.diegocastroviadero.financemanager.cryptoutils.exception.CsvIOException;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Stream;

@ShellComponent
public class HashCommand {

    @ShellMethod("Hashes decrypted csv files present in workdir")
    public String hash(
            @ShellOption(arity = 0) boolean whatif
    ) {
        final File[] csvDecryptedFiles = CrypterContext.getWorkdir().toFile().listFiles(IS_CSV_DECRYPTED_FILE);

        if (null != csvDecryptedFiles && csvDecryptedFiles.length > 0) {
            System.out.printf("Following files are being hashed:%n%n");

            Stream.of(csvDecryptedFiles).forEach(file -> System.out.printf("  - %s%n", file.getName()));

            if (whatif) {
                System.out.printf("%nWhatIf activated, no files will be hashed%n");
            } else {
                System.out.printf("%nHashing ...%n");

                Stream.of(csvDecryptedFiles).forEach(file -> {
                    List<String[]> decryptedCsvElements = null;

                    try {
                        decryptedCsvElements = CsvUtils.readFromCsvFile(file);
                    } catch (CsvIOException e) {
                        System.out.printf("  - Not hashed: %s (error while reading)", file.getName());
                    }

                    if (null != decryptedCsvElements) {
                        if (decryptedCsvElements.isEmpty()) {
                            deleteDecryptedCsvFile(file);

                            System.out.printf("  - Deleted: %s (empty csv)%n", file.getName());
                        } else {
                            String hash = null;

                            try {
                                hash = HashUtils.getHash(decryptedCsvElements);
                            } catch (IOException e) {
                                System.out.printf("  - Not hashed: %s (error while calculating hash)%n", file.getName());
                            }

                            if (null != hash) {
                                try {
                                    persistHash(hash, file);

                                    System.out.printf("  - Hashed: %s%n", file.getName());
                                } catch (IOException e) {
                                    System.out.printf("  - Not hashed: %s (error while persisting hash)%n", file.getName());
                                }
                            }
                        }
                    }
                });
            }

            return "";
        } else {
            return "No decrypted csv files to hash";
        }
    }

    private final FilenameFilter IS_CSV_DECRYPTED_FILE = (dir, name) -> name.endsWith(Extensions.CSV_DECRYPTED_EXTENSION);

    private void persistHash(final String hash, final File csvDecryptedFile) throws IOException {
        Files.writeString(getCsvHashPath(csvDecryptedFile), hash, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private Path getCsvHashPath(final File file) {
        return Utils.getSiblingFile(file, Extensions.CSV_HASH_EXTENSION).toPath();
    }

    private void deleteDecryptedCsvFile(final File csvFile) {
        csvFile.delete();
    }
}