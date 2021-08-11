package com.diegocastroviadero.financemanager.bankscrapper.scrapper.common.model;

import com.diegocastroviadero.financemanager.bankscrapper.model.Bank;

import java.io.IOException;

public interface BankCredentialReader {
    Bank getBank();

    boolean applies(final String rawCredentials);

    BankCredential parseCredentials(final String rawCredentials) throws IOException;

    BankCredential readBankCredentials();
}
