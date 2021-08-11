package com.diegocastroviadero.financemanager.app.services;

import com.diegocastroviadero.financemanager.app.configuration.ImportProperties;
import com.diegocastroviadero.financemanager.app.configuration.PersistenceProperties;
import com.diegocastroviadero.financemanager.app.model.ImportFile;
import com.diegocastroviadero.financemanager.cryptoutils.exception.CsvCryptoIOException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.springframework.stereotype.Service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class ImportService {
    private final ImportProperties importProperties;
    private final PersistenceProperties persistenceProperties;
    private final List<Importer> importers;

    public List<ImportFile> getFilesToImport(final char[] password) throws CsvCryptoIOException {
        final List<ImportFile> result;

        final File[] filesInImportPath = importProperties.getBasePath().toFile().listFiles(File::isFile);

        if (null == filesInImportPath) {
            result = Collections.emptyList();
        } else {
            result = new ArrayList<>();

            for (File file : filesInImportPath) {
                ImportFile.ImportFileBuilder importFileBuilder = null;

                for (Importer importer : importers) {
                    if (importer.applies(file)) {
                        if (null == importFileBuilder) {
                            importFileBuilder = ImportFile.builder()
                                    .file(file)
                                    .bank(importer.getBank())
                                    .accountNumber(importer.getAccountNumber(file))
                                    .password(password)
                                    .importer(importer);
                        } else {
                            importFileBuilder
                                    .importer(importer);
                        }
                    }
                }

                final ImportFile importFile;

                if (null == importFileBuilder) {
                    importFile = ImportFile.builder()
                            .file(file)
                            .build();
                } else {
                    importFile = importFileBuilder
                            .build();
                }

                result.add(importFile);
            }
        }

        return result;
    }

    public boolean backupFiles() {
        log.debug("Backing up existing data ...");

        boolean result = true;

        final String backupFileName = String.format("financemanagerapp_backup_%s.tar.gz", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

        final File backupFile = persistenceProperties.getDbfiles().getBasePath()
                .resolveSibling(backupFileName)
                .toFile();

        try (OutputStream os = new FileOutputStream(backupFile);
             BufferedOutputStream bos = new BufferedOutputStream(os);
             GzipCompressorOutputStream gzos = new GzipCompressorOutputStream(bos);
             TarArchiveOutputStream tos = new TarArchiveOutputStream(gzos)) {

            for (File file : persistenceProperties.getDbfiles().getBasePath().toFile().listFiles()) {
                final TarArchiveEntry tarEntry = new TarArchiveEntry(file, file.getName());

                tos.putArchiveEntry(tarEntry);

                Files.copy(file.toPath(), tos);

                tos.closeArchiveEntry();
            }

            tos.finish();
        } catch (Throwable e) {
            log.error("There was an unexpected error backing up existing files", e);

            if (backupFile.exists()) {
                backupFile.delete();
            }

            result = false;
        }

        return result;
    }
}
