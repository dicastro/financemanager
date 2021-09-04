package com.diegocastroviadero.financemanager.app.services;

import com.diegocastroviadero.financemanager.app.model.Account;
import com.diegocastroviadero.financemanager.app.model.ImporterResult;
import com.diegocastroviadero.financemanager.cryptoutils.exception.CsvCryptoIOException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Slf4j
public abstract class AbstractImporter<T> implements Importer {

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

    protected abstract List<T> loadElements(final InputStream is, final String fileName, final Account account) throws IOException;

    protected abstract void persistElements(final char[] password, final UUID accountId, final List<T> elements) throws IOException;

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
