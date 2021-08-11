package com.diegocastroviadero.financemanager.bankscrapper.scrapper.kb.utils;

import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KbUtils {
    public static LocalDate parseKbMovementDate(final String date) {
        return LocalDate.parse(date, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public static Long parseKbMovementQuantity(final String rawQuantity) {
        final String cleanRawQuantity = rawQuantity.replaceAll("\\.", "");

        final Pattern regexPattern = Pattern.compile("(\\-?\\d+)(?:,(\\d+))?.*");
        final Matcher patternMatcher = regexPattern.matcher(cleanRawQuantity.trim());

        final String extracted;

        if (patternMatcher.matches()) {
            final String partInteger = patternMatcher.group(1);
            final String partDecimal = patternMatcher.group(2);

            final StringBuilder extractedBuilder = new StringBuilder();

            if (StringUtils.isNotBlank(partInteger)) {
                extractedBuilder.append(partInteger);
            }

            if (StringUtils.isNotBlank(partDecimal)) {
                extractedBuilder.append(partDecimal);
            } else {
                extractedBuilder.append("00");
            }

            extracted = extractedBuilder.toString();
        } else {
            // this exception should never be thrown
            throw new RuntimeException(String.format("Quantity '%s' could not be parsed", rawQuantity));
        }

        return Long.valueOf(extracted);
    }
}
