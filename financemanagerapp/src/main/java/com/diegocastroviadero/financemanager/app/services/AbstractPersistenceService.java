package com.diegocastroviadero.financemanager.app.services;

import com.diegocastroviadero.financemanager.app.configuration.PersistenceProperties;

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

    public AbstractPersistenceService(final PersistenceProperties properties) {
        this.properties = properties;
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
}
