package com.diegocastroviadero.financemanager.app.views.accounts;

import com.diegocastroviadero.financemanager.app.model.Account;
import com.diegocastroviadero.financemanager.app.model.AccountPurpose;
import com.diegocastroviadero.financemanager.app.model.Bank;
import com.diegocastroviadero.financemanager.app.model.Scope;
import com.diegocastroviadero.financemanager.app.utils.IconUtils;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.ShortcutRegistration;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.shared.Registration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public class AccountForm extends FormLayout {
    private Account account;

    final ComboBox<Bank> bank = new ComboBox<>("Bank");
    final TextField accountNumber = new TextField("Account number");
    final TextField alias = new TextField("Alias");
    final ComboBox<AccountPurpose> purpose = new ComboBox<>("Purpose");
    final ComboBox<Scope> scope = new ComboBox<>("Scope");
    final BigDecimalField balance = new BigDecimalField("Balance");
    final DatePicker balanceDate = new DatePicker("Balance date");
    final ComboBox<Account> link = new ComboBox<>("Link");

    final Button save = new Button("Save");
    final Button close = new Button("Close");

    final List<ShortcutRegistration> shortcuts = new ArrayList<>();

    final Binder<Account> binder = new BeanValidationBinder<>(Account.class);

    public AccountForm(final Bank[] banks, final AccountPurpose[] accountPurposes, final Scope[] scopes) {
        addClassName("account-form");

        binder.bindInstanceFields(this);

        bank.setReadOnly(Boolean.TRUE);
        bank.setItems(banks);
        bank.setItemLabelGenerator(Bank::name);

        accountNumber.setReadOnly(Boolean.TRUE);

        balance.addThemeVariants(TextFieldVariant.LUMO_ALIGN_RIGHT);
        balance.setPrefixComponent(new Icon(VaadinIcon.EURO));

        purpose.setItems(accountPurposes);
        purpose.setItemLabelGenerator(AccountPurpose::name);
        purpose.setRenderer(new ComponentRenderer<>(purpose -> {
            final Image icon = IconUtils.getPurposeIcon(purpose);
            final Span text = new Span(purpose.name());

            final HorizontalLayout layout = new HorizontalLayout(icon, text);
            layout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

            return layout;
        }));

        scope.setItems(scopes);
        scope.setItemLabelGenerator(Scope::name);
        scope.setRenderer(new ComponentRenderer<>(scope -> {
            final Icon icon = IconUtils.getScopeIcon(scope);
            final Span text = new Span(scope.name());

            final HorizontalLayout layout = new HorizontalLayout(icon, text);
            layout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

            return layout;
        }));

        link.setItemLabelGenerator(account -> StringUtils.isBlank(account.getAlias()) ? account.getAccountNumber() : account.getAlias());
        link.setClearButtonVisible(Boolean.TRUE);

        add(bank, accountNumber, alias, purpose, scope, balance, balanceDate, link, createButtonsLayout());
    }

    public void show(final Account account, final List<Account> linkableAccounts) {
        setAccount(account, linkableAccounts);

        shortcuts.add(save.addClickShortcut(Key.ENTER));
        shortcuts.add(close.addClickShortcut(Key.ESCAPE));

        setVisible(Boolean.TRUE);
    }

    public void hide() {
        setAccount(null, Collections.emptyList());

        shortcuts.forEach(ShortcutRegistration::remove);
        shortcuts.clear();

        setVisible(Boolean.FALSE);
    }

    private void setAccount(final Account account, final List<Account> linkableAccounts) {
        this.account = account;

        if (linkableAccounts.isEmpty()) {
            link.setEnabled(Boolean.FALSE);
        } else {
            link.setEnabled(Boolean.TRUE);
            link.setItems(linkableAccounts);
        }

        binder.readBean(account);
    }

    private HorizontalLayout createButtonsLayout() {
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        save.addClickListener(event -> validateAndSave());

        close.addClickListener(event -> fireEvent(new CloseEvent(this)));

        binder.addStatusChangeListener(e -> save.setEnabled(binder.isValid()));

        return new HorizontalLayout(save, close);
    }

    private void validateAndSave() {
        try {
            binder.writeBean(account);
            fireEvent(new SaveEvent(this, account));
        } catch (ValidationException e) {
            log.error("Account could not be saved because form is not valid", e);
        }
    }

    @Override
    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType, ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }

    @Getter
    public static abstract class AccountFormEvent extends ComponentEvent<AccountForm> {
        private final Account account;

        protected AccountFormEvent(final AccountForm source, final Account account) {
            super(source, false);
            this.account = account;
        }
    }

    public static class SaveEvent extends AccountFormEvent {
        SaveEvent(final AccountForm source, final Account account) {
            super(source, account);
        }
    }

    public static class CloseEvent extends AccountFormEvent {
        CloseEvent(final AccountForm source) {
            super(source, null);
        }
    }
}
