package com.diegocastroviadero.financemanager.app.services.kb;

import com.diegocastroviadero.financemanager.app.configuration.ImportProperties;
import com.diegocastroviadero.financemanager.app.model.ImportScope;
import com.diegocastroviadero.financemanager.app.services.AccountService;
import com.diegocastroviadero.financemanager.app.services.MovementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KbAccountMovementImporter extends AbstractKbImporter {
    private static final String FILENAME_REGEX = "movimientos_kb_(\\d{4} \\d{4} \\d{2} \\d{10}).xls";

    public KbAccountMovementImporter(final ImportProperties properties, final AccountService accountService, final MovementService movementService) {
        super(properties, accountService, movementService);
    }

    @Override
    public ImportScope getImportScope() {
        return ImportScope.ACCOUNT;
    }

    @Override
    protected String getFilenameRegex() {
        return FILENAME_REGEX;
    }
}
