package com.diegocastroviadero.financemanager.bankscrapper.scrapper.kb.console;

import com.diegocastroviadero.financemanager.bankscrapper.model.Bank;
import com.diegocastroviadero.financemanager.bankscrapper.scrapper.common.model.BankCredential;
import lombok.Getter;

@Getter
public class KbBankCredential extends BankCredential {
    private final String username;
    private final String password;

    public KbBankCredential(final Bank bank, final String username, final String password) {
        super(bank);
        this.username = username;
        this.password = password;
    }
}
