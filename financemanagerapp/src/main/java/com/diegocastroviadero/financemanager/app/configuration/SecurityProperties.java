package com.diegocastroviadero.financemanager.app.configuration;

import lombok.Builder;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@ConfigurationProperties(prefix = SecurityProperties.SECURITY_CONFIG_PREFIX)
@ConstructorBinding
public class SecurityProperties {
    public static final String SECURITY_CONFIG_PREFIX = "financemanagerapp.security";

    private final Boolean requiresHttps;
    private final Integer incorrectLoginsToBlockIp;
    private final List<FMUser> users;

    public SecurityProperties(final Boolean requiresHttps, final Integer incorrectLoginsToBlockIp, final String users) {
        this.requiresHttps = requiresHttps;
        this.incorrectLoginsToBlockIp = incorrectLoginsToBlockIp;

        this.users = Stream.of(users.split("\\|")).map(ru -> {
            final String[] credentialRolesSplit = ru.split("@");
            final String[] credentialSplit = credentialRolesSplit[0].split(":");

            return FMUser.builder()
                    .username(credentialSplit[0])
                    .password(credentialSplit[1])
                    .roles(credentialRolesSplit[1].split(","))
                    .build();
        })
        .collect(Collectors.toList());
    }

    @Builder
    @Getter
    public static class FMUser {
        private final String username;
        private final String password;
        private final String[] roles;
    }
}
