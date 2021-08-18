package com.diegocastroviadero.financemanager.app.configuration;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@ConfigurationProperties(prefix = BuildProperties.BUILD_CONFIG_PREFIX)
@ConstructorBinding
public class BuildProperties {
    public static final String BUILD_CONFIG_PREFIX = "financemanagerapp.build";

    private final String version;
    private final LocalDateTime timestamp;

    public BuildProperties(final String version, final String timestamp) {
        this.version = version;

        if (timestamp.matches("\\d{8}-\\d{4}")) {
            this.timestamp = LocalDateTime.parse(timestamp, DateTimeFormatter.ofPattern("yyyyMMdd-HHmm")).withSecond(0).withNano(0);
        } else {
            this.timestamp = LocalDateTime.parse(timestamp, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")).withSecond(0).withNano(0);
        }
    }
}
