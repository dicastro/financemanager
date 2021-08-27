package com.diegocastroviadero.financemanager.app.configuration;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@NoArgsConstructor
@ConfigurationProperties(prefix = CacheProperties.CACHE_CONFIG_PREFIX)
public class CacheProperties {
    public static final String CACHE_CONFIG_PREFIX = "financemanagerapp.cache";

    private Long cleanInterval;
    private Integer noopCleansToInvalidateSession;
}
