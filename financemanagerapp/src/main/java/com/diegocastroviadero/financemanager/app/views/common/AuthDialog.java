package com.diegocastroviadero.financemanager.app.views.common;

import com.diegocastroviadero.financemanager.app.model.Authentication;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.ShortcutRegistration;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
public class AuthDialog extends Dialog {
    private Authentication authentication;

    final PasswordField password = new PasswordField("Password");
    final Button accept = new Button("Accept");

    private final List<ShortcutRegistration> shortcuts = new ArrayList<>();

    private Registration onClosedRegistration;

    final Binder<Authentication> binder = new BeanValidationBinder<>(Authentication.class);

    public AuthDialog() {
        binder.forField(password)
                .asRequired("Password is required")
                .bind(Authentication::getPassword, Authentication::setPassword);

        setCloseOnEsc(Boolean.FALSE);
        setCloseOnOutsideClick(Boolean.FALSE);

        getElement()
                .setAttribute("aria-label", "Enter cypher password");

        final H2 title = new H2("Enter cypher password");
        title.getStyle()
                .set("margin", "var(--lumo-space-m) 0 0 0")
                .set("font-size", "1.5em").set("font-weight", "bold");

        password.setAutofocus(Boolean.TRUE);

        final VerticalLayout fieldsLayout = new VerticalLayout(password);
        fieldsLayout.setSpacing(Boolean.FALSE);
        fieldsLayout.setPadding(Boolean.FALSE);
        fieldsLayout.setAlignItems(FlexComponent.Alignment.STRETCH);

        accept.addClickListener(event -> {
            try {
                binder.writeBean(authentication);

                close();
            } catch (ValidationException e) {
                log.error("Authentication dialog could not be closed because form is not valid");
            }
        });
        accept.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        final HorizontalLayout buttonsLayout = new HorizontalLayout(accept);
        buttonsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        final VerticalLayout dialogLayout = new VerticalLayout(title, fieldsLayout, buttonsLayout);
        dialogLayout.setPadding(Boolean.FALSE);
        dialogLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
        dialogLayout.getStyle()
                .set("width", "300px")
                .set("max-width", "100%");

        add(dialogLayout);
    }

    public void open() {
        shortcuts.add(accept.addClickShortcut(Key.ENTER));

        authentication = Authentication.builder().build();
        binder.readBean(authentication);

        super.open();
    }

    public void setOnClosedListener(final Consumer<String> closeListener) {
        if (null != onClosedRegistration) {
            onClosedRegistration.remove();
        }

        onClosedRegistration = addOpenedChangeListener(changedEvent -> {
            if (!changedEvent.isOpened()) {
                shortcuts.forEach(ShortcutRegistration::remove);
                shortcuts.clear();

                closeListener.accept(authentication.getPassword());
            }
        });
    }
}
