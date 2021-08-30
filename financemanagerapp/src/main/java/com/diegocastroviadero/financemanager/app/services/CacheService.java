package com.diegocastroviadero.financemanager.app.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Slf4j
@Service
public class CacheService {

    private final Map<String, Object> cache = new HashMap<>();

    public void clearCache() {
        log.debug("Clearing all entries from cache ...");

        synchronized (cache) {
            cache.clear();
        }
    }

    public <T> T putIfAbsent(final String key, final Supplier<T> function) {
        T result;

        synchronized (cache) {
            if (cache.containsKey(key)) {
                result = (T) cache.get(key);
            } else {
                result = function.get();

                cache.put(key, result);
            }
        }

        return result;
    }

    public <T> T put(final String key, final T value) {
        log.debug("Storing key '{}' in cache ...", key);

        synchronized (cache) {
            return (T) cache.put(key, value);
        }
    }

    public void invalidate(final String key) {
        log.debug("Invalidating key '{}' from cache ...", key);

        synchronized (cache) {
            cache.remove(key);
        }
    }

    public void invalidateAllStartingWith(final String keyPrefix) {
        synchronized (cache) {
            cache.entrySet()
                    .removeIf(e -> e.getKey().startsWith(keyPrefix));
        }
    }
}
