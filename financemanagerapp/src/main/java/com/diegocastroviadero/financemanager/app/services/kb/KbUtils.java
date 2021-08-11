package com.diegocastroviadero.financemanager.app.services.kb;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class KbUtils {
    public static boolean isDate(final String rawValue) {
        return rawValue.matches("\\d{2}/\\d{2}/\\d{4}");
    }

    public static LocalDate parseMovementDate(final String rawDate) {
        return LocalDate.parse(rawDate, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public static BigDecimal parseMovementQuantityToBigDecimal(final String rawQuantity) {
        return new BigDecimal(rawQuantity);
    }

    public static String parseMovementConcept(final String rawConcept) {
        return StringUtils.trim(rawConcept);
    }
}
