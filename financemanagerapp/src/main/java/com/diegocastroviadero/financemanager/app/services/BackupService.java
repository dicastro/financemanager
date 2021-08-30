package com.diegocastroviadero.financemanager.app.services;

import com.diegocastroviadero.financemanager.app.utils.Utils;
import com.diegocastroviadero.financemanager.app.views.common.LoadBackupException;
import com.vaadin.flow.server.StreamResource;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.springframework.stereotype.Service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

@Slf4j
@Service
@AllArgsConstructor
public class BackupService {

    private static final String TMP_DIR = System.getProperty("java.io.tmpdir");

    private final PersistencePropertiesService propertiesService;
    private final CacheService cacheService;

    public StreamResource backupFiles(final Consumer<Exception> errorLogic) {
        final String backupFileName = String.format("financemanager_backup_%s.tar.gz", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

        return new StreamResource(backupFileName, () -> {
            final File[] filesToBackup = propertiesService.listFiles();

            if (null == filesToBackup || filesToBackup.length == 0) {
                final RuntimeException e = new RuntimeException("There are no files to backup");
                errorLogic.accept(e);
                throw e;
            } else {
                log.debug("Backing up existing data ...");
                log.debug("{} files will be included in backup file '{}'", filesToBackup.length, backupFileName);

                final File backupFile = Paths.get(TMP_DIR).resolveSibling(backupFileName).toFile();

                try (final OutputStream os = new FileOutputStream(backupFile);
                     final BufferedOutputStream bos = new BufferedOutputStream(os);
                     final GzipCompressorOutputStream gzos = new GzipCompressorOutputStream(bos);
                     final TarArchiveOutputStream tos = new TarArchiveOutputStream(gzos)) {

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

    public void loadBackup(final String tarGzBackupFile, final InputStream backupInputStream) throws LoadBackupException {
        final Path backupExtractedFolder = propertiesService.createNewFolderInDbfilesBasePath();

        log.debug("Extracting backup '{}' to '{}'", tarGzBackupFile, backupExtractedFolder);

        try (final GzipCompressorInputStream gzis = new GzipCompressorInputStream(backupInputStream);
             final TarArchiveInputStream tis = new TarArchiveInputStream(gzis)) {

            TarArchiveEntry entry;

            while (null != (entry = tis.getNextTarEntry())) {
                final Path outputFile = backupExtractedFolder.resolve(entry.getName());

                Files.copy(tis, outputFile);

                log.debug("  - Extracted: {}", entry.getName());
            }

            log.info("Extracted backup '{}' to '{}'", tarGzBackupFile, backupExtractedFolder);

            propertiesService.updateDbfilesPath(backupExtractedFolder);

            cacheService.invalidateAllStartingWith(AbstractPersistenceService.PERSISTENCE_CACHE_KEY_PREFIX);
        } catch (IOException e) {
            Utils.deleteFolder(backupExtractedFolder);
            throw new LoadBackupException(String.format("There was an error extracting backup '%s'", tarGzBackupFile), e);
        }
    }
}
