package com.diegocastroviadero.financemanager.cryptoutils.exception;

public class RuntimeCsvCryptoIOException extends RuntimeException {
    public RuntimeCsvCryptoIOException(final CsvCryptoIOException cause) {
        super(cause);
    }

    public CsvCryptoIOException toCheckedException() {
        return (CsvCryptoIOException) this.getCause();
    }
}
