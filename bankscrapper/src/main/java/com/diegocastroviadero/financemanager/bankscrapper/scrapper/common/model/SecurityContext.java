package com.diegocastroviadero.financemanager.bankscrapper.scrapper.common.model;

import com.diegocastroviadero.financemanager.bankscrapper.model.Bank;

import java.util.Map;

public class SecurityContext {
    private static final ThreadLocal<String> ENCRYPTION_PASSWORD_CONTEXT = new ThreadLocal<>();
    private static final ThreadLocal<Map<Bank, BankCredential>> BANK_CREDENTIALS_CONTEXT = new ThreadLocal<>();

    public static void setEncryptionPassword(final String encryptionPasswordContext) {
        ENCRYPTION_PASSWORD_CONTEXT.set(encryptionPasswordContext);
    }

    public static String getEncryptionPassword() {
        return ENCRYPTION_PASSWORD_CONTEXT.get();
    }

    public static void setBankCredentials(final Map<Bank, BankCredential> bankCredentials) {
        BANK_CREDENTIALS_CONTEXT.set(bankCredentials);
    }

    public static Map<Bank, BankCredential> getBankCredentials() {
        return BANK_CREDENTIALS_CONTEXT.get();
    }

    public static void clear() {
        ENCRYPTION_PASSWORD_CONTEXT.remove();
        BANK_CREDENTIALS_CONTEXT.remove();
    }
}
