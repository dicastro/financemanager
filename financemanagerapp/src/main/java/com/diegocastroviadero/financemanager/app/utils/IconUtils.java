package com.diegocastroviadero.financemanager.app.utils;

import com.diegocastroviadero.financemanager.app.model.Account;
import com.diegocastroviadero.financemanager.app.model.AccountPurpose;
import com.diegocastroviadero.financemanager.app.model.Scope;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

public final class IconUtils {
    private IconUtils() {}

    public static Image getBankIcon(final Account account) {
        final Image bankIcon = new Image(String.format("images/bank_logo_%s.svg", account.getBank().name()), account.getBank().name());
        bankIcon.setWidth(1.0f, Unit.EM);

        return bankIcon;
    }

    public static Icon getScopeIcon(final Account account) {
        final Icon scopeIcon;

        if (account.getScope() == Scope.PERSONAL) {
            scopeIcon = new Icon(VaadinIcon.MALE);
        } else {
            scopeIcon = new Icon(VaadinIcon.GROUP);
        }

        return scopeIcon;
    }

    public static Icon getPurposeIcon(final Account account) {
        final Icon purposeIcon;

        if (account.getPurpose() == AccountPurpose.CREDIT) {
            purposeIcon = VaadinIcon.CREDIT_CARD.create();
        } else if (account.getPurpose() == AccountPurpose.SAVINGS) {
            purposeIcon = VaadinIcon.PIGGY_BANK.create();
        } else {
            purposeIcon = VaadinIcon.CASH.create();
        }

        return purposeIcon;
    }
}
