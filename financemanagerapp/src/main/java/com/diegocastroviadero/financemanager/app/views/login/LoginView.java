package com.diegocastroviadero.financemanager.app.views.login;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.Collections;

@Tag("sa-login-view")
@Route(value = LoginView.ROUTE)
@PageTitle("Login | Finance Manager")
public class LoginView extends VerticalLayout implements BeforeEnterObserver {
    public static final String ROUTE = "login";

    private final LoginOverlay login = new LoginOverlay();

    private final LoginI18n CREDENTIALS_ERROR = LoginI18n.createDefault();
    private final LoginI18n BLOCKED_ERROR = LoginI18n.createDefault();

    public LoginView() {
        BLOCKED_ERROR.getErrorMessage().setTitle("Blocked");
        BLOCKED_ERROR.getErrorMessage().setMessage("You have been blocked");

        final Image titleLogo = new Image("images/logo.png", "Finance Manager logo");
        titleLogo.setWidth(3, Unit.EM);

        final H1 titleText = new H1("Finance Manager");
        titleText.getStyle().set("color", "var(--lumo-base-color)");
        titleText.getStyle().set("margin", "0 var(--lumo-space-m)");

        final HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        titleLayout.add(titleLogo);
        titleLayout.add(titleText);

        login.setAction("login");
        login.setTitle(titleLayout);
        login.setDescription(null);
        login.setForgotPasswordButtonVisible(false);
        login.setOpened(true);

        getElement().appendChild(login.getElement());
    }

    @Override
    public void beforeEnter(final BeforeEnterEvent event) {
        // inform the user about an authentication error
        if (queryParameterContains(event, "error")) {
            setLoginError(CREDENTIALS_ERROR);
        // inform the user he has been blocked
        } else if (queryParameterContains(event, "blocked")) {
            setLoginError(BLOCKED_ERROR);
        }
    }

    private boolean queryParameterContains(final BeforeEvent event, final String parameterName) {
        return !event.getLocation().getQueryParameters().getParameters().getOrDefault(parameterName, Collections.emptyList()).isEmpty();
    }

    private void setLoginError(final LoginI18n error) {
        login.setI18n(error);
        login.setError(true);
    }
}
