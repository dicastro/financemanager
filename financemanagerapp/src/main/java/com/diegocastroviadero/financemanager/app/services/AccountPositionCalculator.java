package com.diegocastroviadero.financemanager.app.services;

import com.diegocastroviadero.financemanager.app.model.Account;
import com.diegocastroviadero.financemanager.app.model.AccountPosition;
import com.diegocastroviadero.financemanager.cryptoutils.exception.CsvCryptoIOException;

public interface AccountPositionCalculator {
    boolean applies(final Account account);

    AccountPosition getAccountPosition(final char[] password, final Account account) throws CsvCryptoIOException;
}
