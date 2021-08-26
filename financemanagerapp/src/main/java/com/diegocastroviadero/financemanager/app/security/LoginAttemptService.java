package com.diegocastroviadero.financemanager.app.security;

import com.diegocastroviadero.financemanager.app.configuration.SecurityProperties;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
public class LoginAttemptService {
    private final Duration BLOCK_DURATION = Duration.of(1, ChronoUnit.DAYS);

    private final SecurityProperties securityProperties;
    private final LoadingCache<String, Integer> attemptsCache;

    public LoginAttemptService(final SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
        this.attemptsCache = CacheBuilder.newBuilder()
                .expireAfterWrite(BLOCK_DURATION)
                .build(new CacheLoader<>() {
                    public Integer load(final String key) {
                        return 0;
                    }
                });
    }

    public void loginSucceeded(final String key) {
        attemptsCache.invalidate(key);
    }

    public void loginFailed(final String key) {
        int attempts;

        try {
            attempts = attemptsCache.get(key);
        } catch (ExecutionException e) {
            attempts = 0;
        }

        attempts++;
        attemptsCache.put(key, attempts);

        if (blocked(attempts)) {
            log.warn("'{}' has been blocked during '{}' (until {})", key, BLOCK_DURATION, LocalDateTime.now().plus(BLOCK_DURATION).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss.SSS")));
        }
    }

    public boolean isBlocked(final String key) {
        try {
            return blocked(attemptsCache.get(key));
        } catch (ExecutionException e) {
            return false;
        }
    }

    public boolean blocked(final Integer attempts) {
        return attempts >= securityProperties.getIncorrectLoginsToBlockIp();
    }
}
