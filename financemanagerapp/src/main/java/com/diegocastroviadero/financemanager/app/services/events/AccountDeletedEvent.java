package com.diegocastroviadero.financemanager.app.services.events;

import com.diegocastroviadero.financemanager.app.model.Account;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class AccountDeletedEvent extends ApplicationEvent {
    private final Account account;

    public AccountDeletedEvent(final Object source, final Account account) {
        super(source);
        this.account = account;
    }
}
