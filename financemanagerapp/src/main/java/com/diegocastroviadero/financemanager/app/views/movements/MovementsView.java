package com.diegocastroviadero.financemanager.app.views.movements;

import com.diegocastroviadero.financemanager.app.model.Account;
import com.diegocastroviadero.financemanager.app.model.Movement;
import com.diegocastroviadero.financemanager.app.services.AccountService;
import com.diegocastroviadero.financemanager.app.services.AuthService;
import com.diegocastroviadero.financemanager.app.services.MovementService;
import com.diegocastroviadero.financemanager.app.utils.IconUtils;
import com.diegocastroviadero.financemanager.app.utils.Utils;
import com.diegocastroviadero.financemanager.app.views.common.AuthDialog;
import com.diegocastroviadero.financemanager.app.views.main.MainView;
import com.diegocastroviadero.financemanager.cryptoutils.exception.CsvCryptoIOException;
import com.diegocastroviadero.financemanager.cryptoutils.exception.WrongEncryptionPasswordException;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;
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

    final ComboBox<Account> accountFilter = new ComboBox<>("Account");
    final Button previousMonth = new Button(new Icon(VaadinIcon.ARROW_LEFT));
    final ComboBox<YearMonth> monthFilter = new ComboBox<>("Month");
    final Button nextMonth = new Button(new Icon(VaadinIcon.ARROW_RIGHT));

    Registration monthFilterListener;
    List<YearMonth> months;

    private final Grid<Movement> movementsGrid = new Grid<>(Movement.class);

    public MovementsView(final AuthService authService, final AccountService accountService, final MovementService movementService) {
        this.authService = authService;
        this.accountService = accountService;
        this.movementService = movementService;

        addClassName("movements-view");
        setSizeFull();

        configureMovementsGrid();

        authService.configureAuth(this);

        final Div content = new Div(movementsGrid);
        content.setClassName("content");

        add(getToolbar(), content);

        populateAccountsInToolbar();
    }

    private void configureMovementsGrid() {
        movementsGrid.addClassName("movements-grid");

        movementsGrid.removeAllColumns();

        movementsGrid.addColumn(Movement::getIndex)
                .setHeader("#")
                .setTextAlign(ColumnTextAlign.END);
        movementsGrid.addColumn(movement -> movement.getDate().getDayOfMonth())
                .setHeader("Day")
                .setTextAlign(ColumnTextAlign.END);
        movementsGrid.addColumn(Movement::getConcept)
                .setHeader("Concept");
        movementsGrid.addColumn(movement -> Utils.tableFormatMoney(movement.getQuantity()))
                .setHeader("Quantity")
                .setTextAlign(ColumnTextAlign.END);

        movementsGrid.getColumns().forEach(column -> column.setAutoWidth(Boolean.TRUE));
        movementsGrid.setHeightByRows(Boolean.TRUE);
    }

    private Component getToolbar() {
        accountFilter.getElement().getStyle().set("--vaadin-combo-box-overlay-width", "250px");
        accountFilter.setRequired(Boolean.TRUE);
        accountFilter.setItemLabelGenerator(Account::getLabel);
        accountFilter.setRenderer(new ComponentRenderer<>(account -> {
            final Image icon = IconUtils.getBankIcon(account);
            final Span text = new Span(account.getLabel());

            final HorizontalLayout layout = new HorizontalLayout(icon, text);
            layout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

            return layout;
        }));

        accountFilter.addValueChangeListener(event -> {
            final UUID accountId = event.getValue().getId();

            months = movementService.getYearMonthRange(accountId);

            if (months.isEmpty()) {
                monthFilter.setEnabled(Boolean.FALSE);
                previousMonth.setEnabled(Boolean.FALSE);
                nextMonth.setEnabled(Boolean.FALSE);
                Notification.show("There are no movements to be shown", 10000, Notification.Position.MIDDLE);
            } else {
                monthFilter.setEnabled(Boolean.TRUE);

                final YearMonth oldMonthValue = monthFilter.getValue();

                if (null != monthFilterListener) {
                    monthFilterListener.remove();
                }

                monthFilter.clear();
                monthFilter.setItems(months);

                if (null != oldMonthValue && months.contains(oldMonthValue)) {
                    monthFilter.setValue(oldMonthValue);
                } else {
                    monthFilter.setValue(months.get(0));
                }

                updatePreviousNextMonthButtons();

                monthFilterListener = configureMonthFilterValueChangeListener();

                updateValues(accountId, monthFilter.getValue());
            }
        });

        previousMonth.setEnabled(Boolean.FALSE);
        previousMonth.addClickListener(event -> {
            final int currentMonthPosition = months.indexOf(monthFilter.getValue());

            monthFilter.setValue(months.get(currentMonthPosition + 1));
        });

        nextMonth.setEnabled(Boolean.FALSE);
        nextMonth.addClickListener(event -> {
            final int currentMonthPosition = months.indexOf(monthFilter.getValue());

            monthFilter.setValue(months.get(currentMonthPosition - 1));
        });

        monthFilter.setRequired(Boolean.TRUE);
        monthFilter.setItemLabelGenerator(yearMonth -> yearMonth.format(DateTimeFormatter.ofPattern("yyyy/MM")));

        monthFilterListener = configureMonthFilterValueChangeListener();

        final HorizontalLayout accountLayout = new HorizontalLayout();
        accountLayout.setWidthFull();
        accountLayout.setDefaultVerticalComponentAlignment(Alignment.END);

        accountLayout.expand(accountFilter);

        accountLayout.add(accountFilter);

        final HorizontalLayout monthLayout = new HorizontalLayout();
        monthLayout.setWidthFull();
        monthLayout.setDefaultVerticalComponentAlignment(Alignment.END);

        monthLayout.expand(monthFilter);
        monthLayout.setFlexGrow(0.15, previousMonth);
        monthLayout.setFlexGrow(0.15, nextMonth);

        monthLayout.add(previousMonth, monthFilter, nextMonth);

        return new FormLayout(accountLayout, monthLayout);
    }

    private Registration configureMonthFilterValueChangeListener() {
        return monthFilter.addValueChangeListener(event -> {
            updatePreviousNextMonthButtons();

            if (null != accountFilter.getValue()) {
                updateValues(accountFilter.getValue().getId(), event.getValue());
            } else {
                Notification.show("Movements cannot be loaded because no account was selected", 5000, Notification.Position.MIDDLE);
            }
        });
    }

    private void updatePreviousNextMonthButtons() {
        final YearMonth selectedMonth = monthFilter.getValue();

        final int selectedMonthPosition = months.indexOf(selectedMonth);

        if (selectedMonthPosition == 0) {
            nextMonth.setEnabled(Boolean.FALSE);
            previousMonth.setEnabled(Boolean.TRUE);
        } else if (selectedMonthPosition == (months.size() - 1)) {
            nextMonth.setEnabled(Boolean.TRUE);
            previousMonth.setEnabled(Boolean.FALSE);
        } else {
            nextMonth.setEnabled(Boolean.TRUE);
            previousMonth.setEnabled(Boolean.TRUE);
        }
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
