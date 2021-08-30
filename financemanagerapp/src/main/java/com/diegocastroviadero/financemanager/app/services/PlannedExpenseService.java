package com.diegocastroviadero.financemanager.app.services;

import com.diegocastroviadero.financemanager.app.model.PlannedExpense;
import com.diegocastroviadero.financemanager.app.model.Scope;
import com.diegocastroviadero.financemanager.cryptoutils.exception.CsvIOException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.Month;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PlannedExpenseService extends AbstractPersistenceService {
    private static final String PLANNED_EXPENSES_FILENAME = "planned_expenses.csv";

    public PlannedExpenseService(final PersistencePropertiesService propertiesService, final CacheService cacheService) {
        super(propertiesService, cacheService);
    }

    public List<PlannedExpense> getAllPlannedExpenses() throws CsvIOException {
        final File file = getFile(getFilename());

        List<PlannedExpense> plannedExpenses;

        if (file.exists()) {
            final List<String[]> rawCsvData = loadData(file);

            plannedExpenses = rawCsvData.stream()
                    .map(PlannedExpense::fromStringArray)
                    .collect(Collectors.toList());

        } else {
            plannedExpenses = new ArrayList<>();
        }

        return plannedExpenses;
    }

    public List<PlannedExpense> getAllPlannedExpenses(final String filter) throws CsvIOException {
        final List<PlannedExpense> result;

        if (StringUtils.isBlank(filter)) {
            result = getAllPlannedExpenses();
        } else {
            result = getAllPlannedExpenses().stream()
                    .filter(e -> StringUtils.containsIgnoreCase(e.getConcept(), filter))
                    .collect(Collectors.toList());
        }

        return result;
    }

    public Map<Scope, List<PlannedExpense>> getPlannedExpensesByMonthGroupedByScope(final Month month) throws CsvIOException {
        return getAllPlannedExpenses().stream()
                .filter(plannedExpense -> plannedExpense.getMonth() == month)
                .collect(Collectors.groupingBy(PlannedExpense::getScope));
    }

    public PlannedExpense upsertPlannedExpense(final PlannedExpense plannedExpense) throws CsvIOException {
        final List<PlannedExpense> existingPlannedExpenses = getAllPlannedExpenses();

        boolean somethingToPersist = false;
        PlannedExpense result;

        if (null == plannedExpense.getId()) {
            // without id -> new planned expense
            result = plannedExpense.toBuilder().id(UUID.randomUUID()).build();

            existingPlannedExpenses.add(result);
            somethingToPersist = true;
        } else {
            // with id -> existing planned expense
            result = existingPlannedExpenses.stream()
                    .filter(a -> a.getId().equals(plannedExpense.getId()))
                    .findFirst()
                    .orElse(null);

            if (null == result) {
                log.warn("[upsertPlannedExpense] Received a planned expense with an id, but there is no existing one with this id (operation ignored)");
            } else {
                result.setConcept(plannedExpense.getConcept());
                result.setMonth(plannedExpense.getMonth());
                result.setQuantity(plannedExpense.getQuantity());
                result.setScope(plannedExpense.getScope());
                somethingToPersist = true;
            }
        }

        if (somethingToPersist) {
            persistPlannedExpenses(existingPlannedExpenses);
        }

        return result;
    }

    public void persistPlannedExpenses(final List<PlannedExpense> plannedExpenses) throws CsvIOException {
        final File file = getFile(getFilename());

        final List<String[]> sortedRawPlannedExpenses = plannedExpenses.stream()
                .sorted(Comparator
                        .comparing(PlannedExpense::getScope)
                        .thenComparing(PlannedExpense::getConcept)
                        .thenComparing(PlannedExpense::getMonth))
                .map(PlannedExpense::toStringArray)
                .collect(Collectors.toList());

        persistData(sortedRawPlannedExpenses, file);
    }

    private String getFilename() {
        return PLANNED_EXPENSES_FILENAME;
    }
}
