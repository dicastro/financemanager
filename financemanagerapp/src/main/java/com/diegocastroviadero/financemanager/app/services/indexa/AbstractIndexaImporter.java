package com.diegocastroviadero.financemanager.app.services.indexa;

import com.diegocastroviadero.financemanager.app.model.AccountPurpose;
import com.diegocastroviadero.financemanager.app.model.Bank;
import com.diegocastroviadero.financemanager.app.services.AbstractImporter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractIndexaImporter<T> extends AbstractImporter<T> {

    @Override
    public boolean applies(final Bank bank, final AccountPurpose purpose) {
        return Bank.IC == bank;
    }
}
