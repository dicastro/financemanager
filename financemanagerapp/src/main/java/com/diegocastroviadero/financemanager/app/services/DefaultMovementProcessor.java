package com.diegocastroviadero.financemanager.app.services;

import com.diegocastroviadero.financemanager.app.model.AccountPurpose;
import com.diegocastroviadero.financemanager.app.model.Bank;
import com.diegocastroviadero.financemanager.app.model.Movement;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@Order // By default is Ordered.LOWEST_PRECEDENCE
public class DefaultMovementProcessor extends AbstractMovementProcessor implements MovementProcessor {
    @Override
    public boolean applies(final Bank bank, final AccountPurpose accountPurpose) {
        return Boolean.TRUE;
    }

    @Override
    public BigDecimal getMovementsBalance(final BigDecimal initialBalance, final List<Movement> movements) {
        return super.getMovementsBalance(initialBalance, movements);
    }

    @Override
    public Map<YearMonth, BigDecimal> getMovementsBalanceByMonth(final BigDecimal initialBalance, final List<Movement> movements) {
        return super.getMovementsBalanceByMonth(initialBalance, movements);
    }
}
