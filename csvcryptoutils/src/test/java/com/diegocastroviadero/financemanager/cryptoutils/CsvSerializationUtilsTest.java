package com.diegocastroviadero.financemanager.cryptoutils;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CsvSerializationUtilsTest {

    @ParameterizedTest
    @ArgumentsSource(BigDecimalsForSerializationProvider.class)
    void givenBigDecimalValue01_whenSerializeBigDecimalToCsv_thenValueIsSerialized(final BigDecimal givenBigDecimal, final String expected) {
        // When
        final String result = CsvSerializationUtils.serializeBigDecimalToCsv(givenBigDecimal);

        // Then
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @ArgumentsSource(StringsForBigDecimalParseProvider.class)
    void parseLongAsBigDecimalFromCsv(final String givenString, final BigDecimal expected) {
        // When
        final BigDecimal result = CsvSerializationUtils.parseLongAsBigDecimalFromCsv(givenString);

        // Then
        assertEquals(expected, result);
    }

    static class BigDecimalsForSerializationProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    Arguments.of(new BigDecimal("500"), "50000"),
                    Arguments.of(new BigDecimal("-100"), "-10000"),
                    Arguments.of(new BigDecimal("0.5"), "50"),
                    Arguments.of(new BigDecimal("-0.23"), "-23"),
                    Arguments.of(new BigDecimal("23.57"), "2357"),
                    Arguments.of(new BigDecimal("-328.62"), "-32862"),
                    Arguments.of(new BigDecimal("10795.75"), "1079575")
            );
        }
    }

    static class StringsForBigDecimalParseProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    Arguments.of("50000", new BigDecimal("500.00")),
                    Arguments.of("-10000", new BigDecimal("-100.00")),
                    Arguments.of("50", new BigDecimal("0.50")),
                    Arguments.of("-23", new BigDecimal("-0.23")),
                    Arguments.of("2357", new BigDecimal("23.57")),
                    Arguments.of("-32862", new BigDecimal("-328.62")),
                    Arguments.of("1079575", new BigDecimal("10795.75"))
            );
        }
    }
}