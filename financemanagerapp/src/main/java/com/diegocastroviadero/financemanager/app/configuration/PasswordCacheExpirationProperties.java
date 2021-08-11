package com.diegocastroviadero.financemanager.app.configuration;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
@NoArgsConstructor
@ConfigurationProperties(prefix = PasswordCacheExpirationProperties.PASSWORD_CACHE_EXPIRATION_CONFIG_PREFIX)
public class PasswordCacheExpirationProperties {
    public static final String PASSWORD_CACHE_EXPIRATION_CONFIG_PREFIX = "financemanagerapp.password-cache-expiration";

    private Long quantity;
    private TimeUnit unit;
}
