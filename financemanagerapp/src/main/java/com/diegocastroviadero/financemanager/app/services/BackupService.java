package com.diegocastroviadero.financemanager.app.services;

import com.diegocastroviadero.financemanager.app.configuration.PersistenceProperties;
import com.vaadin.flow.server.StreamResource;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

@Slf4j
@Service
@AllArgsConstructor
public class BackupService {

    private static final String TMP_DIR = System.getProperty("java.io.tmpdir");

    private final PersistenceProperties persistenceProperties;

    public StreamResource backupFiles(final Consumer<Exception> errorLogic) {
        final String backupFileName = String.format("financemanager_backup_%s.tar.gz", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

        return new StreamResource(backupFileName, () -> {
            final File[] filesToBackup = persistenceProperties.getDbfiles().getBasePath().toFile().listFiles();

            if (null == filesToBackup || filesToBackup.length == 0) {
                final RuntimeException e = new RuntimeException("There are no files to backup");
                errorLogic.accept(e);
                throw e;
            } else {
                log.debug("Backing up existing data ...");
                log.debug("{} files will be included in backup file '{}'", filesToBackup.length, backupFileName);

                final File backupFile = Paths.get(TMP_DIR).resolveSibling(backupFileName).toFile();

                try (final OutputStream os = new FileOutputStream(backupFile);
                     BufferedOutputStream bos = new BufferedOutputStream(os);
                     GzipCompressorOutputStream gzos = new GzipCompressorOutputStream(bos);
                     TarArchiveOutputStream tos = new TarArchiveOutputStream(gzos)) {

                    for (File file : filesToBackup) {
                        final TarArchiveEntry tarEntry = new TarArchiveEntry(file, file.getName());

                        tos.putArchiveEntry(tarEntry);

                        Files.copy(file.toPath(), tos);

                        tos.closeArchiveEntry();
                    }

                    tos.finish();

                    return new FileInputStream(backupFile);
                } catch (IOException e) {
                    errorLogic.accept(e);
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
