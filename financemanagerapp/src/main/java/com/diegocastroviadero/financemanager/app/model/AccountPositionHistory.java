package com.diegocastroviadero.financemanager.app.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Builder
@Getter
@ToString
public class AccountPositionHistory {
    private final UUID accountId;
    @Singular("serie")
    private final List<AccountPositionHistorySeries> series;
    private final String[] labels;
}