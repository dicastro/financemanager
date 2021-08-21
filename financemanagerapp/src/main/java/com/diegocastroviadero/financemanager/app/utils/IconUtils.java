package com.diegocastroviadero.financemanager.app.utils;

import com.diegocastroviadero.financemanager.app.model.*;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

public final class IconUtils {
    private IconUtils() {}

    public static Image getBankIcon(final Account account) {
        return getBankIcon(account.getBank());
    }

    public static Image getBankIcon(final ImportFile importFile) {
        return getBankIcon(importFile.getBank());
    }

    public static Image getBankIcon(final AccountPosition accountPosition) {
        return getBankIcon(accountPosition.getBank());
    }

    public static Image getBankIcon(final Bank bank) {
        final Image bankIcon = new Image(String.format("images/bank_logo_%s.svg", bank.name()), bank.name());
        bankIcon.setWidth(1.0f, Unit.EM);

        return bankIcon;
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
        final Icon scopeIcon;

        if (scope == Scope.PERSONAL) {
            scopeIcon = new Icon(VaadinIcon.MALE);
        } else {
            scopeIcon = new Icon(VaadinIcon.GROUP);
        }

        return scopeIcon;
    }

    public static Icon getPurposeIcon(final Account account) {
        return getPurposeIcon(account.getPurpose());
    }

    public static Icon getPurposeIcon(final AccountPurpose purpose) {
        final Icon purposeIcon;

        if (purpose == AccountPurpose.CREDIT) {
            purposeIcon = VaadinIcon.CREDIT_CARD.create();
        } else if (purpose == AccountPurpose.SAVINGS) {
            purposeIcon = VaadinIcon.PIGGY_BANK.create();
        } else {
            purposeIcon = VaadinIcon.CASH.create();
        }

        return purposeIcon;
    }
}
