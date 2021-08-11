package com.diegocastroviadero.financemanager.bankscrapper.configuration;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

import static com.diegocastroviadero.financemanager.bankscrapper.configuration.PersistenceProperties.PERSISTENCE_CONFIG_PREFIX;

@Getter
@Setter
@NoArgsConstructor
@ConfigurationProperties(prefix = PERSISTENCE_CONFIG_PREFIX)
public class PersistenceProperties {
    public static final String PERSISTENCE_CONFIG_PREFIX = "bankscrapper.persistence";

    private DbFilesProperties dbfiles;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class DbFilesProperties {
        private Path basePath;
    }
}
