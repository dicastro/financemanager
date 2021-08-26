package com.diegocastroviadero.financemanager.app.views.common;

public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException() {
    }

    public AccessDeniedException(final String message) {
        super(message);
    }
}
