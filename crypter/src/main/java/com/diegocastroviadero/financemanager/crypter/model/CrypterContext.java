package com.diegocastroviadero.financemanager.crypter.model;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class CrypterContext {
    private static final ThreadLocal<String> ENCRYPTION_PASSWORD_FILE_CONTEXT = new ThreadLocal<>();
    private static final ThreadLocal<Path> WORKDIR_CONTEXT = new ThreadLocal<>();

    public static void setEncryptionPasswordFile(final String encryptionPasswordFile) {
        ENCRYPTION_PASSWORD_FILE_CONTEXT.set(encryptionPasswordFile);
    }

    public static String getEncryptionPassword() throws IOException {
        return Files.readString(Paths.get(ENCRYPTION_PASSWORD_FILE_CONTEXT.get()));
    }

    public static void setWorkdir(final Path workdir) {
        WORKDIR_CONTEXT.set(workdir);
    }

    public static Path getWorkdir() {
        return WORKDIR_CONTEXT.get();
    }

    public static void clear() {
        ENCRYPTION_PASSWORD_FILE_CONTEXT.remove();
        WORKDIR_CONTEXT.remove();
    }
}
