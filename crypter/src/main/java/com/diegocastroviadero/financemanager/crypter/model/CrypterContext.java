package com.diegocastroviadero.financemanager.crypter.model;

import java.nio.file.Path;

public class CrypterContext {
    private static final ThreadLocal<String> ENCRYPTION_PASSWORD_CONTEXT = new ThreadLocal<>();
    private static final ThreadLocal<Path> WORKDIR_CONTEXT = new ThreadLocal<>();

    public static void setEncryptionPassword(final String encryptionPasswordContext) {
        ENCRYPTION_PASSWORD_CONTEXT.set(encryptionPasswordContext);
    }

    public static String getEncryptionPassword() {
        return ENCRYPTION_PASSWORD_CONTEXT.get();
    }

    public static void setWorkdir(final Path workdir) {
        WORKDIR_CONTEXT.set(workdir);
    }

    public static Path getWorkdir() {
        return WORKDIR_CONTEXT.get();
    }

    public static void clear() {
        ENCRYPTION_PASSWORD_CONTEXT.remove();
        WORKDIR_CONTEXT.remove();
    }
}
