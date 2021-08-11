package com.diegocastroviadero.financemanager.app.services;

import com.diegocastroviadero.financemanager.app.configuration.PersistenceProperties;
import com.diegocastroviadero.financemanager.cryptoutils.CsvCryptoUtils;
import com.diegocastroviadero.financemanager.cryptoutils.CsvUtils;
import com.diegocastroviadero.financemanager.cryptoutils.exception.CsvCryptoIOException;
import com.diegocastroviadero.financemanager.cryptoutils.exception.CsvIOException;
import com.diegocastroviadero.financemanager.cryptoutils.exception.RuntimeCsvCryptoIOException;
import com.diegocastroviadero.financemanager.cryptoutils.exception.RuntimeCsvIOException;

import java.io.File;
import java.io.FilenameFilter;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractPersistenceService {
    protected final PersistenceProperties properties;
    protected final CacheService cacheService;

    public AbstractPersistenceService(final PersistenceProperties properties, final CacheService cacheService) {
        this.properties = properties;
        this.cacheService = cacheService;
    }

    protected File getFile(final String filename) {
        return properties.getDbfiles().getBasePath().resolve(filename).toFile();
    }

    protected List<YearMonth> getYearMonthRange(final String fileFilterRegex, final Pattern yearMonthExtractorRegex) {
        FilenameFilter accountMovementsFileFilter = (dir, name) -> name.matches(fileFilterRegex);

        final File[] files = properties.getDbfiles().getBasePath().toFile().listFiles(accountMovementsFileFilter);

        List<YearMonth> yearMonths = Collections.emptyList();

        if (null != files) {
            yearMonths = Stream.of(files)
                    .map(File::getName)
                    .map(filename -> yearMonthExtractorRegex.matcher(filename).results()
                            .map(m -> m.group(1))
                            .findFirst()
                            .orElse(null))
                    .filter(Objects::nonNull)
                    .map(rawYearMonth -> YearMonth.parse(rawYearMonth, DateTimeFormatter.ofPattern("yyyyMM")))
                    .distinct()
                    .sorted(Comparator.reverseOrder())
                    .collect(Collectors.toList());
        }

        return yearMonths;
    }

    protected List<String[]> loadData(final File file) throws CsvIOException {
        final String cacheKey = getCacheKey(file);

        try {
            return cacheService.putIfAbsent(cacheKey, () -> {
                try {
                    return CsvUtils.readFromCsvFile(file);
                } catch (CsvIOException e) {
                    throw e.toUncheckedException();
                }
            });
        } catch (RuntimeCsvIOException e) {
            throw e.toCheckedException();
        }
    }

    protected List<String[]> loadData(final char[] password, final File file) throws CsvCryptoIOException {
        final String cacheKey = getCacheKey(file);

        try {
            return cacheService.putIfAbsent(cacheKey, () -> {
                try {
                    return CsvCryptoUtils.decryptFromCsvFile(password, file);
                } catch (CsvCryptoIOException e) {
                    throw e.toUncheckedException();
                }
            });
        } catch (RuntimeCsvCryptoIOException e) {
            throw e.toCheckedException();
        }
    }

    protected void persistData(final List<String[]> data, final File file) throws CsvIOException {
        CsvUtils.persistToCsvFile(data, file);

        cacheService.put(getCacheKey(file), data);
    }

    protected void persistData(final List<String[]> data, final char[] password, final File file) throws CsvCryptoIOException {
        CsvCryptoUtils.encryptToCsvFile(data, password, file);

        cacheService.put(getCacheKey(file), data);
    }

    protected String getCacheKey(final File file) {
        return file.getName();
    }
}
