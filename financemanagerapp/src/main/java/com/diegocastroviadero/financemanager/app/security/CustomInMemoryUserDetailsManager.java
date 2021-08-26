package com.diegocastroviadero.financemanager.app.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public class CustomInMemoryUserDetailsManager extends InMemoryUserDetailsManager {
    private final HttpServletRequest request;
    private final LoginAttemptService loginAttemptService;

    public CustomInMemoryUserDetailsManager(final List<UserDetails> users, final HttpServletRequest request, final LoginAttemptService loginAttemptService) {
        super(users);
        this.request = request;
        this.loginAttemptService = loginAttemptService;
    }

    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        String ip = getClientIP();

        if (loginAttemptService.isBlocked(ip)) {
            throw new RuntimeException("blocked");
        }

        return super.loadUserByUsername(username);
    }

    private String getClientIP() {
        String xfHeader = request.getHeader("X-Forwarded-For");

        if (xfHeader == null) {
            return request.getRemoteAddr();
        }

        return xfHeader.split(",")[0];
    }
}
