package com.diegocastroviadero.financemanager.cryptoutils.exception;

import java.io.IOException;

public class CsvCryptoIOException extends IOException {
    public CsvCryptoIOException(final String message) {
        super(message);
    }

    public CsvCryptoIOException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
