package com.diegocastroviadero.financemanager.app.views.plannedbudgets;

import com.diegocastroviadero.financemanager.app.model.PlannedBudget;
import com.diegocastroviadero.financemanager.app.model.Scope;
import com.diegocastroviadero.financemanager.app.services.PlannedBudgetService;
import com.diegocastroviadero.financemanager.app.utils.IconUtils;
import com.diegocastroviadero.financemanager.app.utils.Utils;
import com.diegocastroviadero.financemanager.app.views.main.MainView;
import com.diegocastroviadero.financemanager.cryptoutils.exception.CsvIOException;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import lombok.extern.slf4j.Slf4j;

import java.time.Month;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Route(value = "plannedbudgets", layout = MainView.class)
@PageTitle("Planned Budgets | Finance Manager")
public class PlannedBudgetsView extends VerticalLayout {

    private final PlannedBudgetService plannedBudgetService;

    private final Grid<PlannedBudget> budgetsGrid;
    private final TextField filterText = new TextField();

    private final PlannedBudgetForm budgetForm;

    public PlannedBudgetsView(final PlannedBudgetService plannedBudgetService) {
        this.plannedBudgetService = plannedBudgetService;
        budgetsGrid = new Grid<>(PlannedBudget.class);

        addClassName("planned-budgets-view");
        setSizeFull();

        configureGrid();

        budgetForm = new PlannedBudgetForm(Scope.values(), Month.values());
        budgetForm.addListener(PlannedBudgetForm.SaveEvent.class, this::saveBudget);
        budgetForm.addListener(PlannedBudgetForm.DeleteEvent.class, this::deleteBudget);
        budgetForm.addListener(PlannedBudgetForm.CloseEvent.class, e -> closeEditor());

        final Div content = new Div(budgetsGrid, budgetForm);
        content.addClassName("content");

        add(getToolbar(), content);

        closeEditor();
        updateBudgetsGrid();
    }

    private HorizontalLayout getToolbar() {
        filterText.setPlaceholder("Filter by concept");
        filterText.setClearButtonVisible(Boolean.TRUE);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> updateBudgetsGrid());

        final Button addElement = new Button("Add budget");
        addElement.addClickListener(click -> addBudget());

        final HorizontalLayout toolbar = new HorizontalLayout(filterText, addElement);
        toolbar.addClassName("toolbar");

        return toolbar;
    }

    private void addBudget() {
        budgetsGrid.deselectAll();
        editBudget(PlannedBudget.builder().build());
    }

    private void configureGrid() {
        budgetsGrid.addClassName("planned-budgets-grid");

        budgetsGrid.setMultiSort(Boolean.TRUE);

        budgetsGrid.removeAllColumns();

        budgetsGrid.addColumn(PlannedBudget::getConcept)
                .setHeader("Concept")
                .setSortProperty("concept");
        budgetsGrid.addComponentColumn(IconUtils::getScopeIcon)
                .setHeader("Scope")
                .setComparator(Comparator.comparing(PlannedBudget::getScope))
                .setSortProperty("scope");
        budgetsGrid.addColumn(budget -> Utils.tableFormatMonthAbbreviated(budget.getMonth()))
                .setHeader("Month")
                .setComparator(Comparator.comparing(PlannedBudget::getMonth))
                .setSortProperty("month");
        budgetsGrid.addColumn(budget -> Utils.tableFormatMoney(budget.getQuantity()))
                .setHeader("Quantity")
                .setTextAlign(ColumnTextAlign.END);

        budgetsGrid.getColumns().forEach(column -> column.setAutoWidth(Boolean.TRUE));
        budgetsGrid.setHeightByRows(Boolean.TRUE);
        budgetsGrid.asSingleSelect().addValueChangeListener(event -> editBudget(event.getValue()));
    }

    private void editBudget(final PlannedBudget budget) {
        if (budget == null) {
            closeEditor();
        } else {
            budgetForm.show(budget);
            addClassName("editing");
        }
    }

    private void saveBudget(final PlannedBudgetForm.SaveEvent event) {
        try {
            plannedBudgetService.upsertPlannedBudget(event.getBudget());
        } catch (CsvIOException e) {
            log.error("Error while persisting budget changes", e);

            Notification.show("Planned budget data could not be persisted", 5000, Notification.Position.MIDDLE);
        }

        updateBudgetsGrid();
        closeEditor();
    }

    private void deleteBudget(final PlannedBudgetForm.DeleteEvent event) {
        if (null == event.getBudget().getId()) {
            closeEditor();
        } else {
            boolean deleted = true;

            try {
                final List<PlannedBudget> plannedBudgets = budgetsGrid.getDataProvider()
                        .fetch(new Query<>())
                        .filter(e -> !e.getId().equals(event.getBudget().getId()))
                        .collect(Collectors.toList());

                plannedBudgetService.persistPlannedBudgets(plannedBudgets);
            } catch (CsvIOException e) {
                log.error("Error while deleting budget", e);
                deleted = false;
            }

            updateBudgetsGrid();
            closeEditor();

            if (!deleted) {
                Notification.show("Planned budget could not be deleted", 5000, Notification.Position.MIDDLE);
            }
        }
    }

    private void closeEditor() {
        budgetForm.hide();
        budgetsGrid.deselectAll();
        removeClassName("editing");
    }

    private void updateBudgetsGrid() {
        List<PlannedBudget> budgets = Collections.emptyList();

        try {
            budgets = plannedBudgetService.getAllPlannedBudgets(filterText.getValue());
        } catch (CsvIOException e) {
            log.error("Error while reading budgets", e);
        }

        budgetsGrid.setItems(budgets);
    }
}
