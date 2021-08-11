package com.diegocastroviadero.financemanager.app.services;

import com.diegocastroviadero.financemanager.app.model.Bank;
import com.diegocastroviadero.financemanager.app.model.ImportScope;
import com.diegocastroviadero.financemanager.app.model.ImportedFile;

import java.io.File;

public interface Importer {
    boolean applies(final File filename);

    Bank getBank();

    ImportScope getImportScope();

    String getAccountNumber(final File file);

    ImportedFile doImport(final char[] password, final File file, final Bank bank, final String accountNumber);
}
