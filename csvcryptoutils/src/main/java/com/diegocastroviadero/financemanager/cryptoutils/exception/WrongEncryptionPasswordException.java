package com.diegocastroviadero.financemanager.cryptoutils.exception;

import java.io.IOException;

public class WrongEncryptionPasswordException extends CsvCryptoIOException {
    public WrongEncryptionPasswordException(final String message) {
        super(message);
    }
}
