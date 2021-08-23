package com.diegocastroviadero.financemanager.app.views.administration;

import com.diegocastroviadero.financemanager.app.services.AuthCleanerService;
import com.diegocastroviadero.financemanager.app.services.CacheCleanerService;
import com.diegocastroviadero.financemanager.app.services.UserConfigService;
import com.diegocastroviadero.financemanager.app.views.main.MainView;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Route(value = "administration", layout = MainView.class)
@PageTitle("Administration | Finance Manager")
public class Administration extends VerticalLayout {

    public Administration(final UserConfigService userConfigService, final AuthCleanerService authCleanerService, final CacheCleanerService cacheCleanerService) {
        addClassName("administration-view");
        setSizeFull();

        final TextField version = new TextField("Version");
        version.setReadOnly(Boolean.TRUE);
        version.setWidthFull();
        version.setValue(userConfigService.getVersionLabel());

        final VerticalLayout versionLayout = new VerticalLayout(version);
        versionLayout.setWidthFull();

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
        final VerticalLayout cleanLayout = new VerticalLayout(authCacheLayout, globalCacheLayout, cleanAllLayout);
        cleanLayout.setWidthFull();

        final Checkbox demoMode = new Checkbox("Demo Mode", userConfigService.isDemoMode());
        demoMode.addValueChangeListener(e -> userConfigService.setDemoMode(e.getValue()));

        final VerticalLayout demoModeLayout = new VerticalLayout(demoMode);
        demoModeLayout.setWidthFull();

        final VerticalLayout content = new VerticalLayout(versionLayout, cleanLayout, demoModeLayout);
        content.addClassName("content");
        content.setSizeFull();

        add(content);
    }
}
