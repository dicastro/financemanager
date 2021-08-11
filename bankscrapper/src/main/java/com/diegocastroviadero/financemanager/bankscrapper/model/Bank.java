package com.diegocastroviadero.financemanager.bankscrapper.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public enum Bank {
    KB("KB");

    private final String filenameText;
}
