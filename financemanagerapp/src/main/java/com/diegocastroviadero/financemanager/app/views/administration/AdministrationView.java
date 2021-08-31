package com.diegocastroviadero.financemanager.app.views.administration;

import com.diegocastroviadero.financemanager.app.services.AuthCleanerService;
import com.diegocastroviadero.financemanager.app.services.BackupService;
import com.diegocastroviadero.financemanager.app.services.CacheCleanerService;
import com.diegocastroviadero.financemanager.app.services.UserConfigService;
import com.diegocastroviadero.financemanager.app.views.common.ConfirmationDialog;
import com.diegocastroviadero.financemanager.app.views.common.LoadBackupException;
import com.diegocastroviadero.financemanager.app.views.main.MainView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import elemental.json.Json;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;

@Slf4j
@Route(value = "administration", layout = MainView.class)
@PageTitle("AdministrationView | Finance Manager")
@Secured("ROLE_ADMIN")
public class AdministrationView extends VerticalLayout {

    private final UserConfigService userConfigService;
    private final AuthCleanerService authCleanerService;
    private final CacheCleanerService cacheCleanerService;
    private final BackupService backupService;

    public AdministrationView(final UserConfigService userConfigService, final AuthCleanerService authCleanerService, final CacheCleanerService cacheCleanerService, final BackupService backupService) {
        this.userConfigService = userConfigService;
        this.authCleanerService = authCleanerService;
        this.cacheCleanerService = cacheCleanerService;
        this.backupService = backupService;

        addClassName("administration-view");
        setSizeFull();

        final Component versionLayout = getVersionLayout();

        final Component cacheLayout = getCacheLayout();

        final Component backupLayout = getBackupLayout();

        final Component demoLayout = getDemoLayout();

        add(versionLayout, cacheLayout, backupLayout, demoLayout);
    }

    private Component getVersionLayout() {
        final TextField version = new TextField();
        version.setReadOnly(Boolean.TRUE);
        version.setWidthFull();
        version.setValue(userConfigService.getVersionLabel());

        final VerticalLayout layout = new VerticalLayout(new H4("Version"), version);
        layout.setWidthFull();
        layout.setPadding(Boolean.FALSE);
        layout.setSpacing(Boolean.FALSE);

        return layout;
    }

    private Component getCacheLayout() {
        // Auth cache layout
        final TextField authCacheStatus = new TextField("Auth cache status");
        authCacheStatus.setReadOnly(Boolean.TRUE);
        authCacheStatus.setValue(authCleanerService.getAuthStatusLabel());

        final Button cleanAuthCacheButton = new Button("Clean");
        cleanAuthCacheButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        cleanAuthCacheButton.addClickListener(event -> {
            authCleanerService.cleanAuth();
            authCacheStatus.setValue(authCleanerService.getAuthStatusLabel());
        });

        final HorizontalLayout authCacheLayout = new HorizontalLayout(authCacheStatus, cleanAuthCacheButton);
        authCacheLayout.setWidthFull();
        authCacheLayout.setDefaultVerticalComponentAlignment(Alignment.END);
        authCacheLayout.expand(authCacheStatus);

        // File cache layout
        final TextField globalCacheStatus = new TextField("Global cache status");
        globalCacheStatus.setReadOnly(Boolean.TRUE);
        globalCacheStatus.setValue(cacheCleanerService.getCacheStatusLabel());

        final Button cleanGlobalCacheButton = new Button("Clean");
        cleanGlobalCacheButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        cleanGlobalCacheButton.addClickListener(event -> {
            cacheCleanerService.cleanCache();
            globalCacheStatus.setValue(cacheCleanerService.getCacheStatusLabel());
        });

        final HorizontalLayout globalCacheLayout = new HorizontalLayout(globalCacheStatus, cleanGlobalCacheButton);
        globalCacheLayout.setWidthFull();
        globalCacheLayout.setDefaultVerticalComponentAlignment(Alignment.END);
        globalCacheLayout.expand(globalCacheStatus);

        // Clean All
        final Button cleanAllButton = new Button("Clean ALL caches");
        cleanAllButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        cleanAllButton.addClickListener(event -> {
            authCleanerService.cleanAuth();
            authCacheStatus.setValue(authCleanerService.getAuthStatusLabel());
            cacheCleanerService.cleanCache();
            globalCacheStatus.setValue(cacheCleanerService.getCacheStatusLabel());
        });

        final HorizontalLayout cleanAllLayout = new HorizontalLayout(cleanAllButton);
        cleanAllLayout.setWidthFull();
        cleanAllLayout.setDefaultVerticalComponentAlignment(Alignment.END);

        // Clean main layout
        final VerticalLayout layout = new VerticalLayout(new H4("Cache"), authCacheLayout, globalCacheLayout, cleanAllLayout);
        layout.setWidthFull();
        layout.setPadding(Boolean.FALSE);
        layout.setSpacing(Boolean.FALSE);

        return layout;
    }

