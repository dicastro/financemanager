package com.diegocastroviadero.financemanager.app;

import com.vaadin.flow.component.dependency.NpmPackage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.vaadin.artur.helpers.LaunchUtil;

@Slf4j
@AllArgsConstructor
@SpringBootApplication(exclude = ErrorMvcAutoConfiguration.class)
@NpmPackage(value = "lumo-css-framework", version = "^4.0.10")
@NpmPackage(value = "line-awesome", version = "1.3.0")
@EnableScheduling
public class FinanceManagerApplication extends SpringBootServletInitializer {

    public static void main(final String[] args) {
        LaunchUtil.launchBrowserInDevelopmentMode(SpringApplication.run(FinanceManagerApplication.class, args));
    }

}
