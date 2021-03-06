package com.diegocastroviadero.financemanager.app.services;

import com.diegocastroviadero.financemanager.app.configuration.CacheProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CacheCleanerService {
    private final CacheProperties cacheProperties;
    private final CacheService cacheService;

    private Long lastClean;
    private Long lastScheduledClean;

    public CacheCleanerService(final CacheProperties cacheProperties, final CacheService cacheService) {
        this.cacheProperties = cacheProperties;
        this.cacheService = cacheService;
    }

    @Scheduled(fixedRateString = "${financemanagerapp.cache.clean-interval}", initialDelayString = "${financemanagerapp.cache.clean-interval}")
    public void cleanCacheScheduled() {
        cleanCache(true);
    }

    public void cleanCache() {
        cleanCache(false);
    }

    public void cleanCache(final boolean scheduled) {
        log.debug("{}Cleaning cache ...", scheduled ? "(scheduled) " : "");

        cacheService.clearCache();

        log.info("{}Cache was cleaned successfully", scheduled ? "(scheduled) " : "");

        final long now = System.currentTimeMillis();

        lastClean = now;

        if (scheduled) {
            lastScheduledClean = now;
        }
    }

    public String getCacheStatusLabel() {
        final String firstPart;
        if (null == lastClean) {
            firstPart = "Global cache was never cleaned";
        } else {
            final long elapsedFromLastClean = System.currentTimeMillis() - lastClean;

            firstPart = String.format("Global cache was cleaned %d millis ago", elapsedFromLastClean);
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
