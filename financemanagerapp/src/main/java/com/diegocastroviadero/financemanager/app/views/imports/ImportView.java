package com.diegocastroviadero.financemanager.app.views.imports;

import com.diegocastroviadero.financemanager.app.model.Account;
import com.diegocastroviadero.financemanager.app.services.AccountService;
import com.diegocastroviadero.financemanager.app.services.AuthService;
import com.diegocastroviadero.financemanager.app.services.ImportService;
import com.diegocastroviadero.financemanager.app.utils.IconUtils;
import com.diegocastroviadero.financemanager.app.views.main.MainView;
import com.diegocastroviadero.financemanager.cryptoutils.exception.CsvCryptoIOException;
import com.diegocastroviadero.financemanager.cryptoutils.exception.WrongEncryptionPasswordException;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Route(value = "Import", layout = MainView.class)
@PageTitle("Import | Finance Manager")
public class ImportView extends VerticalLayout {

    private final AuthService authService;
    private final AccountService accountService;
    private final ImportService importService;

    private final Account NEW_ACCOUNT = Account.builder()
            .accountNumber("NEW ACCOUNT")
            .build();

    private final TextField extractedAccount = new TextField("Extracted account from file name");
    private final ComboBox<Account> accountCombo = new ComboBox<>("Account");
    private final Component accountSelectorForm;
    private final Component accountRegistrationForm;

    public ImportView(final AuthService authService, final AccountService accountService, final ImportService importService) {
        this.authService = authService;
        this.accountService = accountService;
        this.importService = importService;

        addClassName("import-view");
        setSizeFull();

        authService.configureAuth(this);

        accountSelectorForm = getAccountSelectorForm();
        accountRegistrationForm = getAccountRegistrationForm();

        add(getToolbar(), accountSelectorForm, accountRegistrationForm);
    }

    private HorizontalLayout getToolbar() {
        final MemoryBuffer uploadBuffer = new MemoryBuffer();

        final Button uploadButton = new Button("Load file", new Icon(VaadinIcon.UPLOAD_ALT));
        uploadButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);

        final Upload uploadFile = new Upload(uploadBuffer);
        uploadFile.setMaxFiles(1);
        uploadFile.setUploadButton(uploadButton);
        uploadFile.setDropLabel(new Label("Upload file"));
        uploadFile.setAcceptedFileTypes("text/csv", ".csv", "application/vnd.ms-excel");

        uploadFile.addSucceededListener(uploadedEvent -> authService.authenticate(this, password -> {
            populateAccountsInCombo(password, uploadedEvent.getFileName());

            accountSelectorForm.setVisible(Boolean.TRUE);
        }));

        uploadFile.addFileRejectedListener(event -> Notification.show(String.format("Error uploading backup file '%s' to server. Try again later", event.getErrorMessage()), 5000, Notification.Position.MIDDLE));

        uploadFile.getElement().addEventListener("file-remove", event -> {
            accountSelectorForm.setVisible(Boolean.FALSE);
            accountRegistrationForm.setVisible(Boolean.FALSE);
        });

        final HorizontalLayout toolbar = new HorizontalLayout(uploadFile);
        toolbar.addClassName("toolbar");
        toolbar.setWidthFull();
        toolbar.setSpacing(Boolean.FALSE);
        toolbar.setDefaultVerticalComponentAlignment(Alignment.CENTER);

        toolbar.expand(uploadFile);

        toolbar.add(uploadFile);

        return toolbar;
    }

    private Component getAccountSelectorForm() {
        extractedAccount.setWidthFull();
        extractedAccount.setReadOnly(Boolean.TRUE);

        accountCombo.setWidthFull();
        accountCombo.getElement().getStyle().set("--vaadin-combo-box-overlay-width", "250px");
        accountCombo.setRequired(Boolean.TRUE);
        accountCombo.setItemLabelGenerator(account -> null == account.getAlias() ? account.getAccountNumber() : String.format("%s | %s", account.getAccountNumber(), account.getAlias()));
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
        accountCombo.addValueChangeListener(event -> accountRegistrationForm.setVisible(isNewAccountSelected(event.getValue())));

        final VerticalLayout layout = new VerticalLayout(extractedAccount, accountCombo);
        layout.setWidthFull();
        layout.setPadding(Boolean.FALSE);
        layout.setSpacing(Boolean.FALSE);
        layout.setVisible(Boolean.FALSE);

        return layout;
    }

    private boolean isNewAccountSelected(final Account selectedAccount) {
        return null != selectedAccount
                && StringUtils.equals(selectedAccount.getAccountNumber(), NEW_ACCOUNT.getAccountNumber());
    }

    private Component getAccountRegistrationForm() {
        final VerticalLayout layout = new VerticalLayout(new H4("Register new account"), new TextField("Account number"), new Button("Register"));
        layout.setWidthFull();
        layout.setPadding(Boolean.FALSE);
        layout.setSpacing(Boolean.FALSE);
        layout.setVisible(Boolean.FALSE);

        return layout;
    }

    private void populateAccountsInCombo(final char[] password, final String fileName) {
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

        if (resultOk) {
            accounts.add(NEW_ACCOUNT);
            accountCombo.setItems(accounts);

            final String extractedAccountNumber = importService.extractAccountNumberFromFilename(fileName);

            final Account selectedAccount;

            if (null != extractedAccountNumber) {
                extractedAccount.setValue(extractedAccountNumber);

                LevenshteinDistance levenshteinDistance = new LevenshteinDistance();

                final Optional<FoundAccount> foundAccount = accounts.stream()
                        .map(account -> FoundAccount.builder()
                                .account(account)
                                .levenshteinDistance(levenshteinDistance.apply(account.getAccountNumber(), extractedAccountNumber))
                                .build())
                        .filter(fa -> fa.getLevenshteinDistance() <= 8)
                        .min(Comparator.comparing(FoundAccount::getLevenshteinDistance));

                if (foundAccount.isPresent()) {
                    final FoundAccount fa = foundAccount.get();

                    selectedAccount = fa.getAccount();
                } else {
                    selectedAccount = NEW_ACCOUNT;
                }
            } else {
                extractedAccount.setValue("<Nothing>");

                selectedAccount = NEW_ACCOUNT;
            }

            accountCombo.setValue(selectedAccount);
        }
    }

    @Builder
    @Getter
    private static class FoundAccount {
        private final Account account;
        private final Integer levenshteinDistance;
    }
}
