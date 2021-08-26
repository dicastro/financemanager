package com.diegocastroviadero.financemanager.app.security;

import com.diegocastroviadero.financemanager.app.services.AuthCleanerService;
import com.diegocastroviadero.financemanager.app.services.CacheCleanerService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@AllArgsConstructor
public class CustomLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {
    private final AuthCleanerService authCleanerService;
    private final CacheCleanerService cacheCleanerService;

    @Override
    public void onLogoutSuccess(final HttpServletRequest request, final HttpServletResponse response, final Authentication authentication) throws IOException, ServletException {
        setDefaultTargetUrl("/login?logout");

        authCleanerService.cleanAuth();
        cacheCleanerService.cleanCache();

        super.onLogoutSuccess(request, response, authentication);
    }
}
