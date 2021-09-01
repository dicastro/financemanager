package com.diegocastroviadero.financemanager.app.services;

import com.diegocastroviadero.financemanager.app.model.Movement;
import com.diegocastroviadero.financemanager.app.services.events.AccountDeletedEvent;
import com.diegocastroviadero.financemanager.cryptoutils.CsvSerializationUtils;
import com.diegocastroviadero.financemanager.cryptoutils.exception.CsvCryptoIOException;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class MovementService extends AbstractPersistenceService implements ApplicationListener<AccountDeletedEvent> {
    private static final String ACCOUNT_MOVEMENTS_BY_ACCOUNT_FILENAME_PREFIX = "movements_%s";
    private static final String ACCOUNT_MOVEMENTS_FILENAME_PATTERN = ACCOUNT_MOVEMENTS_BY_ACCOUNT_FILENAME_PREFIX + "_%s.ecsv";
    private static final String ACCOUNT_MOVEMENTS_FILENAME_REGEX_TEMPLATE = ACCOUNT_MOVEMENTS_BY_ACCOUNT_FILENAME_PREFIX + "_[0-9]{6}.ecsv";
    private static final String ACCOUNT_MOVEMENTS_YEARMONTH_EXTRACTOR_PATTERN_TEMPLATE = ACCOUNT_MOVEMENTS_BY_ACCOUNT_FILENAME_PREFIX + "_([0-9]{6}).ecsv";

    private final UserConfigService userConfigService;

    public MovementService(final PersistencePropertiesService persistencePropertiesService, final CacheService cacheService, final UserConfigService userConfigService) {
        super(persistencePropertiesService, cacheService);
        this.userConfigService = userConfigService;
    }

    public List<Movement> getMovementsByAccountAndMonth(final char[] password, final UUID accountId, final YearMonth yearMonth) throws CsvCryptoIOException {
        final File file = getFile(getFilename(accountId, yearMonth));

        List<Movement> movements;

        if (file.exists()) {
            final List<String[]> rawCsvData = loadData(password, file);

            movements = rawCsvData.stream()
                    .map(rawMovement -> Movement.fromStringArray(rawMovement, userConfigService.isDemoMode()))
                    .sorted(Comparator.comparing(Movement::getIndex))
                    .collect(Collectors.toList());

        } else {
            movements = new ArrayList<>();
        }

        return movements;
    }

    public List<Movement> getMovementsByAccountAndFromMonth(final char[] password, final UUID accountId, final YearMonth fromYearMonth) throws CsvCryptoIOException {
        List<YearMonth> yearMonths = getYearMonthRange(getAccountMovementsFilenameRegex(accountId), getAccountMovementsYearMonthExtractorPattern(accountId));

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

    public List<YearMonth> getYearMonthRange(final UUID accountId) {
        return super.getYearMonthRange(getAccountMovementsFilenameRegex(accountId), getAccountMovementsYearMonthExtractorPattern(accountId));
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
            final List<String[]> rawCsvData = loadData(password, previousMonthFile);

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

        persistData(rawMovements, password, file);
    }

    private String getAccountMovementsFilenameRegex(final UUID accountId) {
        return String.format(ACCOUNT_MOVEMENTS_FILENAME_REGEX_TEMPLATE, accountId);
    }

    private Pattern getAccountMovementsYearMonthExtractorPattern(final UUID accountId) {
        return Pattern.compile(String.format(ACCOUNT_MOVEMENTS_YEARMONTH_EXTRACTOR_PATTERN_TEMPLATE, accountId));
    }

    @Override
    public void onApplicationEvent(final AccountDeletedEvent event) {
        final File[] filesToDelete = propertiesService.listFiles((dir, filename) -> filename.startsWith(String.format(ACCOUNT_MOVEMENTS_BY_ACCOUNT_FILENAME_PREFIX, event.getAccount().getId())));

        if (null != filesToDelete && filesToDelete.length > 0) {
            Stream.of(filesToDelete).forEach(File::delete);
        }
    }
}
