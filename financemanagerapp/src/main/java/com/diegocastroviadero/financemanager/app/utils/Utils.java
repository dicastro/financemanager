package com.diegocastroviadero.financemanager.app.utils;

import ch.obermuhlner.math.big.BigDecimalMath;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeFormatter;

public final class Utils {
    private static final ZoneId UTC_ZONE = ZoneId.of("UTC");

    private Utils() {
        // Util methods only
    }

    private static final DateTimeFormatter TABLE_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static String tableFormatDate(final LocalDate date) {
        return date.format(TABLE_DATE_FORMATTER);
    }

    public static String tableFormatMoney(final BigDecimal quantity) {
        return String.format("%.2f €", quantity);
    }

    public static String tableFormatMonthAbbreviated(final Month month) {
        return month.name().substring(0, 3);
    }

    public static ZonedDateTime now() {
        return ZonedDateTime.now(UTC_ZONE);
    }

    public static Month currentMonth() {
        return now().getMonth();
    }

    public static YearMonth previousYearMonth() {
        return YearMonth.now(UTC_ZONE).minusMonths(1);
    }

    public static BigDecimal obfuscateBigDecimal(final BigDecimal input) {
        final BigDecimal REF_VAL = BigDecimalMath.toBigDecimal("1.0");
        final BigDecimal MULTIPLIER = BigDecimalMath.toBigDecimal("10.0");

        BigDecimal bd = input;

        boolean negated = false;

        if (bd.compareTo(BigDecimal.ZERO) < 0) {
            negated = true;
            bd = bd.negate();
        }

        BigDecimal result;

        if (bd.compareTo(REF_VAL) > 0) {
            result = BigDecimalMath.log2(bd, MathContext.DECIMAL32).multiply(MULTIPLIER).setScale(2, RoundingMode.HALF_UP);
        } else {
            result = bd;
        }

        if (negated) {
            result = result.negate();
        }

        return result;
    }
}
