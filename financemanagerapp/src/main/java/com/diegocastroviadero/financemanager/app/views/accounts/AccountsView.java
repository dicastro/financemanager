package com.diegocastroviadero.financemanager.app.views.accounts;

import com.diegocastroviadero.financemanager.app.model.Account;
import com.diegocastroviadero.financemanager.app.model.AccountPurpose;
import com.diegocastroviadero.financemanager.app.model.Bank;
import com.diegocastroviadero.financemanager.app.model.Scope;
import com.diegocastroviadero.financemanager.app.services.AccountService;
import com.diegocastroviadero.financemanager.app.services.AuthService;
import com.diegocastroviadero.financemanager.app.utils.IconUtils;
import com.diegocastroviadero.financemanager.app.utils.Utils;
import com.diegocastroviadero.financemanager.app.views.common.ConfirmationDialog;
import com.diegocastroviadero.financemanager.app.views.main.MainView;
import com.diegocastroviadero.financemanager.cryptoutils.exception.CsvCryptoIOException;
import com.diegocastroviadero.financemanager.cryptoutils.exception.WrongEncryptionPasswordException;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@Route(value = "accounts", layout = MainView.class)
@PageTitle("Accounts | Finance Manager")
public class AccountsView extends HorizontalLayout {

    private final AuthService authService;
    private final AccountService accountService;

    private final AtomicReference<List<Account>> linkableAccounts = new AtomicReference<>(Collections.emptyList());
    private final Grid<Account> accountsGrid;
    private final AccountForm accountForm;

    public AccountsView(final AuthService authService, final AccountService accountService) {
        this.authService = authService;
        this.accountService = accountService;
        accountsGrid = new Grid<>(Account.class);

        addClassName("accounts-view");
        setSizeFull();

        configureGrid();

        final ConfirmationDialog confirmationDialog = new ConfirmationDialog("Confirm account deletion");

        confirmationDialog.addListener(ConfirmationDialog.ConfirmedEvent.class, event -> prepareDeleteAccount((Account) event.getContext().get("account")));

        accountForm = new AccountForm(Bank.values(), AccountPurpose.values(), Scope.values());
        accountForm.addListener(AccountForm.SaveEvent.class, this::prepareSaveAccount);
        accountForm.addListener(AccountForm.DeleteEvent.class, event -> confirmationDialog.open(String.format("Are you sure you want to delete account '%s' (%s)", event.getAccount().getAlias(), event.getAccount().getAccountNumber()), Map.of("account", event.getAccount())));
        accountForm.addListener(AccountForm.CloseEvent.class, e -> closeEditor());

        authService.configureAuth(this);

        final Div content = new Div(accountsGrid, accountForm);
        content.addClassName("content");

        add(content);

        closeEditor();
        prepareLoadDataGrid();
    }

    private void configureGrid() {
        accountsGrid.addClassName("accounts-grid");

        accountsGrid.removeAllColumns();

        accountsGrid.addComponentColumn(IconUtils::getBankIcon);
        accountsGrid.addColumn(Account::getAccountNumber)
                .setHeader("Account number");
        accountsGrid.addColumn(Account::getAlias)
                .setHeader("Alias");
        accountsGrid.addComponentColumn(IconUtils::getPurposeIcon);
        accountsGrid.addComponentColumn(IconUtils::getScopeIcon);
        accountsGrid.addColumn(account -> Utils.tableFormatDate(account.getBalanceDate()))
                .setHeader("Balance date");
        accountsGrid.addColumn(account -> Utils.tableFormatMoney(account.getBalance()))
                .setHeader("Balance")
                .setTextAlign(ColumnTextAlign.END);
        accountsGrid.addColumn(account -> {
            String linkedAccountLabel = null;

            if (null != account.getLink()) {
                linkedAccountLabel = account.getLink().getLabel();
            }

            return linkedAccountLabel;
        }).setHeader("Link");


        accountsGrid.getColumns().forEach(column -> column.setAutoWidth(Boolean.TRUE));
        accountsGrid.asSingleSelect().addValueChangeListener(event -> editAccount(event.getValue()));
        accountsGrid.setHeightByRows(Boolean.TRUE);
    }

