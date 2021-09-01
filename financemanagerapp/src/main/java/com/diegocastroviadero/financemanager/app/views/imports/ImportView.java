package com.diegocastroviadero.financemanager.app.views.imports;

import com.diegocastroviadero.financemanager.app.model.Account;
import com.diegocastroviadero.financemanager.app.model.ImporterResult;
import com.diegocastroviadero.financemanager.app.services.AccountService;
import com.diegocastroviadero.financemanager.app.services.AuthService;
import com.diegocastroviadero.financemanager.app.services.ImportService;
import com.diegocastroviadero.financemanager.app.views.main.MainView;
import com.diegocastroviadero.financemanager.cryptoutils.exception.CsvCryptoIOException;
import com.diegocastroviadero.financemanager.cryptoutils.exception.WrongEncryptionPasswordException;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import elemental.json.Json;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Route(value = "Import", layout = MainView.class)
@PageTitle("Import | Finance Manager")
public class ImportView extends VerticalLayout {

    private final AuthService authService;
    private final AccountService accountService;
    private final ImportService importService;

    private final MemoryBuffer uploadBuffer = new MemoryBuffer();
    private final Upload uploadFile = new Upload(uploadBuffer);

    private final AccountImporterForm accountImporterForm;

    public ImportView(final AuthService authService, final AccountService accountService, final ImportService importService) {
        this.authService = authService;
        this.accountService = accountService;
        this.importService = importService;

        addClassName("import-view");
        setSizeFull();

        authService.configureAuth(this);

        accountImporterForm = new AccountImporterForm();

        accountImporterForm.addListener(AccountImporterForm.RegisterAccountEvent.class, event -> authService.authenticate(this, password -> {
            final Account account = event.getAccount();

            try {
                final Account registeredAccount = accountService.registerAccount(password, account.getBank(), account.getAccountNumber(), account.getAlias(), account.getPurpose());

                accountImporterForm.updateRegisteredAccount(registeredAccount);
            } catch (WrongEncryptionPasswordException e) {
                Notification.show(String.format("Account '%s' (%s) could not be registered because provided encryption password is not correct", account.getAlias(), account.getAccountNumber()), 5000, Notification.Position.MIDDLE);
            } catch (CsvCryptoIOException e) {
                Notification.show(String.format("There was an error registering account '%s' (%s)", account.getAlias(), account.getAccountNumber()), 5000, Notification.Position.MIDDLE);
            }
        }));

        accountImporterForm.addListener(AccountImporterForm.ImportAccountEvent.class, event -> authService.authenticate(this, password -> {
            final List<ImporterResult> importerResults = importService.importFile(password, uploadBuffer.getInputStream(), uploadBuffer.getFileName(), event.getAccount());

            final List<ImporterResult> erroneusImporterResults = importerResults.stream()
                    .filter(ImporterResult::hasError)
                    .collect(Collectors.toList());

            if (erroneusImporterResults.isEmpty()) {
                accountImporterForm.hide();
                uploadFile.getElement()
                        .setPropertyJson("files", Json.createArray());

                Notification.show(String.format("File '%s' has been successcully imported into account '%s' (%s)", uploadBuffer.getFileName(), event.getAccount().getAlias(), event.getAccount().getAccountNumber()), 5000, Notification.Position.MIDDLE);
            } else {
                importerResults
                        .forEach(erroneousImportedFile -> Notification.show(erroneousImportedFile.getErrorCauses().stream()
                                .collect(Collectors.joining("\n - ", "- ", "")), 5000, Notification.Position.BOTTOM_START));
            }
        }));

        add(getToolbar(), accountImporterForm);
    }

    private HorizontalLayout getToolbar() {
        final Button uploadButton = new Button("Load file", new Icon(VaadinIcon.UPLOAD_ALT));
        uploadButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);

        uploadFile.setMaxFiles(1);
        uploadFile.setUploadButton(uploadButton);
        uploadFile.setDropLabel(new Label("Upload file (.csv or .xls)"));
        uploadFile.setAcceptedFileTypes("text/csv", ".csv", "application/vnd.ms-excel");

        uploadFile.addSucceededListener(uploadedEvent -> authService.authenticate(this, password -> populateAccountsInCombo(password, uploadedEvent.getFileName())));

        uploadFile.addFileRejectedListener(event -> Notification.show(String.format("Error uploading backup file '%s' to server. Try again later", event.getErrorMessage()), 5000, Notification.Position.MIDDLE));

        uploadFile.getElement().addEventListener("file-remove", event -> accountImporterForm.hide());

        final HorizontalLayout toolbar = new HorizontalLayout(uploadFile);
        toolbar.addClassName("toolbar");
        toolbar.setWidthFull();
        toolbar.setSpacing(Boolean.FALSE);
        toolbar.setDefaultVerticalComponentAlignment(Alignment.CENTER);

        toolbar.expand(uploadFile);

        toolbar.add(uploadFile);

        return toolbar;
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
            final String extractedAccountNumber = importService.extractAccountNumberFromFilename(fileName);

            accountImporterForm.show(extractedAccountNumber, accounts);
        }
    }
}
