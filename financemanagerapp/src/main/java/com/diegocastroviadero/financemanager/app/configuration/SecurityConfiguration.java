package com.diegocastroviadero.financemanager.app.configuration;

import com.diegocastroviadero.financemanager.app.security.CustomInMemoryUserDetailsManager;
import com.diegocastroviadero.financemanager.app.security.CustomLoginFailureHandler;
import com.diegocastroviadero.financemanager.app.security.CustomLogoutSuccessHandler;
import com.diegocastroviadero.financemanager.app.security.CustomRequestCache;
import com.diegocastroviadero.financemanager.app.security.LoginAttemptService;
import com.diegocastroviadero.financemanager.app.utils.SecurityUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.context.request.RequestContextListener;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@AllArgsConstructor
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    private static final String LOGIN_PROCESSING_URL = "/login";
    private static final String LOGIN_URL = "/login";

    private final CustomLogoutSuccessHandler customLogoutSuccessHandler;
    private final CustomLoginFailureHandler customLoginFailureHandler;

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public RequestContextListener requestContextListener() {
        return new RequestContextListener();
    }

    @Bean
    public UserDetailsService userDetailsService(final SecurityProperties securityProperties, final HttpServletRequest request, final LoginAttemptService loginAttemptService) {
        log.info("Users loaded: {}", securityProperties.getUsers().size());

        final List<UserDetails> users = securityProperties.getUsers().stream()
                .map(fmUser -> User.withUsername(fmUser.getUsername())
                        .password(fmUser.getPassword())
                        .roles(fmUser.getRoles())
                        .build())
                .collect(Collectors.toList());

        return new CustomInMemoryUserDetailsManager(users, request, loginAttemptService);
    }

    /**
     * Require login to access internal pages and configure login form.
     */
    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        // Not using Spring CSRF here to be able to use plain HTML for the login page
        http.csrf().disable()

                // Register our CustomRequestCache that saves unauthorized access attempts, so
                // the user is redirected after login.
                .requestCache().requestCache(new CustomRequestCache())

                // Restrict access to our application.
                .and().authorizeRequests()
                    .antMatchers("/login", "/logout").permitAll()
                    // Allow all flow internal requests.
                    .requestMatchers(SecurityUtils::isFrameworkInternalRequest).permitAll()
                    // Allow all requests by logged in users.
                    .anyRequest().authenticated()

                // Configure the login page.
                .and().formLogin()
                    .loginPage(LOGIN_URL)
                    .loginProcessingUrl(LOGIN_PROCESSING_URL)
                    .failureHandler(customLoginFailureHandler)

                // Configure logout
                .and().logout()
                    .logoutSuccessHandler(customLogoutSuccessHandler)

                // Configure session management
                .and()
                    .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.NEVER);
    }

    /**
     * Allows access to static resources, bypassing Spring security.
     */
    @Override
    public void configure(final WebSecurity web) {
        web.ignoring().antMatchers(
                // Vaadin Flow static resources
                "/VAADIN/**",

                // the standard favicon URI
                "/favicon.ico",

                // the robots exclusion standard
                "/robots.txt",

                // web application manifest
                "/manifest.webmanifest",
                "/sw.js",
                "/offline.html",
                "/offline-page.html",

                // icons and images
                "/icons/**",
                "/images/**",

                // (development mode) static resources
                "/frontend/**",

                // (development mode) webjars
                "/webjars/**",

                // (development mode) H2 debugging console
                "/h2-console/**",

                // (production mode) static resources
                "/frontend-es5/**", "/frontend-es6/**");
    }
}