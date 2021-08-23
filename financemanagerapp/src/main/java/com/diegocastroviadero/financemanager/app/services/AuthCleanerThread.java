package com.diegocastroviadero.financemanager.app.services;

import com.diegocastroviadero.financemanager.app.configuration.CacheProperties;
import com.vaadin.flow.server.VaadinSession;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class AuthCleanerThread extends Thread {

    private final CacheProperties cacheProperties;
    private final AuthCleanerService authCleanerService;
    private final VaadinSession session;
    private final String sessionId;

    private int noopCleans = 0;

    public AuthCleanerThread(final CacheProperties cacheProperties, final AuthCleanerService authCleanerService, final VaadinSession session) {
        this.cacheProperties = cacheProperties;
        this.authCleanerService = authCleanerService;
        this.session = session;
        this.sessionId = session.getSession().getId();
    }

    @Override
    public void run() {
        try {
            final AtomicBoolean sessionOpened = new AtomicBoolean(true);

            while (sessionOpened.get()) {
                if (sessionOpened.get()) {
                    Thread.sleep(cacheProperties.getCleanInterval());
                }

                session.access(() -> {
                    sessionOpened.set(null != session.getSession() && null != session.getSession().getId());

                    if (sessionOpened.get()) {
                        final boolean authCleaned = authCleanerService.cleanAuthScheduled(session);

                        if (!authCleaned) {
                            noopCleans++;
                        }

                        if (noopCleans > cacheProperties.getNoopCleansToInvalidateSession()) {
                            session.getSession().invalidate();

                            log.info("(scheduled) Session '{}' has been invalidated after {} noop cleans", sessionId, cacheProperties.getNoopCleansToInvalidateSession());

                            sessionOpened.set(false);
                        }
                    } else {
                        log.warn("Found session without id, stopped clean process");
                    }
                });
            }
        } catch (InterruptedException e) {
            log.error("Clean process of session '{}' has been interrupted!", sessionId);
        }
    }
}
