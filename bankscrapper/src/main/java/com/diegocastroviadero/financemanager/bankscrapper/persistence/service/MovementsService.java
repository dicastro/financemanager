package com.diegocastroviadero.financemanager.bankscrapper.persistence.service;

import com.diegocastroviadero.financemanager.bankscrapper.configuration.PersistenceProperties;
import com.diegocastroviadero.financemanager.bankscrapper.model.Movement;
import com.diegocastroviadero.financemanager.bankscrapper.scrapper.common.model.SecurityContext;
import com.diegocastroviadero.financemanager.cryptoutils.CsvCryptoUtils;
import com.diegocastroviadero.financemanager.cryptoutils.exception.CsvCryptoIOException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MovementsService extends AbstractPersistenceService {
    private static final String ACCOUNT_MOVEMENTS_FILENAME_PATTERN = "movements_%s_%s.ecsv";

    public MovementsService(final PersistenceProperties persistenceProperties) {
        super(persistenceProperties);
    }

    public Map<YearMonth, List<Movement>> groupAccountMovementsByYearMonth(final List<Movement> accountMovements) {
        return accountMovements.stream()
                .collect(Collectors.groupingBy((m) -> YearMonth.from(m.getDate()), Collectors.toList()));
    }

    public void persistMovements(final UUID accountId, final Map<YearMonth, List<Movement>> accountMovementsByYearMonth) throws CsvCryptoIOException {
        for (Entry<YearMonth, List<Movement>> accountMovementsEntries : accountMovementsByYearMonth.entrySet()) {
            final YearMonth yearMonth = accountMovementsEntries.getKey();
            final List<Movement> accountYearMonthMovements = accountMovementsEntries.getValue();

            final File file = getFile(getFilename(accountId, yearMonth));

            final List<String[]> rawMovements = accountYearMonthMovements.stream()
                    .sorted(Comparator
                            .comparing(Movement::getAccount)
                            .thenComparing(Movement::getDate))
                    .map(Movement::toStringArray)
                    .collect(Collectors.toList());

            CsvCryptoUtils.encryptToCsvFile(rawMovements, SecurityContext.getEncryptionPassword().toCharArray(), file);
        }
    }

    private String getFilename(final UUID accountId, final YearMonth yearMonth) {
        return String.format(ACCOUNT_MOVEMENTS_FILENAME_PATTERN, accountId, yearMonth.format(DateTimeFormatter.ofPattern("yyyyMM")));
    }
}
