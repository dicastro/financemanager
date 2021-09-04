package com.diegocastroviadero.financemanager.app.services;

import com.diegocastroviadero.financemanager.app.model.Account;
import com.diegocastroviadero.financemanager.app.model.AccountPurpose;
import com.diegocastroviadero.financemanager.app.model.Bank;
import com.diegocastroviadero.financemanager.app.model.ImporterResult;

import java.io.File;
import java.io.InputStream;

public interface Importer {
    boolean applies(final Bank bank, final AccountPurpose purpose);

    ImporterResult doImport(final char[] password, final InputStream is, final String fileName, final Account account);
}
