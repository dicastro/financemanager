package com.diegocastroviadero.financemanager.app.services.ing;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class IngUtils {
    public static boolean isDate(final String rawValue) {
        return rawValue.matches("\\d{2}-(?:ene\\.|feb\\.|mar\\.|abr\\.|may\\.|jun\\.|jul\\.|ago\\.|sept\\.|oct\\.|nov\\.|dic\\.)-\\d{4}");
    }

    public static LocalDate parseMovementDate(final String rawDate) {
        return LocalDate.parse(rawDate, DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
    }

    public static BigDecimal parseMovementQuantityToBigDecimal(final String rawQuantity) {
        return new BigDecimal(rawQuantity);
    }

    public static String parseMovementConcept(final String rawConcept) {
        return StringUtils.trim(rawConcept);
    }
}
