package com.diegocastroviadero.financemanager.app.services;

import com.diegocastroviadero.financemanager.app.model.Account;
import com.diegocastroviadero.financemanager.app.model.AccountPurpose;
import com.diegocastroviadero.financemanager.app.model.Bank;
import com.diegocastroviadero.financemanager.app.model.ImportScope;
import com.diegocastroviadero.financemanager.app.model.ImporterResult;

import java.io.File;
import java.io.InputStream;

public interface Importer {
    boolean applies(final File filename);

    boolean applies(final Bank bank, final AccountPurpose purpose);

    Bank getBank();

    ImportScope getImportScope();

    String getAccountNumber(final File file);

    ImporterResult doImport(final char[] password, final File file, final Bank bank, final String accountNumber);

    ImporterResult doImport(final char[] password, final InputStream is, final String fileName, final Account account);
}
