package com.diegocastroviadero.financemanager.app.views.imports;

import com.diegocastroviadero.financemanager.app.model.ImportFile;
import com.diegocastroviadero.financemanager.app.model.ImportedFile;
import com.diegocastroviadero.financemanager.app.services.AuthService;
import com.diegocastroviadero.financemanager.app.services.ImportService;
import com.diegocastroviadero.financemanager.app.utils.IconUtils;
import com.diegocastroviadero.financemanager.app.views.common.AuthDialog;
import com.diegocastroviadero.financemanager.app.views.main.MainView;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Route(value = "Imports", layout = MainView.class)
@PageTitle("Imports | Finance Manager")
public class ImportsView extends VerticalLayout {

    private final AuthService authService;
    private final ImportService importService;

    private final Grid<ImportFile> importsGrid;
    private final Button importButton = new Button("Import");

    public ImportsView(final AuthService authService, final ImportService importService) {
        this.authService = authService;
        this.importService = importService;
        importsGrid = new Grid<>(ImportFile.class);

        addClassName("imports-view");
        setSizeFull();

        configureGrid();

        final AuthDialog authDialog = authService.configureAuth(this);

        add(getToolbar(), importsGrid, authDialog);

        updateImportsGrid();
    }

    private HorizontalLayout getToolbar() {
        importButton.addClickListener(click -> importAction());

        final HorizontalLayout toolbar = new HorizontalLayout(importButton);
        toolbar.addClassName("toolbar");

        return toolbar;
    }

    private void importAction() {
        log.debug("Importing all files ...");

        final List<ImportFile> importFiles = importsGrid.getDataProvider().fetch(new Query<>()).collect(Collectors.toList());

        importFiles.stream()
                .map(ImportFile::doImport)
                .filter(ImportedFile::hasError)
                .forEach(erroneousImportedFile -> Notification.show(erroneousImportedFile.getErrorCauses().stream()
                        .collect(Collectors.joining("\n - ", "- ", "")), 5000, Notification.Position.BOTTOM_START));

        updateImportsGrid();
    }

    private void configureGrid() {
        importsGrid.addClassName("imports-grid");
        importsGrid.setHeightByRows(Boolean.TRUE);
        importsGrid.removeAllColumns();

        importsGrid.addComponentColumn(IconUtils::getBankIcon)
                .setHeader("Bank");
        importsGrid.addColumn(ImportFile::getImportScope)
                .setHeader("Scope");
        importsGrid.addColumn(ImportFile::getAccountNumber)
                .setHeader("Account number");

        importsGrid.getColumns().forEach(column -> column.setAutoWidth(Boolean.TRUE));
    }

    private void updateImportsGrid() {
        authService.authenticate(this, password -> {
            final List<ImportFile> imports = importService.getFilesToImport(password);

            final Map<Boolean, List<ImportFile>> importsByImportable = imports.stream()
                    .collect(Collectors.groupingBy(ImportFile::isImportable));

            if (importsByImportable.containsKey(Boolean.TRUE)
                    && !importsByImportable.get(Boolean.TRUE).isEmpty()) {
                importsGrid.setItems(importsByImportable.get(Boolean.TRUE));
                importButton.setEnabled(Boolean.TRUE);
            } else {
                importsGrid.setItems(Collections.emptyList());
                importButton.setEnabled(Boolean.FALSE);
            }

            if (importsByImportable.containsKey(Boolean.FALSE)
                    && !importsByImportable.get(Boolean.FALSE).isEmpty()) {
                final String message = importsByImportable.get(Boolean.FALSE).stream()
                        .map(ImportFile::getFile)
                        .map(File::getName)
                        .collect(Collectors.joining(", ", "The following files are not importable: ", ""));

                Notification.show(message, 10000, Notification.Position.BOTTOM_START);
            }
        });
    }
}
