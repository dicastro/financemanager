package com.diegocastroviadero.financemanager.app.services.indexa;

import com.diegocastroviadero.financemanager.app.configuration.ImportProperties;
import com.diegocastroviadero.financemanager.app.model.Bank;
import com.diegocastroviadero.financemanager.app.services.AbstractImporter;
import com.diegocastroviadero.financemanager.app.services.AccountService;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public abstract class AbstractIndexaImporter<T> extends AbstractImporter<T> {
    private static final String FILENAME_REGEX = "movimientos_ic_(ES\\d{2} \\d{4} \\d{4} \\d{4} \\d{4} \\d{4}).csv";

    public AbstractIndexaImporter(final ImportProperties properties, final AccountService accountService) {
        super(properties, accountService);
    }

    @Override
    public boolean applies(final File file) {
        return file.getName().matches(FILENAME_REGEX);
    }

    @Override
    public Bank getBank() {
        return Bank.IC;
    }

    @Override
    public String getAccountNumber(final File file) {
        final Pattern pattern = Pattern.compile(FILENAME_REGEX);
        final Matcher matcher = pattern.matcher(file.getName());

        String id = null;

        if (matcher.matches()) {
            final String iban = matcher.group(1);

            final String[] ibanParts = iban.split(" ");

            id = String.format("%s %s %s %s%s%s", ibanParts[1], ibanParts[2], ibanParts[3].substring(0, 2), ibanParts[3].substring(2, 4), ibanParts[4], ibanParts[5]);
        }

        return id;
    }
}
