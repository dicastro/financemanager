package com.diegocastroviadero.financemanager.app.views.expenseestimation;

import com.diegocastroviadero.financemanager.app.model.PlannedBudget;
import com.diegocastroviadero.financemanager.app.model.PlannedExpense;
import com.diegocastroviadero.financemanager.app.model.Scope;
import com.diegocastroviadero.financemanager.app.services.PlannedBudgetService;
import com.diegocastroviadero.financemanager.app.services.PlannedExpenseService;
import com.diegocastroviadero.financemanager.app.utils.Utils;
import com.diegocastroviadero.financemanager.app.views.main.MainView;
import com.diegocastroviadero.financemanager.cryptoutils.exception.CsvIOException;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;

import java.math.BigDecimal;
import java.time.Month;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Route(value = "expenseestimation", layout = MainView.class)
@PageTitle("Expense Estimation | Finance Manager")
public class ExpenseEstimationView extends VerticalLayout {

    private final PlannedExpenseService plannedExpenseService;
    private final PlannedBudgetService plannedBudgetService;

    final ComboBox<Month> monthFilter = new ComboBox<>("Month");
    final Tabs tabs;

    private final Grid<PlannedExpense> plannedExpensesGrid = new Grid<>(PlannedExpense.class);
    private final Grid<PlannedBudget> plannedBudgetsGrid = new Grid<>(PlannedBudget.class);
    private final ExpenseSummaryComponent expenseSummaryComponent = new ExpenseSummaryComponent();

    public ExpenseEstimationView(final PlannedExpenseService plannedExpenseService, final PlannedBudgetService plannedBudgetService) {
        this.plannedExpenseService = plannedExpenseService;
        this.plannedBudgetService = plannedBudgetService;

        addClassName("expense-estimation-view");
        setSizeFull();

        add(new H1("Expense Estimation"));

        configurePlannedExpensesGrid();
        configurePlannedBudgetsGrid();

        final Scope defaultSelectedScope = getDefaultSelectedScope();
        final int defaultSelectedTabPosition = getScopePosition(defaultSelectedScope);

        tabs = new Tabs(Stream.of(Scope.values())
                .map(s -> new Tab(s.name()))
                .collect(Collectors.toList())
                .toArray(Tab[]::new));

        tabs.setSelectedIndex(defaultSelectedTabPosition);

        tabs.addSelectedChangeListener(event -> {
            final Scope scope = EnumUtils.getEnum(Scope.class, event.getSelectedTab().getLabel());

            updateValues(monthFilter.getValue(), scope);
        });

        add(getToolbar(), tabs, expenseSummaryComponent, new VerticalLayout(new H3("Planned expenses"), plannedExpensesGrid, new H3("Planned budgets"), plannedBudgetsGrid));

        updateValues(monthFilter.getValue(), defaultSelectedScope);
    }

    private Scope getDefaultSelectedScope() {
        return Scope.SHARED;
    }

    private int getScopePosition(final Scope scope) {
        return Arrays.binarySearch(Scope.values(), scope);
    }

    private Scope getSelectedScope() {
        return EnumUtils.getEnum(Scope.class, tabs.getSelectedTab().getLabel());
    }

    private void configurePlannedExpensesGrid() {
        plannedExpensesGrid.addClassName("planned-expenses-grid");

        plannedExpensesGrid.removeColumnByKey("id");
        plannedExpensesGrid.removeColumnByKey("month");
        plannedExpensesGrid.removeColumnByKey("scope");

        plannedExpensesGrid.setColumns("concept");
        plannedExpensesGrid.addColumn(plannedExpense -> String.format("%.2f €", plannedExpense.getQuantity())).setHeader("Quantity").setTextAlign(ColumnTextAlign.END);

        plannedExpensesGrid.setHeightByRows(Boolean.TRUE);
    }

