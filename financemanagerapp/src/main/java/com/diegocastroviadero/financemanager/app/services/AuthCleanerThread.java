package com.diegocastroviadero.financemanager.app.services;

import com.vaadin.flow.server.VaadinSession;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class AuthCleanerThread extends Thread {

    private final Long cleanInterval;
    private final AuthCleanerService authCleanerService;
    private final VaadinSession session;
    private final String sessionId;

    public AuthCleanerThread(final Long cleanInterval, final AuthCleanerService authCleanerService, final VaadinSession session) {
        this.cleanInterval = cleanInterval;
        this.authCleanerService = authCleanerService;
        this.session = session;
        this.sessionId = session.getSession().getId();
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
            log.error("Clean process of session '{}' has been interrupted!", sessionId);
        }
    }
}
