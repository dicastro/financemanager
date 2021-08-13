package com.diegocastroviadero.financemanager.app.views.administration;

import com.diegocastroviadero.financemanager.app.services.CacheCleanerService;
import com.diegocastroviadero.financemanager.app.services.UserConfigService;
import com.diegocastroviadero.financemanager.app.views.main.MainView;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.H1;
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

    public Administration(final UserConfigService userConfigService, final CacheCleanerService cacheCleanerService) {
        addClassName("administration-view");
        setSizeFull();

        add(new H1("Administration"));

        final TextField version = new TextField("Version");
        version.setReadOnly(Boolean.TRUE);
        version.setWidthFull();
        version.setValue(userConfigService.getVersionLabel());

        final VerticalLayout versionLayout = new VerticalLayout(version);
        versionLayout.setWidthFull();

        final TextField cacheStatus = new TextField("Cache Status");
        cacheStatus.setReadOnly(Boolean.TRUE);
        cacheStatus.setWidthFull();
        cacheStatus.setValue(cacheCleanerService.getCacheStatusLabel());

        final Button invalidateCacheButton = new Button("Invalidate cache");
        invalidateCacheButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        invalidateCacheButton.addClickListener(event -> {
            cacheCleanerService.invalidateCache();
            cacheStatus.setValue(cacheCleanerService.getCacheStatusLabel());
        });

        final VerticalLayout cacheLayout = new VerticalLayout(cacheStatus, invalidateCacheButton);
        cacheLayout.setWidthFull();
        cacheLayout.setDefaultHorizontalComponentAlignment(Alignment.END);

        final Checkbox demoMode = new Checkbox("Demo Mode", userConfigService.isDemoMode());
        demoMode.addValueChangeListener(e -> userConfigService.setDemoMode(e.getValue()));

        final VerticalLayout demoModeLayout = new VerticalLayout(demoMode);
        demoModeLayout.setWidthFull();

        final VerticalLayout content = new VerticalLayout(versionLayout, cacheLayout, demoModeLayout);
        content.addClassName("content");
        content.setSizeFull();

        add(content);
    }
}
