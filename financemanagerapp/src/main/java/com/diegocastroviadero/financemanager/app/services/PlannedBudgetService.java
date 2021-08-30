package com.diegocastroviadero.financemanager.app.services;

import com.diegocastroviadero.financemanager.app.model.PlannedBudget;
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
public class PlannedBudgetService extends AbstractPersistenceService {
    private static final String PLANNED_BUDGETS_FILENAME = "planned_budgets.csv";

    public PlannedBudgetService(final PersistencePropertiesService propertiesService, final CacheService cacheService) {
        super(propertiesService, cacheService);
    }

    public List<PlannedBudget> getAllPlannedBudgets() throws CsvIOException {
        final File file = getFile(getFilename());

        List<PlannedBudget> plannedBudgets;

        if (file.exists()) {
            final List<String[]> rawCsvData = loadData(file);

            plannedBudgets = rawCsvData.stream()
                    .map(PlannedBudget::fromStringArray)
                    .collect(Collectors.toList());

        } else {
            plannedBudgets = new ArrayList<>();
        }

        return plannedBudgets;
    }

    public List<PlannedBudget> getAllPlannedBudgets(final String filter) throws CsvIOException {
        final List<PlannedBudget> result;

        if (StringUtils.isBlank(filter)) {
            result = getAllPlannedBudgets();
        } else {
            result = getAllPlannedBudgets().stream()
                    .filter(e -> StringUtils.containsIgnoreCase(e.getConcept(), filter))
                    .collect(Collectors.toList());
        }

        return result;
    }

    public Map<Scope, List<PlannedBudget>> getPlannedBudgetsByMonthGroupedByScope(final Month month) throws CsvIOException {
        return getAllPlannedBudgets().stream()
                .filter(plannedBudget -> plannedBudget.getMonth() == month)
                .collect(Collectors.groupingBy(PlannedBudget::getScope));
    }

    public PlannedBudget upsertPlannedBudget(final PlannedBudget plannedBudget) throws CsvIOException {
        final List<PlannedBudget> existingPlannedBudgets = getAllPlannedBudgets();

        boolean somethingToPersist = false;
        PlannedBudget result;

        if (null == plannedBudget.getId()) {
            // without id -> new planned expense
            result = plannedBudget.toBuilder().id(UUID.randomUUID()).build();

            existingPlannedBudgets.add(result);
            somethingToPersist = true;
        } else {
            // with id -> existing planned expense
            result = existingPlannedBudgets.stream()
                    .filter(a -> a.getId().equals(plannedBudget.getId()))
                    .findFirst()
                    .orElse(null);

            if (null == result) {
                log.warn("[upsertPlannedExpense] Received a planned expense with an id, but there is no existing one with this id (operation ignored)");
            } else {
                result.setConcept(plannedBudget.getConcept());
                result.setMonth(plannedBudget.getMonth());
                result.setQuantity(plannedBudget.getQuantity());
                result.setScope(plannedBudget.getScope());
                somethingToPersist = true;
            }
        }

        if (somethingToPersist) {
            persistPlannedBudgets(existingPlannedBudgets);
        }

        return result;
    }

    public void persistPlannedBudgets(final List<PlannedBudget> plannedBudgets) throws CsvIOException {
        final File file = getFile(getFilename());

        final List<String[]> sortedRawPlannedBudgets = plannedBudgets.stream()
                .sorted(Comparator
                        .comparing(PlannedBudget::getScope)
                        .thenComparing(PlannedBudget::getConcept)
                        .thenComparing(PlannedBudget::getMonth))
                .map(PlannedBudget::toStringArray)
                .collect(Collectors.toList());

        persistData(sortedRawPlannedBudgets, file);
    }

    private String getFilename() {
        return PLANNED_BUDGETS_FILENAME;
    }
}
