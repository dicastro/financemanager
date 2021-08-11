package com.diegocastroviadero.financemanager.app.services;

import com.diegocastroviadero.financemanager.app.model.AccountPosition;
import com.diegocastroviadero.financemanager.app.model.AccountPositionHistory;
import com.diegocastroviadero.financemanager.app.model.AccountPositionHistorySeries;
import com.diegocastroviadero.financemanager.app.model.Movement;
import com.diegocastroviadero.financemanager.cryptoutils.exception.CsvCryptoIOException;
import lombok.AllArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Order // By default is Ordered.LOWEST_PRECEDENCE
public class DefaultAccountPositionHistoryCalculator implements AccountPositionHistoryCalculator {

    private final MovementProcessorFactory movementProcessorFactory;
    private final MovementService movementService;

    @Override
    public boolean applies(final AccountPosition accountPosition) {
        return Boolean.TRUE;
    }

    @Override
    public AccountPositionHistory getAccountPositionHistory(final char[] password, final AccountPosition accountPosition) throws CsvCryptoIOException {
        final List<Movement> accountMovements = movementService.getMovementsByAccountAndFromMonth(password, accountPosition.getAccountId(), accountPosition.getMonthFrom());

        final MovementProcessor movementProcessor = movementProcessorFactory.getMovementProcessor(accountPosition.getBank(), accountPosition.getType());

        final Map<YearMonth, BigDecimal> balancesByMonth = movementProcessor.getMovementsBalanceByMonth(accountPosition.getInitialBalance(), accountMovements);

        final BigDecimal[] balances = balancesByMonth.values().toArray(BigDecimal[]::new);
        final String[] labels = balancesByMonth.keySet().stream()
                .map(m -> m.format(DateTimeFormatter.ofPattern("MM/yyyy")))
                .collect(Collectors.toList())
                .toArray(String[]::new);

        return AccountPositionHistory.builder()
                .accountId(accountPosition.getAccountId())
                .serie(AccountPositionHistorySeries.builder()
                        .alias(accountPosition.getAlias())
                        .values(balances)
                        .build())
                .labels(labels)
                .build();
    }
}
