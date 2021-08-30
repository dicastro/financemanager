package com.diegocastroviadero.financemanager.app.services;

import com.diegocastroviadero.financemanager.app.configuration.PersistenceProperties;
import com.diegocastroviadero.financemanager.app.services.CacheService;
import com.diegocastroviadero.financemanager.app.utils.Utils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;

@Slf4j
@Service
@AllArgsConstructor
public class PersistencePropertiesService {
    private static final String DBFILES_BASEPATH_KEY = "dbFilesBasePath";

    private final PersistenceProperties properties;
    private final CacheService cacheService;

    public File getFile(final String filename) {
        return getPath(filename).toFile();
    }

    public Path getPath(final String filename) {
        return getDbfilesPath().resolve(filename);
    }

    public File[] listFiles() {
        return listFiles(null);
    }

    public File[] listFiles(final FilenameFilter filenameFilter) {
        final File[] result;

        final File dbFilesFolder = getDbfilesPath().toFile();

        if (null == filenameFilter) {
            result = dbFilesFolder.listFiles();
        } else {
            result = dbFilesFolder.listFiles(filenameFilter);
        }

        return result;
    }

    public Path createNewFolderInDbfilesBasePath() {
        final Path newFolder = properties.getDbfiles().getBasePath()
                .resolve(Utils.getDateTimeAsTimestamp());

        newFolder.toFile().mkdirs();

        return newFolder;
    }

    public void updateDbfilesPath(final Path path) {
        final Path oldDbfilesPath = cacheService.put(DBFILES_BASEPATH_KEY, path);

        if (null != oldDbfilesPath) {
            Utils.deleteFolder(oldDbfilesPath);
        }
    }

    private Path getDbfilesPath() {
        return cacheService.putIfAbsent(DBFILES_BASEPATH_KEY, () -> {
            final File[] foldersInDbfilesBasePath = properties.getDbfiles().getBasePath().toFile()
                    .listFiles(File::isDirectory);

            final Path result;

            if (null == foldersInDbfilesBasePath || foldersInDbfilesBasePath.length == 0) {
                result = createNewFolderInDbfilesBasePath();
            } else {
                result = Arrays.stream(foldersInDbfilesBasePath)
                        .max(Comparator.comparing(File::getName))
                        .get()
                        .toPath();
            }

            return result;
        });
    }
}
