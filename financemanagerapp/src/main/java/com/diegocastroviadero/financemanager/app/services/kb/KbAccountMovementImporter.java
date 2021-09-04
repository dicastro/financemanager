package com.diegocastroviadero.financemanager.app.services.kb;

import com.diegocastroviadero.financemanager.app.model.AccountPurpose;
import com.diegocastroviadero.financemanager.app.model.Bank;
import com.diegocastroviadero.financemanager.app.services.MovementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KbAccountMovementImporter extends AbstractKbImporter {

    public KbAccountMovementImporter(final MovementService movementService) {
        super(movementService);
    }

    @Override
    public boolean applies(final Bank bank, final AccountPurpose purpose) {
        return Bank.KB == bank
                && AccountPurpose.CREDIT != purpose;
    }
}