    private void prepareSaveAccount(final AccountForm.SaveEvent event) {
        authService.authenticate(this, password -> saveAccount(password, event.getAccount()));
    }

    private void saveAccount(final char[] password, final Account account) {
        boolean close = true;

        try {
            accountService.persistAccounts(password, accountsGrid.getDataProvider().fetch(new Query<>()).collect(Collectors.toList()));
        } catch (WrongEncryptionPasswordException e) {
            Notification.show(String.format("Account '%s' (%s) could not be persisted because provided encryption password is not correct", account.getAlias(), account.getAccountNumber()), 5000, Notification.Position.MIDDLE);
            close = false;
        } catch (CsvCryptoIOException e) {
            final String message = String.format("There was an error while persisting account '%s' (%s)", account.getAlias(), account.getAccountNumber());
            log.error(message, e);
            Notification.show(message, 5000, Notification.Position.MIDDLE);
        }

        if (close) {
            updateAccountGrid(password);
            closeEditor();
        }
    }

    private void prepareDeleteAccount(final Account account) {
        authService.authenticate(this, password -> deleteAccount(password, account));
    }

    private void deleteAccount(final char[] password, final Account account) {
        boolean close = true;

        try {
            accountService.deleteAccount(password, account);
        } catch (WrongEncryptionPasswordException e) {
            Notification.show(String.format("Account '%s' (%s) could not be deleted because provided encryption password is not correct", account.getAlias(), account.getAccountNumber()), 5000, Notification.Position.MIDDLE);
            close = false;
        } catch (CsvCryptoIOException e) {
            final String message = String.format("There was an error deleting account '%s' (%s)", account.getAlias(), account.getAccountNumber());
            log.error(message, e);
            Notification.show(message, 5000, Notification.Position.MIDDLE);
        }

        if (close) {
            updateAccountGrid(password);
            closeEditor();
        }
    }

    private void editAccount(final Account account) {
        if (account == null) {
            closeEditor();
        } else {
            accountForm.show(account, getLinkableAccounts(account));
            addClassName("editing");
        }
    }

    private void closeEditor() {
        accountForm.hide();
        accountsGrid.deselectAll();
        removeClassName("editing");
    }

    private void prepareLoadDataGrid() {
        authService.authenticate(this, password -> {
            boolean result = updateAccountGrid(password);

            if (result) {
                loadLinkableAccounts(password);
            }
        });
    }

    private boolean updateAccountGrid(final char[] password) {
        boolean result = true;
        try {
            final List<Account> accounts = accountService.getAllAccounts(password);
            accountsGrid.setItems(accounts);
        } catch (WrongEncryptionPasswordException e) {
            final String errorMessage = "Accounts could not be read because provided encryption password is wrong";
            log.error(errorMessage);
            authService.forgetPassword();
            result = false;
            Notification.show(errorMessage, 5000, Notification.Position.MIDDLE);
        } catch (CsvCryptoIOException e) {
            final String errorMessage = "Accounts could not be read because an unexpected error";
            log.error(errorMessage, e);
            result = false;
            Notification.show(errorMessage, 5000, Notification.Position.MIDDLE);
        }

        return result;
    }

    private void loadLinkableAccounts(final char[] password) {
        try {
            linkableAccounts.set(accountService.getAllLinkableAccounts(password));
        } catch (WrongEncryptionPasswordException e) {
            final String errorMessage = "Linkable accounts could not be read because provided encryption password is wrong";
            log.error(errorMessage);
            authService.forgetPassword();
            Notification.show(errorMessage, 5000, Notification.Position.MIDDLE);
        } catch (CsvCryptoIOException e) {
            final String errorMessage = "Linkable accounts could not be read because an unexpected error";
            log.error(errorMessage, e);
            Notification.show(errorMessage, 5000, Notification.Position.MIDDLE);
        }
    }

    public List<Account> getLinkableAccounts(final Account account) {
        final List<Account> linkableAccounts;

        if (account.getPurpose() == AccountPurpose.CREDIT) {
            linkableAccounts = this.linkableAccounts.get();
        } else {
            linkableAccounts = Collections.emptyList();
        }

        return linkableAccounts;
    }
}
