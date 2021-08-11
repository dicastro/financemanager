package com.diegocastroviadero.financemanager.app.configuration;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

@Getter
@Setter
@NoArgsConstructor
@ConfigurationProperties(prefix = ImportProperties.IMPORT_CONFIG_PREFIX)
public class ImportProperties {
    public static final String IMPORT_CONFIG_PREFIX = "financemanagerapp.import";

    private Path basePath;
    private Boolean deleteAfterImport;
}
