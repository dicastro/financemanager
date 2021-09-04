package com.diegocastroviadero.financemanager.app.services;

import com.diegocastroviadero.financemanager.app.model.Account;
import com.diegocastroviadero.financemanager.app.model.ImporterResult;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@AllArgsConstructor
@Service
public class ImportService {
    private final List<Importer> importers;

    public String extractAccountNumberFromFilename(final String filename) {
        final Pattern p = Pattern.compile("(?:import_|movimientos_)?(?:[a-z]+_)?((?:ES)?[0-9 ]+)(?:_[0-9]{6})?(?:\\.csv|\\.xls)");
        final Matcher m = p.matcher(filename);

        String accountNumber = null;

        if (m.matches()) {
            accountNumber = m.group(1);
        }

        return accountNumber;
    }

    public List<ImporterResult> importFile(final char[] password, final InputStream fis, final String fileName, final Account account) {
        final List<ImporterResult> importerResults = new ArrayList<>();

        for (Importer importer : importers) {
            if (importer.applies(account.getBank(), account.getPurpose())) {
                importerResults.add(importer.doImport(password, fis, fileName, account));
            }
        }

        return importerResults;
    }
}
