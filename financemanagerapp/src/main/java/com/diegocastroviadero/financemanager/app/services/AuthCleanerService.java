package com.diegocastroviadero.financemanager.app.services;

import com.diegocastroviadero.financemanager.app.configuration.CacheProperties;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.WrappedSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthCleanerService {
    private final CacheProperties cacheProperties;
    private final AuthService authService;

    private Long lastClean;
    private Long lastScheduledClean;

    public AuthCleanerService(final CacheProperties cacheProperties, final AuthService authService) {
        this.cacheProperties = cacheProperties;
        this.authService = authService;
    }

    public void cleanAuthScheduled(final VaadinSession session) {
        cleanAuth(true, session);
    }

    public void cleanAuth() {
        cleanAuth(false, VaadinSession.getCurrent());
    }

    public void cleanAuth(final boolean scheduled, final VaadinSession vaadinSession) {
        final WrappedSession session = vaadinSession.getSession();

        log.debug("{}Cleaning auth in session '{}' ...", scheduled ? "(scheduled) " : "", session.getId());

        authService.forgetPassword(vaadinSession);

        log.info("{}Auth in session '{}' was cleaned successfully", scheduled ? "(scheduled) " : "", session.getId());

        final long now = System.currentTimeMillis();

        lastClean = now;

        if (scheduled) {
            lastScheduledClean = now;
        }
    }

    public String getAuthStatusLabel() {
        final String firstPart;
        if (null == lastClean) {
            firstPart = "Auth was never cleaned";
        } else {
            final long elapsedFromLastClean = System.currentTimeMillis() - lastClean;

            firstPart = String.format("Auth was cleaned %d millis ago", elapsedFromLastClean);
        }

        final String secondPart;
        if (null == lastScheduledClean) {
            secondPart = String.format("(scheduled each %d millis)", cacheProperties.getCleanInterval());
        } else {
            final long elapsedFromLastScheduledClean = System.currentTimeMillis() - lastScheduledClean;

            secondPart = String.format("(next clean in %d millis)", cacheProperties.getCleanInterval() - elapsedFromLastScheduledClean);
        }

        return String.format("%s %s", firstPart, secondPart);
    }
}
