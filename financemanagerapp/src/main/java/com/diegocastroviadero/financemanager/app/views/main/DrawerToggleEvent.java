package com.diegocastroviadero.financemanager.app.views.main;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class DrawerToggleEvent extends ApplicationEvent {
    private final boolean drawerOpened;

    public DrawerToggleEvent(final Object source, final boolean drawerOpened) {
        super(source);
        this.drawerOpened = drawerOpened;
    }
}
