package com.diegocastroviadero.financemanager.app.services;

import com.diegocastroviadero.financemanager.app.model.Account;
import com.diegocastroviadero.financemanager.app.model.AccountPosition;
import com.diegocastroviadero.financemanager.app.model.Movement;
import com.diegocastroviadero.financemanager.cryptoutils.exception.CsvCryptoIOException;
import lombok.AllArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;

@Service
@AllArgsConstructor
@Order // By default is Ordered.LOWEST_PRECEDENCE
public class DefaultAccountPositionCalculator implements AccountPositionCalculator {

    private final MovementProcessorFactory movementProcessorFactory;
    private final MovementService movementService;

    @Override
    public boolean applies(final Account account) {
        return Boolean.TRUE;
    }

    @Override
    public AccountPosition getAccountPosition(final char[] password, final Account account) throws CsvCryptoIOException {
        final List<Movement> accountMovements = movementService.getMovementsByAccountAndFromMonth(password, account.getId(), account.getBalanceDateYearMonth());

        final MovementProcessor movementProcessor = movementProcessorFactory.getMovementProcessor(account.getBank(), account.getPurpose());

        final BigDecimal balance = movementProcessor.getMovementsBalance(account.getBalance(), accountMovements);

        final LocalDate balanceDate = accountMovements.stream()
                .map(Movement::getDate)
                .max(Comparator.naturalOrder())
                .map(d -> YearMonth.from(d).atEndOfMonth())
                .orElse(null);

        return AccountPosition.builder()
                .bank(account.getBank())
                .accountId(account.getId())
                .alias(account.getAlias())
                .scope(account.getScope())
                .type(account.getPurpose())
                .balance(balance)
                .balanceDate(balanceDate)
                .monthFrom(account.getBalanceDateYearMonth())
                .initialBalance(account.getBalance())
                .build();
    }
}
