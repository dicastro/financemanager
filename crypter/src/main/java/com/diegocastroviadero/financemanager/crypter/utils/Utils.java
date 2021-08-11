package com.diegocastroviadero.financemanager.crypter.utils;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class Utils {
    public static File getSiblingFile(final File file, final String extension) {
        final String filenameWithoutExtension = FilenameUtils.removeExtension(file.getName());
        return file.toPath().resolveSibling(String.format("%s%s", filenameWithoutExtension, extension)).toFile();
    }
}
