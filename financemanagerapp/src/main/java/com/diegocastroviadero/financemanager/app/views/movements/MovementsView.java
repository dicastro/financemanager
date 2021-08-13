package com.diegocastroviadero.financemanager.app.views.movements;

import com.diegocastroviadero.financemanager.app.model.Account;
import com.diegocastroviadero.financemanager.app.model.Movement;
import com.diegocastroviadero.financemanager.app.services.AccountService;
import com.diegocastroviadero.financemanager.app.services.AuthService;
import com.diegocastroviadero.financemanager.app.services.MovementService;
import com.diegocastroviadero.financemanager.app.views.common.AuthDialog;
import com.diegocastroviadero.financemanager.app.views.main.MainView;
import com.diegocastroviadero.financemanager.cryptoutils.exception.CsvCryptoIOException;
import com.diegocastroviadero.financemanager.cryptoutils.exception.WrongEncryptionPasswordException;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import lombok.extern.slf4j.Slf4j;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Route(value = "movements", layout = MainView.class)
@PageTitle("Movements | Finance Manager")
public class MovementsView extends VerticalLayout {

    private final AuthService authService;
    private final AccountService accountService;
    private final MovementService movementService;

    final ComboBox<YearMonth> monthFilter = new ComboBox<>("Month");
    final ComboBox<Account> accountFilter = new ComboBox<>("Account");

    private final Grid<Movement> movementsGrid = new Grid<>(Movement.class);

    public MovementsView(final AuthService authService, final AccountService accountService, final MovementService movementService) {
        this.authService = authService;
        this.accountService = accountService;
        this.movementService = movementService;

        addClassName("movements-view");
        setSizeFull();

        add(new H1("Movements"));

        configureMovementsGrid();

        final AuthDialog authDialog = authService.configureAuth(this);

        add(getToolbar(), movementsGrid, authDialog);

        populateAccountsInToolbar();
    }

    private void configureMovementsGrid() {
        movementsGrid.addClassName("movements-grid");

        movementsGrid.removeColumnByKey("accountId");
        movementsGrid.removeColumnByKey("account");
        movementsGrid.removeColumnByKey("bank");
        movementsGrid.removeColumnByKey("quantity");

        movementsGrid.setColumns("index", "date", "concept");

        movementsGrid.addColumn(Movement::getQuantity).setHeader("Quantity").setTextAlign(ColumnTextAlign.END);

        movementsGrid.getColumns().forEach(column -> column.setAutoWidth(Boolean.TRUE));
        movementsGrid.setHeightByRows(Boolean.TRUE);
    }

    private HorizontalLayout getToolbar() {
        final List<YearMonth> months = movementService.getYearMonthRange();

        monthFilter.setRequired(Boolean.TRUE);
        monthFilter.setItemLabelGenerator(yearMonth -> yearMonth.format(DateTimeFormatter.ofPattern("yyyy/MM")));

        if (months.isEmpty()) {
            monthFilter.setEnabled(Boolean.FALSE);
            Notification.show("There are no movements to be shown", 10000, Notification.Position.MIDDLE);
        } else {
            monthFilter.setItems(months);
            monthFilter.setValue(months.get(0));
        }

        monthFilter.addValueChangeListener(event -> {
            if (null != accountFilter.getValue()) {
                updateValues(accountFilter.getValue().getId(), event.getValue());
            } else {
                Notification.show("Movements cannot be loaded because no account was selected", 5000, Notification.Position.MIDDLE);
            }
        });

        accountFilter.setRequired(Boolean.TRUE);
        accountFilter.setItemLabelGenerator(Account::getLabel);

        accountFilter.addValueChangeListener(event -> {
            if (null != monthFilter.getValue()) {
                updateValues(event.getValue().getId(), monthFilter.getValue());
            } else {
                Notification.show("Movements cannot be loaded because no month was selected", 5000, Notification.Position.MIDDLE);
            }
        });

        final HorizontalLayout toolbar = new HorizontalLayout(monthFilter, accountFilter);
        toolbar.addClassName("toolbar");

        return toolbar;
    }

    private void populateAccountsInToolbar() {
        authService.authenticate(this, password -> {
            List<Account> accounts = Collections.emptyList();

            boolean resultOk = true;

            try {
                accounts = accountService.getAllAccounts(password);
            } catch (WrongEncryptionPasswordException e) {
                final String errorMessage = "Accounts could not be read because provided encryption password is wrong";
                log.error(errorMessage);
                authService.forgetPassword();
                resultOk = false;
                Notification.show(errorMessage, 5000, Notification.Position.MIDDLE);
            } catch (CsvCryptoIOException e) {
                final String errorMessage = "Accounts could not be read because an unexpected error";
                log.error(errorMessage, e);
                resultOk = false;
                Notification.show(errorMessage, 5000, Notification.Position.MIDDLE);
            }

            if (accounts.isEmpty()) {
                accountFilter.setEnabled(Boolean.FALSE);

                if (resultOk) {
                    Notification.show("There are no movements to be shown", 10000, Notification.Position.MIDDLE);
                }
            } else {
                accountFilter.setItems(accounts);
                accountFilter.setValue(accounts.get(0));
            }
        });
    }

    private void updateValues(final UUID id, final YearMonth currentYearMonth) {
        authService.authenticate(this, password -> {
            final List<Movement> movements = getMovements(password, id, currentYearMonth);

            movementsGrid.setItems(movements);
        });
    }

    private List<Movement> getMovements(final char[] password, final UUID id, final YearMonth currentYearMonth) {
        List<Movement> movements = Collections.emptyList();

        try {
            movements = movementService.getMovementsByAccountAndMonth(password, id, currentYearMonth);
        } catch (WrongEncryptionPasswordException e) {
            final String errorMessage = String.format("Movements of account '%s' in '%s' could not be read because provided encryption password is wrong", id, currentYearMonth);
            log.error(errorMessage);
            authService.forgetPassword();
            Notification.show(errorMessage, 5000, Notification.Position.MIDDLE);
        } catch (CsvCryptoIOException e) {
            final String errorMessage = String.format("Movements of account '%s' in '%s' could not be read because an unexpected error", id, currentYearMonth);
            log.error(errorMessage, e);
            Notification.show(errorMessage, 5000, Notification.Position.MIDDLE);
        }

        return movements;
    }
}
