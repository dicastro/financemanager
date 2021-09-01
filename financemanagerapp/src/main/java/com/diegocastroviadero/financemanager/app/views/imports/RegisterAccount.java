package com.diegocastroviadero.financemanager.app.views.imports;

import com.diegocastroviadero.financemanager.app.model.AccountPurpose;
import com.diegocastroviadero.financemanager.app.model.Bank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@ToString
public class RegisterAccount {
    private Bank bank;
    private String accountNumber;
    private String alias;
    private AccountPurpose purpose;
}