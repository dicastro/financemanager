package com.diegocastroviadero.financemanager.bankscrapper.model;

import com.diegocastroviadero.financemanager.bankscrapper.utils.Utils;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZonedDateTime;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
public enum SyncType {
    PAST_ONE_MONTH(1),
    PAST_TWO_MONTHS(2);

    private final int monthDiff;

    public LocalDate getStartDate() {
        final ZonedDateTime today = Utils.now();

        final YearMonth now = YearMonth.from(today);

        // It is taken into account that in the first days of the month, the bank may not have processed the credit card transactions
        // Given that the current month is M
        //   - from 1st to 4th, monthDiff = monthDiff + 1
        //   - from 4th onwards, monthDiff remains the same
        int monthDiffToApply = today.getDayOfMonth() < 5 ? monthDiff + 1 : monthDiff;

        // With the limitation that the maximum monthDiff will be 6
        // This is because some banks require extra authentication to view movements older than 6 months
        monthDiffToApply = Math.min(monthDiffToApply, 2);

        return now.minusMonths(monthDiffToApply).atDay(1);
    }

    public LocalDate getEndDate() {
        final ZonedDateTime today = Utils.now();

        final YearMonth now = YearMonth.from(today);

        int monthDiffToApply = today.getDayOfMonth() < 5 ? 2 : 1;

        return now.minusMonths(monthDiffToApply).atEndOfMonth();
    }
}
