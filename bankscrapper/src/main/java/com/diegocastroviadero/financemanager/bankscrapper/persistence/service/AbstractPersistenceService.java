package com.diegocastroviadero.financemanager.bankscrapper.persistence.service;

import com.diegocastroviadero.financemanager.bankscrapper.configuration.PersistenceProperties;

import java.io.File;

public abstract class AbstractPersistenceService {
    protected final PersistenceProperties properties;

    public AbstractPersistenceService(final PersistenceProperties properties) {
        this.properties = properties;
    }

    protected File getFile(final String filename) {
        return properties.getDbfiles().getBasePath().resolve(filename).toFile();
    }
}
