package com.diegocastroviadero.financemanager.app.services;

import com.diegocastroviadero.financemanager.app.model.AccountPurpose;
import com.diegocastroviadero.financemanager.app.model.Bank;
import com.diegocastroviadero.financemanager.app.model.Movement;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

public interface MovementProcessor {
    boolean applies(final Bank bank, final AccountPurpose accountPurpose);

    BigDecimal getMovementsBalance(final BigDecimal initialBalance, final List<Movement> movements);

    Map<YearMonth, BigDecimal> getMovementsBalanceByMonth(final BigDecimal initialBalance, final List<Movement> movements);
}
