package com.diegocastroviadero.financemanager.app.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserConfigService {
    private boolean demoMode = false;

    public boolean isDemoMode() {
        return demoMode;
    }

    public void setDemoMode(final boolean demoMode) {
        this.demoMode = demoMode;
    }
}
