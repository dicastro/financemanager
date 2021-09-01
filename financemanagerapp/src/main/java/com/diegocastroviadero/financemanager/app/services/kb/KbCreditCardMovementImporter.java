package com.diegocastroviadero.financemanager.app.services.kb;

import com.diegocastroviadero.financemanager.app.configuration.ImportProperties;
import com.diegocastroviadero.financemanager.app.model.AccountPurpose;
import com.diegocastroviadero.financemanager.app.model.Bank;
import com.diegocastroviadero.financemanager.app.model.ImportScope;
import com.diegocastroviadero.financemanager.app.services.AccountService;
import com.diegocastroviadero.financemanager.app.services.MovementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KbCreditCardMovementImporter extends AbstractKbImporter {
    private static final String FILENAME_REGEX = "movimientos_kb_(\\d{16}).xls";

    public KbCreditCardMovementImporter(final ImportProperties properties, final AccountService accountService, final MovementService movementService) {
        super(properties, accountService, movementService);
    }

    @Override
    public ImportScope getImportScope() {
        return ImportScope.CREDIT_CARD;
    }

    @Override
    protected String getFilenameRegex() {
        return FILENAME_REGEX;
    }

    @Override
    public boolean applies(final Bank bank, final AccountPurpose purpose) {
        return Bank.KB == bank
                && AccountPurpose.CREDIT == purpose;
    }
}
