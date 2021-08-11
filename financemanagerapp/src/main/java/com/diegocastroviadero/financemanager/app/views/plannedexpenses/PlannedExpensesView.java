package com.diegocastroviadero.financemanager.app.views.plannedexpenses;

import com.diegocastroviadero.financemanager.app.model.PlannedExpense;
import com.diegocastroviadero.financemanager.app.model.Scope;
import com.diegocastroviadero.financemanager.app.services.PlannedExpenseService;
import com.diegocastroviadero.financemanager.app.views.main.MainView;
import com.diegocastroviadero.financemanager.cryptoutils.exception.CsvIOException;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
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
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Route(value = "plannedexpenses", layout = MainView.class)
@PageTitle("Planned Expenses | Finance Manager")
public class PlannedExpensesView extends VerticalLayout {

    private final PlannedExpenseService plannedExpenseService;

    private final Grid<PlannedExpense> expensesGrid;
    private final TextField filterText = new TextField();

    private final PlannedExpenseForm expenseForm;

    public PlannedExpensesView(final PlannedExpenseService plannedExpenseService) {
        this.plannedExpenseService = plannedExpenseService;
        expensesGrid = new Grid<>(PlannedExpense.class);

        addClassName("planned-expenses-view");
        setSizeFull();

        add(new H1("Planned expenses"));

        configureGrid();

        expenseForm = new PlannedExpenseForm(Scope.values(), Month.values());
        expenseForm.addListener(PlannedExpenseForm.SaveEvent.class, this::saveExpense);
        expenseForm.addListener(PlannedExpenseForm.DeleteEvent.class, this::deleteExpense);
        expenseForm.addListener(PlannedExpenseForm.CloseEvent.class, e -> closeEditor());

        final Div content = new Div(expensesGrid, expenseForm);
        content.addClassName("content");
        content.setSizeFull();

        add(getToolbar(), content);

        updateExpensesGrid();
        closeEditor();
    }

    private HorizontalLayout getToolbar() {
        filterText.setPlaceholder("Filter by concept");
        filterText.setClearButtonVisible(Boolean.TRUE);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> updateExpensesGrid());

        final Button addElement = new Button("Add expense");
        addElement.addClickListener(click -> addExpense());

        HorizontalLayout toolbar = new HorizontalLayout(filterText, addElement);
        toolbar.addClassName("toolbar");

        return toolbar;
    }

    private void addExpense() {
        expensesGrid.deselectAll();
        editExpense(PlannedExpense.builder().build());
    }

    private void configureGrid() {
        expensesGrid.addClassName("planned-expenses-grid");
        expensesGrid.setSizeFull();
        expensesGrid.setMultiSort(Boolean.TRUE);
        expensesGrid.removeColumnByKey("quantity");
        expensesGrid.setColumns("concept", "scope", "month");
        expensesGrid.addColumn(expense -> String.format("%.2f €", expense.getQuantity())).setHeader("Quantity").setTextAlign(ColumnTextAlign.END);

        expensesGrid.getColumns().forEach(column -> column.setAutoWidth(Boolean.TRUE));
        expensesGrid.asSingleSelect().addValueChangeListener(event -> editExpense(event.getValue()));
    }

    private void editExpense(final PlannedExpense expense) {
        if (expense == null) {
            closeEditor();
        } else {
            expenseForm.show(expense);
            addClassName("editing");
        }
    }

    private void saveExpense(final PlannedExpenseForm.SaveEvent event) {
        try {
            plannedExpenseService.upsertPlannedExpense(event.getExpense());
        } catch (CsvIOException e) {
            log.error("Error while persisting expense changes", e);

            Notification.show("Planned expense data could not be persisted", 5000, Notification.Position.MIDDLE);
        }

        updateExpensesGrid();
        closeEditor();
    }

    private void deleteExpense(final PlannedExpenseForm.DeleteEvent event) {
        if (null == event.getExpense().getId()) {
            closeEditor();
        } else {
            boolean deleted = true;

            try {
                final List<PlannedExpense> plannedExpenses = expensesGrid.getDataProvider()
                        .fetch(new Query<>())
                        .filter(e -> !e.getId().equals(event.getExpense().getId()))
                        .collect(Collectors.toList());

                plannedExpenseService.persistPlannedExpenses(plannedExpenses);
            } catch (CsvIOException e) {
                log.error("Error while deleting expense", e);
                deleted = false;
            }

            updateExpensesGrid();
            closeEditor();

            if (!deleted) {
                Notification.show("Planned expense could not be deleted", 5000, Notification.Position.MIDDLE);
            }
        }
    }

    private void closeEditor() {
        expenseForm.hide();
        expensesGrid.deselectAll();
        removeClassName("editing");
    }

    private void updateExpensesGrid() {
        List<PlannedExpense> expenses = Collections.emptyList();

        try {
            expenses = plannedExpenseService.getAllPlannedExpenses(filterText.getValue());
        } catch (CsvIOException e) {
            log.error("Error while reading accounts", e);
        }

        expensesGrid.setItems(expenses);
    }
}