    private Component getBackupLayout() {
        final Button downloadButton = new Button("Get Backup", new Icon(VaadinIcon.DOWNLOAD_ALT));
        downloadButton.setWidthFull();
        downloadButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        final Anchor downloadLink = new Anchor(backupService.backupFiles(e -> Notification.show("Backup could not be done", 5000, Notification.Position.MIDDLE)), "Backup");
        downloadLink.getElement().setAttribute("download", true);
        downloadLink.removeAll();
        downloadLink.add(downloadButton);

        final MemoryBuffer uploadBuffer = new MemoryBuffer();

        final Button uploadButton = new Button("Load Backup", new Icon(VaadinIcon.UPLOAD_ALT));
        uploadButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);

        final Upload uploadBackup = new Upload(uploadBuffer);
        uploadBackup.setMaxFiles(1);
        uploadBackup.setUploadButton(uploadButton);
        uploadBackup.setDropLabel(new Label("Upload a .tar.gz backup file"));
        uploadBackup.setAcceptedFileTypes("application/x-gzip");

        final ConfirmationDialog confirmationDialog = new ConfirmationDialog("Confirm backup load");

        confirmationDialog.addListener(ConfirmationDialog.ConfirmedEvent.class, confirmedEvent -> {
            final String backupFileName = uploadBuffer.getFileData().getFileName();

            try {
                backupService.loadBackup(backupFileName, uploadBuffer.getInputStream());

                Notification.show(String.format("Backup '%s' has been loaded successfully", backupFileName), 5000, Notification.Position.MIDDLE);
            } catch (LoadBackupException e) {
                Notification.show(String.format("There was an error loading backup '%s'. Try again later", backupFileName), 5000, Notification.Position.MIDDLE);
            } finally {
                cleanUploadedFiles(uploadBackup);
            }
        });

        confirmationDialog.addListener(ConfirmationDialog.CancelledEvent.class, confirmedEvent -> cleanUploadedFiles(uploadBackup));

        uploadBackup.addSucceededListener(uploadedEvent -> confirmationDialog.open(String.format("Are you sure, you want to load backup '%s'?", uploadedEvent.getFileName())));

        uploadBackup.addFileRejectedListener(event -> Notification.show(String.format("Error uploading backup file '%s' to server. Try again later", event.getErrorMessage()), 5000, Notification.Position.MIDDLE));

        final HorizontalLayout hl = new HorizontalLayout();
        hl.setWidthFull();
        hl.setDefaultVerticalComponentAlignment(Alignment.CENTER);

        hl.expand(uploadBackup);
        hl.setFlexGrow(0.25, downloadLink);

        hl.add(downloadLink, uploadBackup);

        final VerticalLayout layout = new VerticalLayout(new H4("Backup"), hl);
        layout.setWidthFull();
        layout.setPadding(Boolean.FALSE);
        layout.setSpacing(Boolean.FALSE);

        return layout;
    }

    private void cleanUploadedFiles(final Upload upload) {
        upload.getElement()
                .setPropertyJson("files", Json.createArray());
    }

    private Component getDemoLayout() {
        // Demo mode
        final Checkbox demoMode = new Checkbox("Activate demo mode", userConfigService.isDemoMode());
        demoMode.addValueChangeListener(e -> userConfigService.setDemoMode(e.getValue()));

        final VerticalLayout layout = new VerticalLayout(new H4("Demo"), demoMode);
        layout.setWidthFull();
        layout.setPadding(Boolean.FALSE);
        layout.setSpacing(Boolean.FALSE);

        return layout;
    }
}
