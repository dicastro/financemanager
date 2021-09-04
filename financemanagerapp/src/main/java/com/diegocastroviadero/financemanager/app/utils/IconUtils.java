package com.diegocastroviadero.financemanager.app.utils;

import com.diegocastroviadero.financemanager.app.model.*;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

public final class IconUtils {
    private IconUtils() {
    }

    public static Image getBankIcon(final Account account) {
        return getBankIcon(account.getBank());
    }

    public static Image getBankIcon(final AccountPosition accountPosition) {
        return getBankIcon(accountPosition.getBank());
    }

    public static Image getBankIcon(final Bank bank) {
        final String bankName = bank == null ? "UNKNOWN" : bank.name();

        final Image icon = new Image(String.format("images/bank_logo_%s.svg", bankName), bankName);
        icon.setMaxWidth("var(--iron-icon-width, 24px)");
        icon.setMaxHeight("var(--iron-icon-width, 24px)");

        return icon;
    }

    public static Icon getScopeIcon(final Account account) {
        return getScopeIcon(account.getScope());
    }

    public static Icon getScopeIcon(final PlannedBudget budget) {
        return getScopeIcon(budget.getScope());
    }

    public static Icon getScopeIcon(final PlannedExpense expense) {
        return getScopeIcon(expense.getScope());
    }

    public static Icon getScopeIcon(final AccountPosition accountPosition) {
        return getScopeIcon(accountPosition.getScope());
    }

    public static Icon getScopeIcon(final Scope scope) {
        final VaadinIcon scopeIcon;

        if (scope == Scope.PERSONAL) {
            scopeIcon = VaadinIcon.MALE;
        } else if (scope == Scope.SHARED) {
            scopeIcon = VaadinIcon.GROUP;
        } else {
            scopeIcon = VaadinIcon.MINUS;
        }

        return scopeIcon.create();
    }

    public static Image getPurposeIcon(final Account account) {
        return getPurposeIcon(account.getPurpose());
    }

    public static Image getPurposeIcon(final AccountPurpose purpose) {
        final Image icon = new Image(String.format("images/account_purpose_logo_%s.svg", purpose.name()), purpose.name());
        icon.setMaxWidth("var(--iron-icon-width, 24px)");
        icon.setMaxHeight("var(--iron-icon-width, 24px)");

        return icon;
    }
}
