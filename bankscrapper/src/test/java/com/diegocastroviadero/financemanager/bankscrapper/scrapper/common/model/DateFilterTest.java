package com.diegocastroviadero.financemanager.bankscrapper.scrapper.common.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DateFilterTest {
    @Test
    void testYearMonthList1() {
        // Given
        final LocalDate startDate = LocalDate.of(2021, 2, 1);
        final LocalDate endDate = LocalDate.of(2021, 2, 28);

        final Map<Year, List<Month>> expectedMonthsGroupedByYear = Map.of(
                Year.of(2021), List.of(Month.FEBRUARY)
        );

        // When
        final Map<Year, List<Month>> monthsGroupedByYear = new DateFilter(startDate, endDate).getMonthsGroupedByYear();

        // Then
        assertEquals(expectedMonthsGroupedByYear, monthsGroupedByYear);
    }

    @Test
    void testYearMonthList2() {
        // Given
        final LocalDate startDate = LocalDate.of(2021, 2, 1);
        final LocalDate endDate = LocalDate.of(2021, 5, 31);

        final Map<Year, List<Month>> expectedMonthsGroupedByYear = Map.of(
                Year.of(2021), List.of(Month.FEBRUARY, Month.MARCH, Month.APRIL, Month.MAY)
        );

        // When
        final Map<Year, List<Month>> monthsGroupedByYear = new DateFilter(startDate, endDate).getMonthsGroupedByYear();

        // Then
        assertEquals(expectedMonthsGroupedByYear, monthsGroupedByYear);
    }

    @Test
    void testYearMonthList3() {
        // Given
        final LocalDate startDate = LocalDate.of(2021, 2, 1);
        final LocalDate endDate = LocalDate.of(2021, 3, 31);

        final Map<Year, List<Month>> expectedMonthsGroupedByYear = Map.of(
                Year.of(2021), List.of(Month.FEBRUARY, Month.MARCH)
        );

        // When
        final Map<Year, List<Month>> monthsGroupedByYear = new DateFilter(startDate, endDate).getMonthsGroupedByYear();

        // Then
        assertEquals(expectedMonthsGroupedByYear, monthsGroupedByYear);
    }

    @Test
    void testYearMonthList4() {
        // Given
        final LocalDate startDate = LocalDate.of(2020, 12, 1);
        final LocalDate endDate = LocalDate.of(2021, 3, 31);

        final Map<Year, List<Month>> expectedMonthsGroupedByYear = Map.of(
                Year.of(2020), List.of(Month.DECEMBER),
                Year.of(2021), List.of(Month.JANUARY, Month.FEBRUARY, Month.MARCH)
        );

        // When
        final Map<Year, List<Month>> monthsGroupedByYear = new DateFilter(startDate, endDate).getMonthsGroupedByYear();

        // Then
        assertEquals(expectedMonthsGroupedByYear, monthsGroupedByYear);
    }
}