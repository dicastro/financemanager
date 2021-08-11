package com.diegocastroviadero.financemanager.app.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(value = {ImportProperties.class, PersistenceProperties.class})
public class FinanceManagerAppConfiguration {
}
