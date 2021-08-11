package com.diegocastroviadero.financemanager.cryptoutils;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CsvSerializationUtils {
    private static final DateTimeFormatter CSV_DATE_FORMATTER_DEFAULT = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final DateTimeFormatter CSV_DATETIME_FORMATTER_DEFAULT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS");
    private static final DateTimeFormatter CSV_YEARMONTH_FORMATTER_DEFAULT = DateTimeFormatter.ofPattern("yyyy/MM");

    private static final String CSV_LIST_SEPARATOR = "#";
    private static final int BIGDECIMAL_SCALE = 2;

    public static String serializeDateToCsv(final LocalDate data) {
        return serializeDateToCsv(data, CSV_DATE_FORMATTER_DEFAULT);
    }

    public static String serializeDateToCsv(final LocalDate data, final DateTimeFormatter formatter) {
        String result = null;

        if (null != data) {
            result = data.format(formatter);
        }

        return result;
    }

    public static String serializeYearMonthToCsv(final YearMonth data) {
        return serializeYearMonthToCsv(data, CSV_YEARMONTH_FORMATTER_DEFAULT);
    }

    public static String serializeYearMonthToCsv(final YearMonth data, final DateTimeFormatter formatter) {
        String result = null;

        if (null != data) {
            result = data.format(formatter);
        }

        return result;
    }

    public static String serializeDateToCsv(final ZonedDateTime data) {
        return serializeDateToCsv(data, CSV_DATETIME_FORMATTER_DEFAULT);
    }

    public static String serializeDateToCsv(final ZonedDateTime data, final DateTimeFormatter formatter) {
        String result = null;

        if (null != data) {
            result = data.format(formatter);
        }

        return result;
    }

    public static String serializeLongToCsv(final Long data) {
        String result = null;

        if (null != data) {
            result = data.toString();
        }

        return result;
    }

    public static String serializeBigDecimalToCsv(final BigDecimal data) {
        String result = null;

        if (null != data) {
            result = data.setScale(BIGDECIMAL_SCALE).unscaledValue().toString();
        }

        return result;
    }

    public static <E extends Enum<E>> String serializeEnumToCsv(final E data) {
        String result = null;

        if (null != data) {
            result = data.name();
        }

        return result;
    }

    public static <E extends Enum<E>> String serializeEnumToCsv(final Collection<E> data) {
        String result = null;

        if (null != data) {
            result = data.stream()
                    .map(Enum::name)
                    .collect(Collectors.joining(CSV_LIST_SEPARATOR));
        }

        return result;
    }

    public static String serializeUUIDToCsv(final UUID data) {
        String result = null;

        if (null != data) {
            result = data.toString();
        }

        return result;
    }

    public static YearMonth parseYearMonthFromCsv(final String rawCsvData) {
        return parseYearMonthFromCsv(rawCsvData, CSV_YEARMONTH_FORMATTER_DEFAULT);
    }

    public static YearMonth parseYearMonthFromCsv(final String rawCsvData, final DateTimeFormatter formatter) {
        YearMonth result = null;

        if (StringUtils.isNotBlank(rawCsvData) && !StringUtils.equals(rawCsvData, "null")) {
            try {
                result = YearMonth.parse(rawCsvData, formatter);
            } catch (Exception ignore) {}
        }

        return result;
    }

    public static LocalDate parseDateFromCsv(final String rawCsvData) {
        return parseDateFromCsv(rawCsvData, CSV_DATE_FORMATTER_DEFAULT);
    }

    public static LocalDate parseDateFromCsv(final String rawCsvData, final DateTimeFormatter formatter) {
        LocalDate result = null;

        if (StringUtils.isNotBlank(rawCsvData) && !StringUtils.equals(rawCsvData, "null")) {
            try {
                result = LocalDate.parse(rawCsvData, formatter);
            } catch (Exception ignore) {}
        }

        return result;
    }

    public static ZonedDateTime parseTimestampFromCsv(final String rawCsvData, final ZoneId zone) {
        return parseTimestampFromCsv(rawCsvData, CSV_DATETIME_FORMATTER_DEFAULT, zone);
    }

    public static ZonedDateTime parseTimestampFromCsv(final String rawCsvData, final DateTimeFormatter formatter, final ZoneId zone) {
        ZonedDateTime result = null;

        if (StringUtils.isNotBlank(rawCsvData) && !StringUtils.equals(rawCsvData, "null")) {
            try {
                result = LocalDateTime.parse(rawCsvData, formatter)
                        .atZone(zone);
            } catch (Exception ignore) {}
        }

        return result;
    }

    public static Long parseLongFromCsv(final String rawCsvData) {
        Long result = null;

        if (StringUtils.isNotBlank(rawCsvData) && !StringUtils.equals(rawCsvData, "null")) {
            try {
                result = Long.parseLong(rawCsvData);
            } catch (Exception ignore) {
            }
        }

        return result;
    }

    public static BigDecimal parseLongAsBigDecimalFromCsv(final String rawCsvData) {
        BigDecimal result = null;

        if (StringUtils.isNotBlank(rawCsvData) && !StringUtils.equals(rawCsvData, "null")) {
            try {
                result = BigDecimal.valueOf(parseLongFromCsv(rawCsvData), BIGDECIMAL_SCALE);
            } catch (Exception ignore) {
            }
        }

        return result;
    }

    public static <E extends Enum<E>> E parseEnumFromCsv(final String rawCsvData, final Class<E> enumType) {
        E result = null;

        if (StringUtils.isNotBlank(rawCsvData) && !StringUtils.equals(rawCsvData, "null")) {
            try {
                result = Enum.valueOf(enumType, rawCsvData);
            } catch (Exception ignore) {
            }
        }

        return result;
    }

    public static <E extends Enum<E>> Set<E> parseEnumSetFromCsv(final String rawCsvData, final Class<E> enumType) {
        Set<E> result = Collections.emptySet();

        if (StringUtils.isNotBlank(rawCsvData) && !StringUtils.equals(rawCsvData, "null")) {
            try {
                result = Stream.of(rawCsvData.split(CSV_LIST_SEPARATOR))
                        .map(rawEnum -> parseEnumFromCsv(rawEnum, enumType))
                        .collect(Collectors.toSet());
            } catch (Exception ignore) {
            }
        }

        return result;
    }

    public static UUID parseUUIDFromCsv(final String rawCsvData) {
        UUID result = null;

        if (StringUtils.isNotBlank(rawCsvData) && !StringUtils.equals(rawCsvData, "null")) {
            try {
                result = UUID.fromString(rawCsvData);
            } catch (Exception ignore) {
            }
        }

        return result;
    }
}
