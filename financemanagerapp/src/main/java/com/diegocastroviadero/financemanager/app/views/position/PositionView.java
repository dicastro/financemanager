package com.diegocastroviadero.financemanager.app.views.position;

import com.diegocastroviadero.financemanager.app.model.Account;
import com.diegocastroviadero.financemanager.app.model.AccountPosition;
import com.diegocastroviadero.financemanager.app.model.AccountPositionHistory;
import com.diegocastroviadero.financemanager.app.services.AccountService;
import com.diegocastroviadero.financemanager.app.services.AuthService;
import com.diegocastroviadero.financemanager.app.utils.IconUtils;
import com.diegocastroviadero.financemanager.app.utils.Utils;
import com.diegocastroviadero.financemanager.app.views.common.AuthDialog;
import com.diegocastroviadero.financemanager.app.views.main.DrawerToggleEvent;
import com.diegocastroviadero.financemanager.app.views.main.MainView;
import com.diegocastroviadero.financemanager.cryptoutils.exception.CsvCryptoIOException;
import com.diegocastroviadero.financemanager.cryptoutils.exception.WrongEncryptionPasswordException;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.support.GenericApplicationContext;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Route(value = "position", layout = MainView.class)
@PageTitle("Position | Finance Manager")
public class PositionView extends VerticalLayout implements ApplicationListener<DrawerToggleEvent> {

    private final AuthService authService;
    private final AccountService accountService;

    private final Grid<AccountPosition> positionsGrid = new Grid<>(AccountPosition.class);
    private final PositionChart positionChart;

    public PositionView(final AuthService authService, final AccountService accountService, final GenericApplicationContext context) {
        this.authService = authService;
        this.accountService = accountService;

        configureEventListener(context);

        addClassName("position-view");
        setSizeFull();

        configurePositionsGrid();

        positionChart = new PositionChart();
        positionChart.addListener(PositionChart.CloseEvent.class, e -> closeChart());

        authService.configureAuth(this);

        final Div content = new Div(positionsGrid, positionChart);
        content.addClassName("content");

        add(content);

        closeChart();
        updateValues();
    }

    private void configureEventListener(final GenericApplicationContext context) {
        context.getApplicationListeners()
                .removeIf(listener -> listener.getClass().equals(this.getClass()));
        context.addApplicationListener(this);
    }

    private void configurePositionsGrid() {
        positionsGrid.addClassName("position-grid");

        positionsGrid.removeAllColumns();

        positionsGrid.addComponentColumn(IconUtils::getBankIcon);
        positionsGrid.addComponentColumn(IconUtils::getScopeIcon);
        positionsGrid.addColumn(AccountPosition::getAlias)
                .setHeader("Alias");
        positionsGrid.addColumn(accountPosition -> Utils.tableFormatDate(accountPosition.getBalanceDate()))
                .setHeader("Balance date");
        positionsGrid.addColumn(accountPosition -> Utils.tableFormatMoney(accountPosition.getBalance()))
                .setHeader("Balance")
                .setTextAlign(ColumnTextAlign.END);
        positionsGrid.addColumn(AccountPosition::getExtra)
                .setHeader("");

        positionsGrid.getColumns().forEach(column -> column.setAutoWidth(Boolean.TRUE));
        positionsGrid.asSingleSelect().addValueChangeListener(event -> showPositionHistory(event.getValue()));
        positionsGrid.setHeightByRows(Boolean.TRUE);
    }

    private void updateValues() {
        authService.authenticate(this, password -> {
            List<Account> accounts = null;

            try {
                accounts = accountService.getAllAccounts(password);
            } catch (WrongEncryptionPasswordException e) {
                final String errorMessage = "Accounts could not be read because provided encryption password is wrong";
                log.error(errorMessage);
                authService.forgetPassword();
                Notification.show(errorMessage, 5000, Notification.Position.MIDDLE);
            } catch (CsvCryptoIOException e) {
                final String errorMessage = "Accounts could not be read because an unexpected error";
                log.error(errorMessage, e);
                Notification.show(errorMessage, 5000, Notification.Position.MIDDLE);
            }

            final List<AccountPosition> positions = new ArrayList<>();

            if (null != accounts) {
                for (Account account : accounts) {
                    try {
                        positions.add(accountService.getAccountPosition(password, account));
                    } catch (WrongEncryptionPasswordException e) {
                        final String errorMessage = String.format("Balance of account '%s' could not be calculated because provided encryption password is wrong", account.getId());
                        log.error(errorMessage);
                        authService.forgetPassword();
                        Notification.show(errorMessage, 5000, Notification.Position.MIDDLE);
                    } catch (CsvCryptoIOException e) {
                        final String errorMessage = String.format("Balance of account '%s' could not be calculated because an unexpected error", account.getId());
                        log.error(errorMessage, e);
                        Notification.show(errorMessage, 5000, Notification.Position.MIDDLE);
                    }

                }
            }

            positionsGrid.setItems(positions);
        });
    }

    private void showPositionHistory(final AccountPosition accountPosition) {
        if (accountPosition == null) {
            closeChart();
        } else {
            authService.authenticate(this, password -> {
                try {
                    final AccountPositionHistory accountPositionHistory = accountService.getAccountPositionHistory(password, accountPosition);

                    addClassName("showing");
                    positionChart.show(accountPositionHistory);
                } catch (WrongEncryptionPasswordException e) {
                    final String errorMessage = String.format("History of account '%s' cannot be shown because provided encryption password is wrong", accountPosition.getAccountId());
                    log.error(errorMessage);
                    authService.forgetPassword();
                    Notification.show(errorMessage, 5000, Notification.Position.MIDDLE);
                    positionsGrid.deselectAll();
                } catch (CsvCryptoIOException e) {
                    final String errorMessage = String.format("History of account '%s' cannot be shown because of an unexpected error", accountPosition.getAccountId());
                    log.error(errorMessage, e);
                    Notification.show(errorMessage, 5000, Notification.Position.MIDDLE);
                    positionsGrid.deselectAll();
                }
            });
        }
    }

    private void closeChart() {
        positionChart.hide();
        positionsGrid.deselectAll();
        removeClassName("showing");
    }

    @Override
    public void onApplicationEvent(final DrawerToggleEvent drawerToggleEvent) {
        positionChart.rerender();
    }
}
