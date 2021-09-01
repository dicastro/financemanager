package com.diegocastroviadero.financemanager.app.views.imports;

import com.diegocastroviadero.financemanager.app.model.Account;
import com.diegocastroviadero.financemanager.app.model.AccountPurpose;
import com.diegocastroviadero.financemanager.app.model.Bank;
import com.diegocastroviadero.financemanager.app.utils.IconUtils;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.shared.Registration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AccountRegistrationForm extends VerticalLayout {
    private RegisterAccount registerAccount;

    final ComboBox<Bank> bank = new ComboBox<>("Bank");
    final TextField accountNumber = new TextField("Account number");
    final TextField alias = new TextField("Alias");
    final ComboBox<AccountPurpose> purpose = new ComboBox<>("Purpose");

    final Button register = new Button("Register");

    final Binder<RegisterAccount> binder = new BeanValidationBinder<>(RegisterAccount.class);

    public AccountRegistrationForm(final Bank[] banks, final AccountPurpose[] accountPurposes) {
        addClassName("register-account-form");

        binder.forField(bank).asRequired()
                .bind(RegisterAccount::getBank, RegisterAccount::setBank);
        binder.forField(accountNumber).asRequired()
                .bind(RegisterAccount::getAccountNumber, RegisterAccount::setAccountNumber);
        binder.forField(alias).asRequired()
                .bind(RegisterAccount::getAlias, RegisterAccount::setAlias);
        binder.forField(purpose).asRequired()
                .bind(RegisterAccount::getPurpose, RegisterAccount::setPurpose);

        bank.setRequired(Boolean.TRUE);
        bank.setItems(banks);
        bank.setItemLabelGenerator(Bank::name);
        bank.setRenderer(new ComponentRenderer<>(bank -> {
            final Image icon = IconUtils.getBankIcon(bank);
            final Span text = new Span(bank.name());

            final HorizontalLayout layout = new HorizontalLayout(icon, text);
            layout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

            return layout;
        }));

        purpose.setRequired(Boolean.TRUE);
        purpose.setItems(accountPurposes);
        purpose.setItemLabelGenerator(AccountPurpose::name);
        purpose.setRenderer(new ComponentRenderer<>(purpose -> {
            final Image icon = IconUtils.getPurposeIcon(purpose);
            final Span text = new Span(purpose.name());

            final HorizontalLayout layout = new HorizontalLayout(icon, text);
            layout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

            return layout;
        }));

        accountNumber.setRequired(Boolean.TRUE);
        alias.setRequired(Boolean.TRUE);

        final H4 formTitle = new H4("Register account");
        formTitle.setWidthFull();

        final FormLayout formLayout = new FormLayout();
        formLayout.add(bank, accountNumber, alias, purpose);

        add(formTitle, formLayout, createButtonsLayout());

        setPadding(Boolean.FALSE);
        setSpacing(Boolean.FALSE);
        getElement()
                .getStyle().set("padding-bottom", "var(--lumo-space-m)");

        hide();
    }

    public void show(final Account account) {
        setAccount(RegisterAccount.builder()
                .bank(account.getBank())
                .accountNumber(account.getAccountNumber())
                .alias(account.getAlias())
                .purpose(account.getPurpose())
                .build());

        setVisible(Boolean.TRUE);
    }

    public void hide() {
        setAccount(null);

        setVisible(Boolean.FALSE);
    }

    private void setAccount(final RegisterAccount registerAccount) {
        this.registerAccount = registerAccount;

        binder.readBean(registerAccount);
    }

    private HorizontalLayout createButtonsLayout() {
        register.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        register.addClickListener(event -> validateAndSave());

        binder.addStatusChangeListener(e -> register.setEnabled(binder.isValid()));

        final HorizontalLayout layout = new HorizontalLayout(register);
        layout.setWidthFull();
        layout.setSpacing(Boolean.FALSE);
        layout.setPadding(Boolean.FALSE);
        layout.getStyle().set("padding-top", "var(--lumo-space-m)");

        return layout;
    }

    private void validateAndSave() {
        try {
            binder.writeBean(registerAccount);
            fireEvent(new RegisterAccountEvent(this, registerAccount));
        } catch (ValidationException e) {
            log.error("Account could not be registered because form is not valid", e);
        }
    }

    @Override
    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType, ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }

    @Getter
    public static abstract class AccountRegistrationFormEvent extends ComponentEvent<AccountRegistrationForm> {
        private final Account account;

        protected AccountRegistrationFormEvent(final AccountRegistrationForm source, final RegisterAccount registerAccount) {
            super(source, false);
            this.account = Account.builder()
                    .bank(registerAccount.getBank())
                    .accountNumber(registerAccount.getAccountNumber())
                    .alias(registerAccount.getAlias())
                    .purpose(registerAccount.getPurpose())
                    .build();
        }
    }

    public static class RegisterAccountEvent extends AccountRegistrationFormEvent {
        RegisterAccountEvent(final AccountRegistrationForm source, final RegisterAccount registerAccount) {
            super(source, registerAccount);
        }
    }
}
