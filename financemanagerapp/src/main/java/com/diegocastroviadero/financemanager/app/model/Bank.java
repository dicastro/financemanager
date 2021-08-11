package com.diegocastroviadero.financemanager.app.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public enum Bank {
    // Kutxabank
    KB,
    // ING Direct
    ING,
    // Indexa Capital
    IC
}
