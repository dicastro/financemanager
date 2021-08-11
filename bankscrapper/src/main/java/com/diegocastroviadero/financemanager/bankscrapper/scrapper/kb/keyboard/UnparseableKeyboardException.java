package com.diegocastroviadero.financemanager.bankscrapper.scrapper.kb.keyboard;

public class UnparseableKeyboardException extends Exception {
    public UnparseableKeyboardException(final String message) {
        super(message);
    }

    public UnparseableKeyboardException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
