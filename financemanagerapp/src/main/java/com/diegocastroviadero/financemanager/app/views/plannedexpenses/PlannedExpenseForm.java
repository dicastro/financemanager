package com.diegocastroviadero.financemanager.app.views.plannedexpenses;

import com.diegocastroviadero.financemanager.app.model.PlannedExpense;
import com.diegocastroviadero.financemanager.app.model.Scope;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.ShortcutRegistration;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Month;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class PlannedExpenseForm extends FormLayout {
    private PlannedExpense expense;

    final TextField concept = new TextField("Concept");
    final ComboBox<Scope> scope = new ComboBox<>("Scope");
    final ComboBox<Month> month = new ComboBox<>("Month");
    final BigDecimalField quantity = new BigDecimalField("Quantity");

    final Button save = new Button("Save");
    final Button delete = new Button("Delete");
    final Button close = new Button("Close");

    final List<ShortcutRegistration> shortcuts = new ArrayList<>();

    final Binder<PlannedExpense> binder = new BeanValidationBinder<>(PlannedExpense.class);

    public PlannedExpenseForm(final Scope[] scopes, final Month[] months) {
        addClassName("planned-expense-form");

        binder.bindInstanceFields(this);

        quantity.addThemeVariants(TextFieldVariant.LUMO_ALIGN_RIGHT);
        quantity.setPrefixComponent(new Icon(VaadinIcon.EURO));

        scope.setItems(scopes);
        scope.setItemLabelGenerator(Scope::name);

        month.setItems(months);
        month.setItemLabelGenerator(Month::name);

        add(concept, scope, month, quantity, createButtonsLayout());
    }

    public void show(final PlannedExpense plannedExpense) {
        setExpense(plannedExpense);

        shortcuts.add(save.addClickShortcut(Key.ENTER));
        shortcuts.add(close.addClickShortcut(Key.ESCAPE));
        shortcuts.add(delete.addClickShortcut(Key.DELETE));

        setVisible(Boolean.TRUE);
    }

    public void hide() {
        setExpense(null);

        shortcuts.forEach(ShortcutRegistration::remove);
        shortcuts.clear();

        setVisible(Boolean.FALSE);
    }

    public void setExpense(final PlannedExpense expense) {
        this.expense = expense;
        binder.readBean(expense);
    }

    private HorizontalLayout createButtonsLayout() {
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        save.addClickListener(event -> validateAndSave());

        delete.addClickListener(event -> fireEvent(new DeleteEvent(this, expense)));

        close.addClickListener(event -> fireEvent(new CloseEvent(this)));

        binder.addStatusChangeListener(e -> save.setEnabled(binder.isValid()));

        return new HorizontalLayout(save, delete, close);
    }

    private void validateAndSave() {
        try {
            binder.writeBean(expense);
            fireEvent(new SaveEvent(this, expense));
        } catch (ValidationException e) {
            log.error("Expense could not be saved because form is not valid", e);
        }
    }

    @Override
    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType, ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }

    @Getter
    public static abstract class PlannedExpenseFormEvent extends ComponentEvent<PlannedExpenseForm> {
        private final PlannedExpense expense;

        protected PlannedExpenseFormEvent(PlannedExpenseForm source, PlannedExpense expense) {
            super(source, false);
            this.expense = expense;
        }
    }

    public static class SaveEvent extends PlannedExpenseFormEvent {
        SaveEvent(PlannedExpenseForm source, PlannedExpense expense) {
            super(source, expense);
        }
    }

    public static class DeleteEvent extends PlannedExpenseFormEvent {
        DeleteEvent(PlannedExpenseForm source, PlannedExpense expense) {
            super(source, expense);
        }
    }

    public static class CloseEvent extends PlannedExpenseFormEvent {
        CloseEvent(PlannedExpenseForm source) {
            super(source, null);
        }
    }
}
