package com.diegocastroviadero.financemanager.cryptoutils.exception;

public class RuntimeCsvIOException extends RuntimeException {
    public RuntimeCsvIOException(final CsvIOException cause) {
        super(cause);
    }

    public CsvIOException toCheckedException() {
        return (CsvIOException) this.getCause();
    }
}
