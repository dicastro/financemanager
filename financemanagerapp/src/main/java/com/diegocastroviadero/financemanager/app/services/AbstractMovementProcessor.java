package com.diegocastroviadero.financemanager.app.services;

import com.diegocastroviadero.financemanager.app.model.Movement;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public abstract class AbstractMovementProcessor {

    protected BigDecimal getMovementsBalance(BigDecimal initialBalance, List<Movement> movements) {
        final BigDecimal balance;

        if (null != movements) {
            balance = movements.stream()
                    .map(Movement::getQuantity)
                    .reduce(initialBalance, BigDecimal::add);
        } else {
            balance = initialBalance;
        }

        return balance;
    }

    protected Map<YearMonth, BigDecimal> getMovementsBalanceByMonth(final BigDecimal initialBalance, final List<Movement> movements) {
        final Map<YearMonth, BigDecimal> balancesByMonth = new TreeMap<>();

        if (!movements.isEmpty()) {
            BigDecimal currentBalance = initialBalance;
            YearMonth currentYearMonth = null;

            for (Movement movement : movements) {
                final YearMonth movementYearMonth = movement.getDateYearMonth();

                if (null == currentYearMonth) {
                    currentYearMonth = movementYearMonth;
                }

                if (!movementYearMonth.equals(currentYearMonth)) {
                    balancesByMonth.put(currentYearMonth, currentBalance);

                    currentBalance = currentBalance.add(movement.getQuantity());
                    currentYearMonth = movementYearMonth;
                } else {
                    currentBalance = currentBalance.add(movement.getQuantity());
                }
            }

            balancesByMonth.put(currentYearMonth, currentBalance);
        }

        return balancesByMonth;
    }
}
