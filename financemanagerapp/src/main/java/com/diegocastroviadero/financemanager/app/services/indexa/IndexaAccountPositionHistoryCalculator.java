package com.diegocastroviadero.financemanager.app.services.indexa;

import com.diegocastroviadero.financemanager.app.model.*;
import com.diegocastroviadero.financemanager.app.services.*;
import com.diegocastroviadero.financemanager.cryptoutils.exception.CsvCryptoIOException;
import lombok.AllArgsConstructor;
import org.springframework.core.Ordered;
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
@Order(value = Ordered.LOWEST_PRECEDENCE - 100)
public class IndexaAccountPositionHistoryCalculator implements AccountPositionHistoryCalculator {

    private final MovementProcessorFactory movementProcessorFactory;
    private final MovementService movementService;
    private final InvestmentPositionService investmentPositionService;

    @Override
    public boolean applies(final AccountPosition accountPosition) {
        return Bank.IC == accountPosition.getBank();
    }

    @Override
    public AccountPositionHistory getAccountPositionHistory(final char[] password, final AccountPosition accountPosition) throws CsvCryptoIOException {
        final List<Movement> accountMovements = movementService.getMovementsByAccountAndFromMonth(password, accountPosition.getAccountId(), accountPosition.getMonthFrom());

        final MovementProcessor movementProcessor = movementProcessorFactory.getMovementProcessor(accountPosition.getBank(), accountPosition.getType());

        final Map<YearMonth, BigDecimal> balancesByMonth = movementProcessor.getMovementsBalanceByMonth(accountPosition.getInitialBalance(), accountMovements);
        final BigDecimal[] inverted = balancesByMonth.values().toArray(BigDecimal[]::new);

        final String[] labels = balancesByMonth.keySet().stream()
                .map(m -> m.format(DateTimeFormatter.ofPattern("MM/yyyy")))
                .collect(Collectors.toList())
                .toArray(String[]::new);

        final List<InvestmentPosition> investmentPositions = investmentPositionService.getInvestmentPositionsByAccountAndFromMonth(password, accountPosition.getAccountId(), accountPosition.getMonthFrom());

        final Map<YearMonth, BigDecimal> investmentsByMonth = investmentPositionService.getInvestmentPositionsBalanceByMonth(accountPosition.getInitialBalance(), investmentPositions);
        final BigDecimal[] values = investmentsByMonth.values().toArray(BigDecimal[]::new);

        return AccountPositionHistory.builder()
                .accountId(accountPosition.getAccountId())
                .serie(AccountPositionHistorySeries.builder()
                        .alias("Inverted")
                        .values(inverted)
                        .build())
                .serie(AccountPositionHistorySeries.builder()
                        .alias("Value")
                        .values(values)
                        .build())
                .labels(labels)
                .build();
    }
}
