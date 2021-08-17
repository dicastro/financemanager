package com.diegocastroviadero.financemanager.app.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@EnableConfigurationProperties(value = {
        ImportProperties.class,
        PersistenceProperties.class,
        BuildProperties.class,
        CacheProperties.class,
        SecurityProperties.class})
public class FinanceManagerAppConfiguration {

    @Bean
    public UserDetailsService userDetailsService(final SecurityProperties securityProperties) {
        log.info("Users loaded: {}", securityProperties.getUsers().size());

        final List<UserDetails> users = securityProperties.getUsers().stream()
                .map(fmUser -> User.withUsername(fmUser.getUsername())
                        .password(fmUser.getPassword())
                        .roles(fmUser.getRoles())
                        .build())
                .collect(Collectors.toList());

        return new InMemoryUserDetailsManager(users);
    }
}
