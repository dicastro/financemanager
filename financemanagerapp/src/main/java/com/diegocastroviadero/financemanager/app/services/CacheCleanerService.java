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

    public CacheCleanerService(final CacheProperties cacheProperties, final CacheService cacheService) {
        this.cacheProperties = cacheProperties;
        this.cacheService = cacheService;
    }

    @Scheduled(fixedRateString = "${financemanagerapp.cache.expiration-millis}", initialDelayString = "${financemanagerapp.cache.expiration-millis}")
    public void invalidateCacheScheduled() {
        invalidateCache();
    }

    public void invalidateCache() {
        log.debug("Cleaning all cache entries ...");

        cacheService.clearCache();

        lastClean = System.currentTimeMillis();

        log.info("All cache entries were cleaned successfully");
    }

    public String getCacheStatusLabel() {
        final String label;

        if (null == lastClean) {
            label = String.format("Cache was never cleaned (scheduled each %d millis)", cacheProperties.getExpirationMillis());
        } else {
            final long elapsedFromLastClean = System.currentTimeMillis() - lastClean;

            label = String.format("Last cache clean was %d millis ago (next clean in %d millis)", elapsedFromLastClean, cacheProperties.getExpirationMillis() - elapsedFromLastClean);
        }

        return label;
    }
}
