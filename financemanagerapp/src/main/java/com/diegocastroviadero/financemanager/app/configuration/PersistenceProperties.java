package com.diegocastroviadero.financemanager.app.configuration;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

@Getter
@Setter
@NoArgsConstructor
@ConfigurationProperties(prefix = PersistenceProperties.PERSISTENCE_CONFIG_PREFIX)
public class PersistenceProperties {
    public static final String PERSISTENCE_CONFIG_PREFIX = "financemanagerapp.persistence";

    private DbFilesProperties dbfiles;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class DbFilesProperties {
        private Path basePath;
    }
}
