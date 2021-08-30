package com.diegocastroviadero.financemanager.app.views.common;

public class LoadBackupException extends Exception {
    public LoadBackupException(final String message) {
        super(message);
    }

    public LoadBackupException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
