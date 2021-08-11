package com.diegocastroviadero.financemanager.app.services;

import com.diegocastroviadero.financemanager.app.configuration.PasswordCacheExpirationProperties;
import com.diegocastroviadero.financemanager.app.views.common.AuthDialog;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.vaadin.flow.component.Component;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Service
@Slf4j
public class AuthService {
    private static final String PASSWORD_CACHE_ENTRY_KEY = "password";

    private final Map<Class<? extends Component>, AuthDialog> map = new HashMap<>();
    private final Cache<String, char[]> passwordCache;

    public AuthService(final PasswordCacheExpirationProperties properties) {
        passwordCache = Caffeine.newBuilder()
                .expireAfterWrite(properties.getQuantity(), properties.getUnit())
                .maximumSize(1)
                .removalListener((k, v, cause) -> log.debug("Key '{}' has been removed (cause: {})", k, cause))
                .build();
    }

    public AuthDialog configureAuth(final Component component) {
        final AuthDialog authDialog = new AuthDialog();
        map.put(component.getClass(), authDialog);

        return authDialog;
    }

    public void authenticate(final Component component, final Consumer<char[]> listener) {
        char[] pass = passwordCache.getIfPresent(PASSWORD_CACHE_ENTRY_KEY);

        if (pass != null) {
            listener.accept(pass);
        } else {
            if (map.containsKey(component.getClass())) {
                final AuthDialog authDialog = map.get(component.getClass());

                authDialog.setOnClosedListener(password -> {
                    final char[] p = password.toCharArray();

                    passwordCache.put(PASSWORD_CACHE_ENTRY_KEY, p);
                    listener.accept(p);
                });

                authDialog.open();
            } else {
                // TODO: manage this case in a better way
                throw new RuntimeException(String.format("AuthService was not configured for %s", component.getClass().getSimpleName()));
            }
        }
    }

    public void forgetPassword() {
        passwordCache.invalidate(PASSWORD_CACHE_ENTRY_KEY);
    }
}
