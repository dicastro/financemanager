package com.diegocastroviadero.financemanager.app.views.common;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class ConfirmationDialog extends Dialog {

    private final H4 title = new H4();
    private final Label question = new Label();

    private final Map<String, Object> context = new HashMap<>();

    public ConfirmationDialog(final String title) {
        this(title, "");
    }

    public ConfirmationDialog(final String title, final String content) {
        setCloseOnOutsideClick(Boolean.FALSE);
        setCloseOnEsc(Boolean.FALSE);

        createHeader(title);
        createContent(content);
        createFooter();
    }

    private void createHeader(final String title) {
        this.title.setText(title);

        final HorizontalLayout layout = new HorizontalLayout(this.title);
        layout.setWidthFull();

        add(layout);
    }

    private void createContent(final String content) {
        this.question.setText(content);

        final VerticalLayout layout = new VerticalLayout();
        layout.add(question);
        layout.setPadding(Boolean.FALSE);
        layout.getStyle()
                .set("padding", "var(--lumo-space-m) 0");

        add(layout);
    }

    private void createFooter() {
        final Button cancel = new Button("Cancel");
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        cancel.addClickListener(event -> {
            close();
            fireEvent(new CancelledEvent(this, context));
            clearContext();
        });

        final Button confirm = new Button("Confirm");
        confirm.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        confirm.addClickListener(event -> {
            close();
            fireEvent(new ConfirmedEvent(this, context));
            clearContext();
        });

        final HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        layout.setFlexGrow(1.0, cancel);
        layout.setFlexGrow(1.0, confirm);

        layout.add(cancel, confirm);

        add(layout);
    }

    public void clearContext() {
        this.context.clear();
    }

    public void open(final String content) {
        this.question.setText(content);

        super.open();
    }

    public void open(final String content, final Map<String, Object> context) {
        this.question.setText(content);
        this.context.putAll(context);

        super.open();
    }

    @Override
    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType, ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }

    @Getter
    public static abstract class ConfirmationDialogEvent extends ComponentEvent<ConfirmationDialog> {
        private final Map<String, Object> context;

        protected ConfirmationDialogEvent(final ConfirmationDialog source, final Map<String, Object> context) {
            super(source, false);
            this.context = context;
        }
    }

    public static class ConfirmedEvent extends ConfirmationDialogEvent {
        ConfirmedEvent(final ConfirmationDialog source, final Map<String, Object> context) {
            super(source, context);
        }
    }

    public static class CancelledEvent extends ConfirmationDialogEvent {
        CancelledEvent(final ConfirmationDialog source, final Map<String, Object> context) {
            super(source, context);
        }
    }
}
