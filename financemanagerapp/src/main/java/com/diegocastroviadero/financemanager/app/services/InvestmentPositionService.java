package com.diegocastroviadero.financemanager.app.services;

import com.diegocastroviadero.financemanager.app.model.InvestmentPosition;
import com.diegocastroviadero.financemanager.cryptoutils.CsvSerializationUtils;
import com.diegocastroviadero.financemanager.cryptoutils.exception.CsvCryptoIOException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.math.BigDecimal;
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

@Service
public class InvestmentPositionService extends AbstractPersistenceService {
    private static final String ACCOUNT_INVESTMENT_POSITIONS_FILENAME_PATTERN = "investments_%s_%s.ecsv";
    private static final String ACCOUNT_INVESTMENT_POSITIONS_FILENAME_REGEX_TEMPLATE = "investments_%s_[0-9]{6}.ecsv";
    private static final String ACCOUNT_INVESTMENT_POSITIONS_YEARMONTH_EXTRACTOR_PATTERN_TEMPLATE = "investments_%s_([0-9]{6}).ecsv";

    private final UserConfigService userConfigService;

    public InvestmentPositionService(final PersistencePropertiesService propertiesService, final CacheService cacheService, final UserConfigService userConfigService) {
        super(propertiesService, cacheService);
        this.userConfigService = userConfigService;
    }

    public List<InvestmentPosition> getInvestmentPositionsByAccountAndMonth(final char[] password, final UUID accountId, final YearMonth yearMonth) throws CsvCryptoIOException {
        final File file = getFile(getFilename(accountId, yearMonth));

        List<InvestmentPosition> investmentPositions;

        if (file.exists()) {
            final List<String[]> rawCsvData = loadData(password, file);

            investmentPositions = rawCsvData.stream()
                    .map(rawInvestmentPosition -> InvestmentPosition.fromStringArray(rawInvestmentPosition, userConfigService.isDemoMode()))
                    .sorted(Comparator.comparing(InvestmentPosition::getIndex))
                    .collect(Collectors.toList());

        } else {
            investmentPositions = new ArrayList<>();
        }

        return investmentPositions;
    }

    public List<InvestmentPosition> getInvestmentPositionsByAccountAndFromMonth(final char[] password, final UUID accountId, final YearMonth fromYearMonth) throws CsvCryptoIOException {
        List<YearMonth> yearMonths = getYearMonthRange(accountId);

        if (null != fromYearMonth) {
            yearMonths = yearMonths.stream()
                    .filter(ym -> ym.isAfter(fromYearMonth))
                    .collect(Collectors.toList());
        }

        final List<InvestmentPosition> investmentPositions = new ArrayList<>();

        for (YearMonth yearMonth : yearMonths) {
            investmentPositions.addAll(getInvestmentPositionsByAccountAndMonth(password, accountId, yearMonth));
        }

        return investmentPositions.stream()
                .sorted(Comparator.comparing(InvestmentPosition::getIndex))
                .collect(Collectors.toList());
    }

    public InvestmentPosition getLastInvestmentPositionByAccount(final char[] password, final UUID accountId) throws CsvCryptoIOException {
        InvestmentPosition lastInvestmentPosition = null;

        final Optional<YearMonth> lastYearMonth = getYearMonthRange(accountId)
                .stream().max(Comparator.naturalOrder());

        if (lastYearMonth.isPresent()) {
            final File file = getFile(getFilename(accountId, lastYearMonth.get()));


            if (file.exists()) {
                final List<String[]> rawCsvData = loadData(password, file);

                lastInvestmentPosition = rawCsvData.stream()
                        .map(rawInvestmentPosition -> InvestmentPosition.fromStringArray(rawInvestmentPosition, userConfigService.isDemoMode()))
                        .sorted(Comparator.comparing(InvestmentPosition::getIndex))
                        .reduce((first, second) -> second)
                        .orElse(null);
            }
        }

        return lastInvestmentPosition;
    }

