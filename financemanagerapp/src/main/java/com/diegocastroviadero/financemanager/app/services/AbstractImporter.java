package com.diegocastroviadero.financemanager.app.services;

import com.diegocastroviadero.financemanager.app.configuration.ImportProperties;
import com.diegocastroviadero.financemanager.app.model.Account;
import com.diegocastroviadero.financemanager.app.model.Bank;
import com.diegocastroviadero.financemanager.app.model.ImportedFile;
import com.diegocastroviadero.financemanager.cryptoutils.exception.CsvCryptoIOException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@AllArgsConstructor
public abstract class AbstractImporter<T> implements Importer {
    protected final ImportProperties properties;
    protected final AccountService accountService;

    public ImportedFile doImport(final char[] password, final File file, final Bank bank, final String accountNumber) {
        ImportedFile importedFile = null;

        Account account = null;

        try {
            account = accountService.registerAccount(password, bank, accountNumber);
        } catch (CsvCryptoIOException e) {
            final String errorMessage = String.format("[%s] File '%s' could not be imported, there was an error registering its account. Cause: %s", this.getClass().getSimpleName(), file.getName(), e.getMessage());

            log.error(errorMessage, e);

            importedFile = getErroneousImportedFile(file, errorMessage);
        }

        if (null == account) {
            if (null == importedFile) {
                final String errorMessage = String.format("[%s] File '%s' has not been imported because its account could not be registered", this.getClass().getSimpleName(), file.getName());

                log.error(errorMessage);

                importedFile = getErroneousImportedFile(file, errorMessage);
            }
        } else {
            try {
                final List<T> movements = loadElements(file, account);

                log.debug("[{}] Import file '{}' was loaded successfully", this.getClass().getSimpleName(), file.getName());

                persistElements(password, account.getId(), movements);

                log.debug("[{}] Movements of import file '{}' were persisted successfully", this.getClass().getSimpleName(), file.getName());

                if (properties.getDeleteAfterImport()) {
                    deleteFile(file);
                }

                importedFile = getSuccessfulImportedFile(file);
            } catch (CsvCryptoIOException e) {
                final String errorMessage = String.format("[%s] There was an error persisting movements of import file '%s'. Cause: %s", this.getClass().getSimpleName(), file.getName(), e.getMessage());

                log.error(errorMessage, e);

                importedFile = getErroneousImportedFile(file, errorMessage);
            } catch (IOException e) {
                final String errorMessage = String.format("[%s] There was an error loading movements from import file '%s'. Cause: %s", this.getClass().getSimpleName(), file.getName(), e.getMessage());

                log.error(errorMessage, e);

                importedFile = getErroneousImportedFile(file, errorMessage);
            }
        }

        return importedFile;
    }

    protected abstract List<T> loadElements(final File file, final Account account) throws IOException;

    protected abstract void persistElements(final char[] password, final UUID accountId, final List<T> elements) throws IOException;

    private void deleteFile(final File file) {
        boolean deleted = file.delete();

        if (deleted) {
            log.debug("Deleted import file '{}'", file.getName());
        } else {
            log.debug("Import file '{}' was imported successfully but it could not be deleted, reimport it or delete it manually", file.getName());
        }
    }

    private ImportedFile getSuccessfulImportedFile(final File file) {
        return ImportedFile.builder()
                .file(file)
                .build();
    }

    private ImportedFile getErroneousImportedFile(final File file, final String errorCause) {
        return ImportedFile.builder()
                .file(file)
                .errorCause(errorCause)
                .build();
    }
}