    private void configurePlannedBudgetsGrid() {
        plannedBudgetsGrid.addClassName("planned-budgets-grid");

        plannedBudgetsGrid.removeColumnByKey("id");
        plannedBudgetsGrid.removeColumnByKey("month");
        plannedBudgetsGrid.removeColumnByKey("scope");

        plannedBudgetsGrid.setColumns("concept");
        plannedBudgetsGrid.addColumn(plannedBudget -> String.format("%.2f €", plannedBudget.getQuantity())).setHeader("Quantity").setTextAlign(ColumnTextAlign.END);

        plannedBudgetsGrid.setHeightByRows(Boolean.TRUE);
    }

    private HorizontalLayout getToolbar() {
        monthFilter.setItems(Month.values());
        monthFilter.setRequired(Boolean.TRUE);
        monthFilter.setValue(Utils.currentMonth());

        monthFilter.addValueChangeListener(event -> updateValues(event.getValue(), getSelectedScope()));

        final HorizontalLayout toolbar = new HorizontalLayout(monthFilter);
        toolbar.addClassName("toolbar");

        return toolbar;
    }

    private Map<Scope, List<PlannedBudget>> getPlannedBudgets(final Month month) {
        Map<Scope, List<PlannedBudget>> monthPlannedBudgetsByScope = Collections.emptyMap();

        try {
            monthPlannedBudgetsByScope = plannedBudgetService.getPlannedBudgetsByMonthGroupedByScope(month);
        } catch (CsvIOException e) {
            log.error("Error loading planned budgets", e);
        }

        return monthPlannedBudgetsByScope;
    }

    private Map<Scope, List<PlannedExpense>> getPlannedExpenses(final Month month) {
        Map<Scope, List<PlannedExpense>> monthPlannedExpensesByScope = Collections.emptyMap();

        try {
            monthPlannedExpensesByScope = plannedExpenseService.getPlannedExpensesByMonthGroupedByScope(month);
        } catch (CsvIOException e) {
            log.error("Error loading planned expenses", e);
        }

        return monthPlannedExpensesByScope;
    }

    private Map<Scope, BigDecimal> calculateExpenseSummary(final Map<Scope, List<PlannedExpense>> monthPlannedExpensesByScope, final Map<Scope, List<PlannedBudget>> monthPlannedBudgetsByScope) {
        final Map<Scope, BigDecimal> monthPlannedExpensesTotal = monthPlannedExpensesByScope.entrySet().stream()
                .map(entry -> {
                    final BigDecimal entryTotal = entry.getValue().stream().map(PlannedExpense::getQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);

                    return Map.entry(entry.getKey(), entryTotal);
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        final Map<Scope, BigDecimal> monthPlannedBudgetsTotal = monthPlannedBudgetsByScope.entrySet().stream()
                .map(entry -> {
                    final BigDecimal entryTotal = entry.getValue().stream().map(PlannedBudget::getQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);

                    return Map.entry(entry.getKey(), entryTotal);
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return Stream.of(Scope.values())
                .map(s -> {
                    final BigDecimal scopeMonthPlannedExpensesTotal = monthPlannedExpensesTotal.getOrDefault(s, BigDecimal.ZERO);
                    final BigDecimal scopeMonthPlannedBudgetsTotal = monthPlannedBudgetsTotal.getOrDefault(s, BigDecimal.ZERO);

                    return Map.entry(s, scopeMonthPlannedExpensesTotal.add(scopeMonthPlannedBudgetsTotal));
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private void updateValues(final Month month, final Scope selectedScope) {
        final Map<Scope, List<PlannedExpense>> monthPlannedExpensesByScope = getPlannedExpenses(month);
        final Map<Scope, List<PlannedBudget>> monthPlannedBudgetsByScope = getPlannedBudgets(month);

        plannedExpensesGrid.setItems(monthPlannedExpensesByScope.get(selectedScope));
        plannedBudgetsGrid.setItems(monthPlannedBudgetsByScope.get(selectedScope));

        final Map<Scope, BigDecimal> monthExpenseSummary = calculateExpenseSummary(monthPlannedExpensesByScope, monthPlannedBudgetsByScope);

        expenseSummaryComponent.setQuantity(monthExpenseSummary.get(selectedScope));
    }
}
