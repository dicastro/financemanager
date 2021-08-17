package com.diegocastroviadero.financemanager.app.services;

import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.VaadinSessionState;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class AuthCleanerThread extends Thread {

    private final Long cleanInterval;
    private final AuthCleanerService authCleanerService;
    private final VaadinSession session;

    public AuthCleanerThread(final Long cleanInterval, final AuthCleanerService authCleanerService, final VaadinSession session) {
        this.cleanInterval = cleanInterval;
        this.authCleanerService = authCleanerService;
        this.session = session;
    }

    @Override
    public void run() {
        try {
            final AtomicBoolean sessionOpened = new AtomicBoolean(true);

            while (sessionOpened.get()) {
                session.access(() -> {
                    sessionOpened.set(null != session.getSession().getId());

                    if (sessionOpened.get()) {
                        authCleanerService.cleanAuthScheduled(session);
                    } else {
                        log.warn("Found session without id, stopped clean process");
                    }
                });

                if (sessionOpened.get()) {
                    Thread.sleep(cleanInterval);
                }
            }
        } catch (InterruptedException e) {
            session.access(() -> log.error("Clean process of session '{}' has been interrupted!", session.getSession().getId()));
        }
    }
}
