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
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class AccountImporterForm extends VerticalLayout {
    private static final String NEW_ACCOUNT_ALIAS = "REGISTER NEW ACCOUNT";

    private List<Account> accounts;
    private SelectedAccount selectedAccount;

    private final LevenshteinDistance levenshteinDistance = new LevenshteinDistance();

    private final TextField extractedAccount = new TextField("Extracted account from file name");
    private final ComboBox<Account> accountCombo = new ComboBox<>("Import to account");
    private final Button importFileButton = new Button("Import");

    private final Binder<SelectedAccount> binder = new BeanValidationBinder<>(SelectedAccount.class);

    private final AccountRegistrationForm accountRegistrationForm = new AccountRegistrationForm(Bank.values(), AccountPurpose.values());

    public AccountImporterForm() {
        addClassName("account-importer-form");

        accountRegistrationForm.addListener(AccountRegistrationForm.RegisterAccountEvent.class, event -> {
            fireEvent(new RegisterAccountEvent(this, event.getAccount()));
        });

        extractedAccount.setWidthFull();
        extractedAccount.setReadOnly(Boolean.TRUE);

        accountCombo.getElement().getStyle().set("--vaadin-combo-box-overlay-width", "300px");
        accountCombo.setRequired(Boolean.TRUE);
        accountCombo.setItemLabelGenerator(account -> Stream.of(account.getAccountNumber(), account.getAlias())
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" | ")));
        accountCombo.setRenderer(new ComponentRenderer<>(account -> {
            final Image icon = IconUtils.getBankIcon(account);

            final VerticalLayout vl = new VerticalLayout(new Span(account.getAccountNumber()), new Span(account.getAlias()));
            vl.setWidthFull();
            vl.setSpacing(Boolean.FALSE);
            vl.setPadding(Boolean.FALSE);

            final HorizontalLayout layout = new HorizontalLayout(icon, vl);
            layout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

            return layout;
        }));
        accountCombo.addValueChangeListener(event -> {
            if (newAccountItemIsSelected(event.getValue())) {
                accountRegistrationForm.show(event.getValue());
            } else {
                accountRegistrationForm.hide();
            }
        });

        importFileButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        importFileButton.setEnabled(Boolean.FALSE);
        importFileButton.addClickListener(event -> validateAndImport());

        binder.forField(accountCombo)
                .withValidator(this::newAccountItemIsNotSelected, "Registered account must be selected")
                .bind(SelectedAccount::getAccount, SelectedAccount::setAccount);
        binder.addStatusChangeListener(e -> importFileButton.setEnabled(binder.isValid()));

        final HorizontalLayout accountComboLayout = new HorizontalLayout();
        accountComboLayout.setWidthFull();
        accountComboLayout.setDefaultVerticalComponentAlignment(Alignment.BASELINE);

        accountComboLayout.setFlexGrow(0.75, accountCombo);
        accountComboLayout.setFlexGrow(0.25, importFileButton);

        accountComboLayout.add(accountCombo, importFileButton);

        add(extractedAccount, accountComboLayout, accountRegistrationForm);
        setWidthFull();
        setPadding(Boolean.FALSE);
        setSpacing(Boolean.FALSE);

        hide();
    }

    private void validateAndImport() {
        try {
            binder.writeBean(selectedAccount);
            fireEvent(new ImportAccountEvent(this, selectedAccount));
        } catch (ValidationException e) {
            log.error("File could not be imported because form is not valid", e);
        }
    }

    private boolean newAccountItemIsSelected(final Account selectedAccount) {
        return null != selectedAccount
                && null == selectedAccount.getId();
    }

    private boolean newAccountItemIsNotSelected(final Account selectedAccount) {
        return null != selectedAccount
                && null != selectedAccount.getId();
    }

    public void show(final String extractedAccountNumber, final List<Account> accounts) {
        setAccounts(extractedAccountNumber, accounts);

        setVisible(Boolean.TRUE);
    }

    public void hide() {
        setAccounts(null, new ArrayList<>());

        accountRegistrationForm.hide();

        setVisible(Boolean.FALSE);
    }

    public void updateRegisteredAccount(final Account registeredAccount) {
        accounts.removeIf(account -> account.getId() == null);

        accounts.add(registeredAccount);

        accountCombo.getDataProvider().refreshAll();
        accountCombo.setValue(registeredAccount);
    }

    private void setAccounts(final String extractedAccountNumber, final List<Account> accounts) {
        this.accounts = accounts;

        this.extractedAccount.setValue(StringUtils.isBlank(extractedAccountNumber) ? "<Nothing>" : extractedAccountNumber);

        final Account newAccountItem;
        final Account accountToSelect;

        if (null != extractedAccountNumber) {
            newAccountItem = Account.builder()
                    .accountNumber(extractedAccountNumber)
                    .alias(NEW_ACCOUNT_ALIAS)
                    .build();

            final Optional<FoundAccount> foundAccount = accounts.stream()
                    .map(account -> FoundAccount.builder()
                            .account(account)
                            .levenshteinDistance(levenshteinDistance.apply(account.getAccountNumber(), extractedAccountNumber))
                            .build())
                    .filter(fa -> fa.getLevenshteinDistance() <= 8)
                    .min(Comparator.comparing(FoundAccount::getLevenshteinDistance));

            if (foundAccount.isPresent()) {
                final FoundAccount fa = foundAccount.get();

                accountToSelect = fa.getAccount();
            } else {
                accountToSelect = newAccountItem;
            }
        } else {
            newAccountItem = Account.builder()
                    .alias(NEW_ACCOUNT_ALIAS)
                    .build();

            accountToSelect = newAccountItem;
        }

        accounts.add(newAccountItem);
        accountCombo.setItems(accounts);
        accountCombo.setValue(accountToSelect);

        selectedAccount = SelectedAccount.builder()
                .account(accountCombo.getValue())
                .build();

        binder.readBean(selectedAccount);
    }

    @Builder
    @Getter
    private static class FoundAccount {
        private final Account account;
        private final Integer levenshteinDistance;
    }

    @Override
    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType, ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }

    @Getter
    public static abstract class AccountImporterFormEvent extends ComponentEvent<AccountImporterForm> {
        private final Account account;

        protected AccountImporterFormEvent(final AccountImporterForm source, final Account account) {
            super(source, false);
            this.account = account;
        }
    }

    public static class RegisterAccountEvent extends AccountImporterFormEvent {
        RegisterAccountEvent(final AccountImporterForm source, final Account account) {
            super(source, account);
        }
    }

    public static class ImportAccountEvent extends AccountImporterFormEvent {
        ImportAccountEvent(final AccountImporterForm source, final SelectedAccount selectedAccount) {
            super(source, selectedAccount.getAccount());
        }
    }
}
