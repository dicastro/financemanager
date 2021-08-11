package com.diegocastroviadero.financemanager.app.services.kb;

import com.diegocastroviadero.financemanager.app.model.AccountPurpose;
import com.diegocastroviadero.financemanager.app.model.Bank;
import com.diegocastroviadero.financemanager.app.model.Movement;
import com.diegocastroviadero.financemanager.app.services.AbstractMovementProcessor;
import com.diegocastroviadero.financemanager.app.services.MovementProcessor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Order(Ordered.LOWEST_PRECEDENCE - 100)
public class KbCreditMovementProcessor extends AbstractMovementProcessor implements MovementProcessor {
    @Override
    public boolean applies(final Bank bank, final AccountPurpose accountPurpose) {
        return Bank.KB == bank
                && AccountPurpose.CREDIT == accountPurpose;
    }

    @Override
    public BigDecimal getMovementsBalance(final BigDecimal initialBalance, final List<Movement> movements) {
        final List<Movement> filteredMovements = movements.stream()
                .filter(m -> !StringUtils.startsWithIgnoreCase(m.getConcept(), "PAGO CAJERO ")
                        && !StringUtils.startsWithIgnoreCase(m.getConcept(), "INGRESO EFECTIVO "))
                .collect(Collectors.toList());

        return super.getMovementsBalance(initialBalance, filteredMovements);
    }

    @Override
    public Map<YearMonth, BigDecimal> getMovementsBalanceByMonth(final BigDecimal initialBalance, final List<Movement> movements) {
        final List<Movement> filteredMovements = movements.stream()
                .filter(m -> !StringUtils.startsWithIgnoreCase(m.getConcept(), "PAGO CAJERO ")
                        && !StringUtils.startsWithIgnoreCase(m.getConcept(), "INGRESO EFECTIVO "))
                .collect(Collectors.toList());

        return super.getMovementsBalanceByMonth(initialBalance, filteredMovements);
    }
}
