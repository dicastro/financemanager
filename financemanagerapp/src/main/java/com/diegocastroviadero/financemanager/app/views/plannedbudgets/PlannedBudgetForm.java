package com.diegocastroviadero.financemanager.app.views.plannedbudgets;

import com.diegocastroviadero.financemanager.app.model.PlannedBudget;
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
public class PlannedBudgetForm extends FormLayout {
     private PlannedBudget budget;

    final TextField concept = new TextField("Concept");
    final ComboBox<Scope> scope = new ComboBox<>("Scope");
    final ComboBox<Month> month = new ComboBox<>("Month");
    final BigDecimalField quantity = new BigDecimalField("Quantity");

    final Button save = new Button("Save");
    final Button delete = new Button("Delete");
    final Button close = new Button("Close");

    final List<ShortcutRegistration> shortcuts = new ArrayList<>();

    final Binder<PlannedBudget> binder = new BeanValidationBinder<>(PlannedBudget.class);

    public PlannedBudgetForm(final Scope[] scopes, final Month[] months) {
        addClassName("planned-budget-form");

        binder.bindInstanceFields(this);

        quantity.addThemeVariants(TextFieldVariant.LUMO_ALIGN_RIGHT);
        quantity.setPrefixComponent(new Icon(VaadinIcon.EURO));

        scope.setItems(scopes);
        scope.setItemLabelGenerator(Scope::name);

        month.setItems(months);
        month.setItemLabelGenerator(Month::name);

        add(concept, scope, month, quantity, createButtonsLayout());
    }

    public void show(final PlannedBudget budget) {
        setBudget(budget);

        shortcuts.add(save.addClickShortcut(Key.ENTER));
        shortcuts.add(close.addClickShortcut(Key.ESCAPE));
        shortcuts.add(delete.addClickShortcut(Key.DELETE));

        setVisible(Boolean.TRUE);
    }

    public void hide() {
        setBudget(null);

        shortcuts.forEach(ShortcutRegistration::remove);
        shortcuts.clear();

        setVisible(Boolean.FALSE);
    }

    public void setBudget(final PlannedBudget budget) {
        this.budget = budget;
        binder.readBean(budget);
    }

    private HorizontalLayout createButtonsLayout() {
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        save.addClickListener(event -> validateAndSave());

        delete.addClickListener(event -> fireEvent(new DeleteEvent(this, budget)));

        close.addClickListener(event -> fireEvent(new CloseEvent(this)));

        binder.addStatusChangeListener(e -> save.setEnabled(binder.isValid()));

        return new HorizontalLayout(save, delete, close);
    }

    private void validateAndSave() {
        try {
            binder.writeBean(budget);
            fireEvent(new SaveEvent(this, budget));
        } catch (ValidationException e) {
            log.error("Expense could not be saved because form is not valid", e);
        }
    }

    @Override
    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType, ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }

    @Getter
    public static abstract class PlannedBudgetFormEvent extends ComponentEvent<PlannedBudgetForm> {
        private final PlannedBudget budget;

        protected PlannedBudgetFormEvent(PlannedBudgetForm source, PlannedBudget budget) {
            super(source, false);
            this.budget = budget;
        }
    }

    public static class SaveEvent extends PlannedBudgetFormEvent {
        SaveEvent(PlannedBudgetForm source, PlannedBudget budget) {
            super(source, budget);
        }
    }

    public static class DeleteEvent extends PlannedBudgetFormEvent {
        DeleteEvent(PlannedBudgetForm source, PlannedBudget budget) {
            super(source, budget);
        }
    }

    public static class CloseEvent extends PlannedBudgetFormEvent {
        CloseEvent(PlannedBudgetForm source) {
            super(source, null);
        }
    }
}
