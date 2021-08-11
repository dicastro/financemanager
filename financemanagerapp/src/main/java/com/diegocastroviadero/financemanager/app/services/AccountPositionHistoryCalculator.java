package com.diegocastroviadero.financemanager.app.services;

import com.diegocastroviadero.financemanager.app.model.AccountPosition;
import com.diegocastroviadero.financemanager.app.model.AccountPositionHistory;
import com.diegocastroviadero.financemanager.cryptoutils.exception.CsvCryptoIOException;

public interface AccountPositionHistoryCalculator {
    boolean applies(final AccountPosition accountPosition);

    AccountPositionHistory getAccountPositionHistory(final char[] password, final AccountPosition accountPosition) throws CsvCryptoIOException;
}