    public void persistInvestmentPositions(final char[] password, final UUID accountId, final List<InvestmentPosition> investmentPositions) throws CsvCryptoIOException {
        final Map<YearMonth, List<InvestmentPosition>> accountMovementsByYearMonth = groupAccountInvestmentPositionsByYearMonth(investmentPositions);

        final AtomicLong indexSeq = new AtomicLong(-1L);

        for (Entry<YearMonth, List<InvestmentPosition>> accountInvestmentPositionsEntries : accountMovementsByYearMonth.entrySet()) {
            final YearMonth yearMonth = accountInvestmentPositionsEntries.getKey();
            final List<InvestmentPosition> accountYearMonthInvestmentPositions = accountInvestmentPositionsEntries.getValue();

            if (-1L == indexSeq.get()) {
                indexSeq.set(getStartIndex(password, accountId, yearMonth));
            }

            final File file = getFile(getFilename(accountId, yearMonth));

            indexInvestmentPositionsAndSave(password, accountYearMonthInvestmentPositions, indexSeq, file);
        }

        final Optional<YearMonth> lastMonth = accountMovementsByYearMonth.keySet().stream()
                .max(Comparator.naturalOrder());

        if (lastMonth.isPresent()) {
            reindexSuccessiveMovements(password, accountId, lastMonth.get(), indexSeq);
        }
    }

    private Map<YearMonth, List<InvestmentPosition>> groupAccountInvestmentPositionsByYearMonth(final List<InvestmentPosition> investmentPositions) {
        return investmentPositions.stream()
                .collect(Collectors.groupingBy(m -> YearMonth.from(m.getDate()), TreeMap::new, Collectors.toList()));
    }

    private String getFilename(final UUID accountId, final YearMonth yearMonth) {
        return String.format(ACCOUNT_INVESTMENT_POSITIONS_FILENAME_PATTERN, accountId, yearMonth.format(DateTimeFormatter.ofPattern("yyyyMM")));
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
            final List<InvestmentPosition> accountYearMonthMovements = getInvestmentPositionsByAccountAndMonth(password, accountId, nextMonth);

            indexInvestmentPositionsAndSave(password, accountYearMonthMovements, indexSeq, nextMonthFile);

            nextMonth = nextMonth.plusMonths(1L);
        }
    }

    private void indexInvestmentPositionsAndSave(final char[] password, final List<InvestmentPosition> investmentPositions, final AtomicLong indexSeq, final File file) throws CsvCryptoIOException {
        investmentPositions.forEach(investmentPosition -> investmentPosition.setIndex(indexSeq.incrementAndGet()));

        final List<String[]> rawMovements = investmentPositions.stream()
                .sorted(Comparator
                        .comparing(InvestmentPosition::getIndex))
                .map(InvestmentPosition::toStringArray)
                .collect(Collectors.toList());

        persistData(rawMovements, password, file);
    }

    public Map<YearMonth, BigDecimal> getInvestmentPositionsBalanceByMonth(final BigDecimal initialBalance, final List<InvestmentPosition> investmentPositions) {
        return investmentPositions.stream()
                .collect(Collectors.groupingBy(InvestmentPosition::getDateYearMonth, TreeMap::new, Collectors.toList()))
                .entrySet().stream()
                .map(e -> Map.entry(e.getKey(), e.getValue().stream()
                        .reduce((f, s) -> s)
                        .map(InvestmentPosition::getValue)
                        .orElse(BigDecimal.ZERO)))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    private String getAccountInvestmentPositionsFilenameRegex(final UUID accountId) {
        return String.format(ACCOUNT_INVESTMENT_POSITIONS_FILENAME_REGEX_TEMPLATE, accountId);
    }

    private Pattern getAccountInvestmentPositionsYearMonthExtractorPattern(final UUID accountId) {
        return Pattern.compile(String.format(ACCOUNT_INVESTMENT_POSITIONS_YEARMONTH_EXTRACTOR_PATTERN_TEMPLATE, accountId));
    }

    private List<YearMonth> getYearMonthRange(final UUID accountId) {
        return super.getYearMonthRange(getAccountInvestmentPositionsFilenameRegex(accountId), getAccountInvestmentPositionsYearMonthExtractorPattern(accountId));
    }
}
