package com.diegocastroviadero.financemanager.app.services.indexa;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class IndexaUtils {

    public static LocalDate parseMovementDate(final String rawDate) {
        return LocalDate.parse(rawDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public static BigDecimal parseQuantityToBigDecimal(final String rawQuantity) {
        return new BigDecimal(rawQuantity
                .replaceAll("\\.", "")
                .replaceAll("€", "")
                .replaceAll(",", "."));
    }

    public static BigDecimal parsePercentageToBigDecimal(final String rawPercentage) {
        return new BigDecimal(rawPercentage
                .replaceAll("\\.", "")
                .replaceAll("\\+", "")
                .replaceAll("%", "")
                .replaceAll(",", "."));
    }

    public static String parseMovementConcept(final String rawQuantity) {
        return StringUtils.startsWith(rawQuantity, "-") ? "RETIRADA" : "DEPOSITO";
    }
}
