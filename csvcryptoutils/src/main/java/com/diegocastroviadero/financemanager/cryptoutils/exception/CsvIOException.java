package com.diegocastroviadero.financemanager.cryptoutils.exception;

import java.io.IOException;

public class CsvIOException extends IOException {
    public CsvIOException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public RuntimeCsvIOException toUncheckedException() {
        return new RuntimeCsvIOException(this);
    }
}
