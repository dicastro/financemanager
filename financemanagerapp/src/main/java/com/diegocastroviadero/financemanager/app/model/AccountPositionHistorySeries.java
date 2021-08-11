package com.diegocastroviadero.financemanager.app.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
@Getter
@ToString
public class AccountPositionHistorySeries {
    private final String alias;
    private final BigDecimal[] values;
}