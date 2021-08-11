package com.diegocastroviadero.financemanager.app.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class CacheCleanerService {
    private final CacheService cacheService;

    @Scheduled(fixedRateString = "${financemanagerapp.password-cache-expiration-millis}", initialDelayString = "${financemanagerapp.password-cache-expiration-millis}")
    public void cleanCache() {
        log.debug("Cleaning all cache entries ...");

        cacheService.clearCache();

        log.info("All cache entries were cleaned successfully");
    }
}
