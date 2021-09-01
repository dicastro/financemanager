package com.diegocastroviadero.financemanager.app.services;

import com.diegocastroviadero.financemanager.app.configuration.ImportProperties;
import com.diegocastroviadero.financemanager.app.model.Account;
import com.diegocastroviadero.financemanager.app.model.Bank;
import com.diegocastroviadero.financemanager.app.model.ImporterResult;
import com.diegocastroviadero.financemanager.cryptoutils.exception.CsvCryptoIOException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Slf4j
@AllArgsConstructor
public abstract class AbstractImporter<T> implements Importer {
    protected final ImportProperties properties;
    protected final AccountService accountService;

    @Override
    public ImporterResult doImport(final char[] password, final File file, final Bank bank, final String accountNumber) {
        ImporterResult importerResult = null;

        Account account = null;

        try {
            account = accountService.registerAccount(password, bank, accountNumber);
        } catch (CsvCryptoIOException e) {
            final String errorMessage = String.format("[%s] File '%s' could not be imported, there was an error registering its account. Cause: %s", this.getClass().getSimpleName(), file.getName(), e.getMessage());

            log.error(errorMessage, e);

            importerResult = getErroneousImportedFile(file.getName(), errorMessage);
        }

        if (null == account) {
            if (null == importerResult) {
                final String errorMessage = String.format("[%s] File '%s' has not been imported because its account could not be registered", this.getClass().getSimpleName(), file.getName());

                log.error(errorMessage);

                importerResult = getErroneousImportedFile(file.getName(), errorMessage);
            }
        } else {
            try {
                importerResult = doImport(password, new FileInputStream(file), file.getName(), account);

                if (properties.getDeleteAfterImport()) {
                    deleteFile(file);
                }
            } catch (FileNotFoundException e) {
                final String errorMessage = String.format("[%s] File '%s' has not been imported because it does not exist or it is a directory", this.getClass().getSimpleName(), file.getName());

                log.error(errorMessage);

                importerResult = getErroneousImportedFile(file.getName(), errorMessage);
            }
        }

        return importerResult;
    }

    @Override
    public ImporterResult doImport(final char[] password, final InputStream is, final String fileName, final Account account) {
        ImporterResult importerResult;

        try {
            // In case multiple importers are found for the same file (InputStream)
            is.reset();
            final List<T> movements = loadElements(is, fileName, account);

            log.debug("[{}] Import file '{}' was loaded successfully", this.getClass().getSimpleName(), fileName);

            persistElements(password, account.getId(), movements);

            log.debug("[{}] Movements of import file '{}' were persisted successfully", this.getClass().getSimpleName(), fileName);

            importerResult = getSuccessfulImportedFile(fileName);
        } catch (CsvCryptoIOException e) {
            final String errorMessage = String.format("[%s] There was an error persisting movements of import file '%s'. Cause: %s", this.getClass().getSimpleName(), fileName, e.getMessage());

            log.error(errorMessage, e);

            importerResult = getErroneousImportedFile(fileName, errorMessage);
        } catch (IOException e) {
            final String errorMessage = String.format("[%s] There was an error loading movements from import file '%s'. Cause: %s", this.getClass().getSimpleName(), fileName, e.getMessage());

            log.error(errorMessage, e);

            importerResult = getErroneousImportedFile(fileName, errorMessage);
        }

        return importerResult;
    }

    protected List<T> loadElements(final File file, final Account account) throws IOException {
        return loadElements(new FileInputStream(file), file.getName(), account);
    }

    protected abstract List<T> loadElements(final InputStream is, final String fileName, final Account account) throws IOException;

    protected abstract void persistElements(final char[] password, final UUID accountId, final List<T> elements) throws IOException;

    private void deleteFile(final File file) {
        boolean deleted = file.delete();

        if (deleted) {
            log.debug("Deleted import file '{}'", file.getName());
        } else {
            log.debug("Import file '{}' was imported successfully but it could not be deleted, reimport it or delete it manually", file.getName());
        }
    }

    private ImporterResult getSuccessfulImportedFile(final String fileName) {
        return ImporterResult.builder()
                .fileName(fileName)
                .build();
    }

    private ImporterResult getErroneousImportedFile(final String fileName, final String errorCause) {
        return ImporterResult.builder()
                .fileName(fileName)
                .errorCause(errorCause)
                .build();
    }
}
