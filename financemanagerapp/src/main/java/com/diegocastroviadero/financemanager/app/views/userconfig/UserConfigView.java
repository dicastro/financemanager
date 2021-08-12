package com.diegocastroviadero.financemanager.app.views.userconfig;

import com.diegocastroviadero.financemanager.app.services.UserConfigService;
import com.diegocastroviadero.financemanager.app.views.main.MainView;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Route(value = "configuration", layout = MainView.class)
@PageTitle("Configuration | Finance Manager")
public class UserConfigView extends VerticalLayout {

    public UserConfigView(final UserConfigService userConfigService) {
        addClassName("configuration-view");
        setSizeFull();

        add(new H1("Configuration"));

        final Checkbox demoMode = new Checkbox("Demo Mode", userConfigService.isDemoMode());
        demoMode.addValueChangeListener(e -> userConfigService.setDemoMode(e.getValue()));

        final Div content = new Div(demoMode);
        content.addClassName("content");
        content.setSizeFull();

        add(content);
    }
}
