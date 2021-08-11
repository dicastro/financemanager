package com.diegocastroviadero.financemanager.app.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.UUID;

@Builder
@Getter
@ToString
public class AccountPosition {
    private final Bank bank;
    private final UUID accountId;
    private final String alias;
    private final Scope scope;
    private final AccountPurpose type;
    private final YearMonth monthFrom;
    private final BigDecimal initialBalance;
    private final LocalDate balanceDate;
    private final BigDecimal balance;
    private final String extra;
}