package com.diegocastroviadero.financemanager.app.services;

import com.diegocastroviadero.financemanager.app.configuration.BuildProperties;
import com.diegocastroviadero.financemanager.app.utils.Utils;
import com.diegocastroviadero.financemanager.app.views.common.LoadBackupException;
import com.diegocastroviadero.financemanager.cryptoutils.HashUtils;
import com.vaadin.flow.server.StreamResource;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

@Slf4j
@Service
@AllArgsConstructor
public class BackupService {

    private static final String TMP_DIR = System.getProperty("java.io.tmpdir");
    private static final String BACKUP_META_FILENAME = "BACKUP-META";
    private static final String BACKUP_FILENAME = "financemanager_backup.tar.gz";

    private final BuildProperties buildProperties;
    private final PersistencePropertiesService propertiesService;
    private final CacheService cacheService;

    public StreamResource backupFiles(final Consumer<Exception> errorLogic) {
        return new StreamResource(BACKUP_FILENAME, () -> {
            final LocalDateTime backupDate = LocalDateTime.now();

            // Get files to back up, excluding backup metadata file
            final File[] filesToBackup = propertiesService
                    .listFiles(((dir, name) -> !StringUtils.equals(name, BACKUP_META_FILENAME)));

            if (null == filesToBackup || filesToBackup.length == 0) {
                final RuntimeException e = new RuntimeException("There are no files to backup");
                errorLogic.accept(e);
                throw e;
            } else {
                log.debug("Generating backup meta data file ...");

                File backupMetaFile;
                try {
                    backupMetaFile = generateMetaFile(backupDate, filesToBackup);
                } catch (IOException e) {
                    final RuntimeException r = new RuntimeException("There was an error generating metadata backup file", e);
                    errorLogic.accept(r);
                    throw r;
                }

                log.debug("Backing up existing data ...");

                log.debug("{} files will be included in backup file '{}'", filesToBackup.length, BACKUP_FILENAME);

                final File backupFile = Paths.get(TMP_DIR).resolveSibling(BACKUP_FILENAME).toFile();

                try (final OutputStream os = new FileOutputStream(backupFile);
                     final BufferedOutputStream bos = new BufferedOutputStream(os);
                     final GzipCompressorOutputStream gzos = new GzipCompressorOutputStream(bos);
                     final TarArchiveOutputStream tos = new TarArchiveOutputStream(gzos)) {

                    addEntryToTar(backupMetaFile, tos);

                    for (File file : filesToBackup) {
                        addEntryToTar(file, tos);
                    }

                    tos.finish();

                    return new FileInputStream(backupFile);
                } catch (IOException e) {
                    final RuntimeException r = new RuntimeException("There was an error compressing all backup files in a tar file", e);
                    errorLogic.accept(r);
                    throw r;
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

    private void addEntryToTar(final File file, final TarArchiveOutputStream tos) throws IOException {
        final TarArchiveEntry tarEntry = new TarArchiveEntry(file, file.getName());

        tos.putArchiveEntry(tarEntry);

        Files.copy(file.toPath(), tos);

        tos.closeArchiveEntry();
    }

    private File generateMetaFile(final LocalDateTime backupDate, final File[] backupFiles) throws IOException {
        final StringBuilder metaContent = new StringBuilder();

        metaContent
                .append("Version:   ").append(buildProperties.getVersionFull()).append("\n")
                .append("Date:      ").append(backupDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss.SSS"))).append("\n")
                .append("Nb. Files: ").append(backupFiles.length).append("\n")
                .append("Files:\n");

        for (final File backupFile : backupFiles) {
            metaContent
                    .append("  - name:     ").append(backupFile.getName()).append("\n")
                    .append("    checksum: ").append(HashUtils.getFileChecksum(backupFile)).append("\n");
        }

        final File metaFile = propertiesService.getFile(BACKUP_META_FILENAME);

        Files.writeString(metaFile.toPath(), metaContent.toString(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);

        return metaFile;
    }
}
