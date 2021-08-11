package com.diegocastroviadero.financemanager.app.services;

import com.diegocastroviadero.financemanager.app.configuration.PersistenceProperties;
import com.diegocastroviadero.financemanager.app.model.Movement;
import com.diegocastroviadero.financemanager.cryptoutils.CsvCryptoUtils;
import com.diegocastroviadero.financemanager.cryptoutils.CsvSerializationUtils;
import com.diegocastroviadero.financemanager.cryptoutils.exception.CsvCryptoIOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class MovementService extends AbstractPersistenceService {
    private static final String ACCOUNT_MOVEMENTS_FILENAME_PATTERN = "movements_%s_%s.ecsv";
    private static final String ACCOUNT_MOVEMENTS_FILENAME_REGEX = "movements_[a-f0-9-]+_[0-9]{6}.ecsv";
    private static final Pattern YEARMONTH_EXTRACTOR_PATTERN = Pattern.compile("movements_[a-f0-9-]+_([0-9]{6}).ecsv");

    private final MovementProcessorFactory movementProcessorFactory;

    @Value("${financemanagerapp.demo-mode:true}")
    private Boolean demoMode;

    public MovementService(final PersistenceProperties persistenceProperties, final MovementProcessorFactory movementProcessorFactory) {
        super(persistenceProperties);
        this.movementProcessorFactory = movementProcessorFactory;
    }

    public List<Movement> getMovementsByAccountAndMonth(final char[] password, final UUID accountId, final YearMonth yearMonth) throws CsvCryptoIOException {
        final File file = getFile(getFilename(accountId, yearMonth));

        List<Movement> movements;

        if (file.exists()) {
            final List<String[]> rawCsvData = CsvCryptoUtils.decryptFromCsvFile(password, file);

            movements = rawCsvData.stream()
                    .map(rawMovement -> Movement.fromStringArray(rawMovement, demoMode))
                    .sorted(Comparator.comparing(Movement::getIndex))
                    .collect(Collectors.toList());

        } else {
            movements = new ArrayList<>();
        }

        return movements;
    }

    public List<Movement> getMovementsByAccountAndFromMonth(final char[] password, final UUID accountId, final YearMonth fromYearMonth) throws CsvCryptoIOException {
        List<YearMonth> yearMonths = getYearMonthRange(ACCOUNT_MOVEMENTS_FILENAME_REGEX, YEARMONTH_EXTRACTOR_PATTERN);

        if (null != fromYearMonth) {
            yearMonths = yearMonths.stream()
                    .filter(ym -> ym.isAfter(fromYearMonth))
                    .collect(Collectors.toList());
        }

        final List<Movement> movements = new ArrayList<>();

        for (YearMonth yearMonth : yearMonths) {
            movements.addAll(getMovementsByAccountAndMonth(password, accountId, yearMonth));
        }

        return movements.stream()
                .sorted(Comparator.comparing(Movement::getIndex))
                .collect(Collectors.toList());
    }

    public void persistMovements(final char[] password, final UUID accountId, final List<Movement> accountMovements) throws CsvCryptoIOException {
        final Map<YearMonth, List<Movement>> accountMovementsByYearMonth = groupAccountMovementsByYearMonth(accountMovements);

        final AtomicLong indexSeq = new AtomicLong(-1L);

        for (Entry<YearMonth, List<Movement>> accountMovementsEntries : accountMovementsByYearMonth.entrySet()) {
            final YearMonth yearMonth = accountMovementsEntries.getKey();
            final List<Movement> accountYearMonthMovements = accountMovementsEntries.getValue();

            if (-1 == indexSeq.get()) {
                indexSeq.set(getStartIndex(password, accountId, yearMonth));
            }

            final File file = getFile(getFilename(accountId, yearMonth));

            indexMovementsAndSave(password, accountYearMonthMovements, indexSeq, file);
        }

        final Optional<YearMonth> lastMonth = accountMovementsByYearMonth.keySet().stream()
                .max(Comparator.naturalOrder());

        if (lastMonth.isPresent()) {
            reindexSuccessiveMovements(password, accountId, lastMonth.get(), indexSeq);
        }
    }

    public List<YearMonth> getYearMonthRange() {
        return super.getYearMonthRange(ACCOUNT_MOVEMENTS_FILENAME_REGEX, YEARMONTH_EXTRACTOR_PATTERN);
    }

    private Map<YearMonth, List<Movement>> groupAccountMovementsByYearMonth(final List<Movement> accountMovements) {
        return accountMovements.stream()
                .collect(Collectors.groupingBy(m -> YearMonth.from(m.getDate()), TreeMap::new, Collectors.toList()));
    }

    private String getFilename(final UUID accountId, final YearMonth yearMonth) {
        return String.format(ACCOUNT_MOVEMENTS_FILENAME_PATTERN, accountId, yearMonth.format(DateTimeFormatter.ofPattern("yyyyMM")));
    }

    private Long getStartIndex(final char[] password, final UUID accountId, final YearMonth yearMonth) throws CsvCryptoIOException {
        final File previousMonthFile = getFile(getFilename(accountId, yearMonth.minusMonths(1L)));

        final long startIndex;

        if (previousMonthFile.exists()) {
            final List<String[]> rawCsvData = CsvCryptoUtils.decryptFromCsvFile(password, previousMonthFile);

            startIndex = rawCsvData.stream()
                    .map(row -> row[0])
                    .mapToLong(CsvSerializationUtils::parseLongFromCsv)
                    .max()
                    .orElse(0L);
        } else {
            startIndex = 0L;
        }

        return startIndex;
    }

    private void reindexSuccessiveMovements(final char[] password, final UUID accountId, final YearMonth startMonth, final AtomicLong indexSeq) throws CsvCryptoIOException {
        YearMonth nextMonth = startMonth.plusMonths(1L);
        File nextMonthFile;

        while ((nextMonthFile = getFile(getFilename(accountId, nextMonth))).exists()) {
            final List<Movement> accountYearMonthMovements = getMovementsByAccountAndMonth(password, accountId, nextMonth);

            indexMovementsAndSave(password, accountYearMonthMovements, indexSeq, nextMonthFile);

            nextMonth = nextMonth.plusMonths(1L);
        }
    }

    private void indexMovementsAndSave(final char[] password, final List<Movement> movements, final AtomicLong indexSeq, final File file) throws CsvCryptoIOException {
        movements.forEach(movement -> movement.setIndex(indexSeq.incrementAndGet()));

        final List<String[]> rawMovements = movements.stream()
                .sorted(Comparator
                        .comparing(Movement::getIndex))
                .map(Movement::toStringArray)
                .collect(Collectors.toList());

        CsvCryptoUtils.encryptToCsvFile(rawMovements, password, file);
    }
}
