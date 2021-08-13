package com.diegocastroviadero.financemanager.app.services;

import com.diegocastroviadero.financemanager.app.configuration.BuildProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class UserConfigService {
    private final BuildProperties buildProperties;

    private boolean demoMode = false;

    public UserConfigService(final BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    public String getVersionLabel() {
        return String.format("Version %s (built %s)", buildProperties.getVersion(), buildProperties.getTimestamp().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
    }

    public boolean isDemoMode() {
        return demoMode;
    }

    public void setDemoMode(final boolean demoMode) {
        this.demoMode = demoMode;
    }
}
