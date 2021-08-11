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
        synchronized (cache) {
            cache.clear();
        }
    }

    public <T> T getIfPresent(final String key, final Class<T> clazz) {
        T result = null;

        synchronized (cache) {
            if (cache.containsKey(key)) {
                result = clazz.cast(cache.get(key));
            }
        }

        return result;
    }

    public <T> T putIfAbsent(final String key, final Supplier<T> function) {
        if (cache.containsKey(key)) {
            return (T) cache.get(key);
        } else {
            final T data = function.get();

            cache.put(key, data);

            return data;
        }
    }

    public void put(final String key, final Object value) {
        synchronized (cache) {
            cache.put(key, value);
        }
    }

    public void invalidate(final String key) {
        synchronized (cache) {
            cache.remove(key);
        }
    }
}
