package com.diegocastroviadero.financemanager.bankscrapper.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(value = {PersistenceProperties.class, ScrappingProperties.class})
public class BankScrapperConfiguration {
}
