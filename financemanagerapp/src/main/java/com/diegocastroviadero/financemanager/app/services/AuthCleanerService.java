package com.diegocastroviadero.financemanager.app.services;

import com.diegocastroviadero.financemanager.app.configuration.CacheProperties;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.WrappedSession;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class AuthCleanerService {
    private static final String LAST_CLEAN_KEY = "lastClean";
    private static final String LAST_SCHEDULED_CLEAN_KEY = "lastScheduledClean";

    private final CacheProperties cacheProperties;
    private final AuthService authService;

    public boolean cleanAuthScheduled(final VaadinSession session) {
        return cleanAuth(true, session);
    }

    public boolean cleanAuth() {
        return cleanAuth(false, VaadinSession.getCurrent());
    }

    private boolean cleanAuth(final boolean scheduled, final VaadinSession vaadinSession) {
        final String scheduledLiteral = scheduled ? "(scheduled) " : "";

        boolean authCleaned = false;

        if (null == vaadinSession) {
            log.debug("{}Session is null, nothing to clean", scheduledLiteral);
        } else {
            final WrappedSession session = vaadinSession.getSession();

            log.debug("{}Cleaning auth in session '{}' ...", scheduledLiteral, session.getId());

            authCleaned = authService.forgetPassword(vaadinSession);

            log.info("{}Auth in session '{}' was cleaned successfully", scheduledLiteral, session.getId());

            final long now = System.currentTimeMillis();

            vaadinSession.setAttribute(LAST_CLEAN_KEY, now);

            if (scheduled) {
                vaadinSession.setAttribute(LAST_SCHEDULED_CLEAN_KEY, now);
            }
        }

        return authCleaned;
    }

    public String getAuthStatusLabel() {
        return getAuthStatusLabel(VaadinSession.getCurrent());
    }

    private String getAuthStatusLabel(final VaadinSession vaadinSession) {
        final String firstPart;
        if (null == vaadinSession.getAttribute(LAST_CLEAN_KEY)) {
            firstPart = "Auth was never cleaned";
        } else {
            final long lastClean = (long) vaadinSession.getAttribute(LAST_CLEAN_KEY);
            final long elapsedFromLastClean = System.currentTimeMillis() - lastClean;

            firstPart = String.format("Auth was cleaned %d millis ago", elapsedFromLastClean);
        }

        final String secondPart;
        if (null == vaadinSession.getAttribute(LAST_SCHEDULED_CLEAN_KEY)) {
            secondPart = String.format("(scheduled each %d millis)", cacheProperties.getCleanInterval());
        } else {
            final long lastScheduledClean = (long) vaadinSession.getAttribute(LAST_SCHEDULED_CLEAN_KEY);
            final long elapsedFromLastScheduledClean = System.currentTimeMillis() - lastScheduledClean;

            secondPart = String.format("(next clean in %d millis)", cacheProperties.getCleanInterval() - elapsedFromLastScheduledClean);
        }

        return String.format("%s %s", firstPart, secondPart);
    }
}
