package com.diegocastroviadero.financemanager.bankscrapper.model;

import com.diegocastroviadero.financemanager.bankscrapper.utils.Utils;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SyncTypeTest {
    @Test
    void testStartAndEndDateAtBeginningOfMonth() {
        // Given
        SyncType syncType = SyncType.PAST_ONE_MONTH;

        try (MockedStatic<Utils> utils = Mockito.mockStatic(Utils.class)) {
            final ZonedDateTime firstMay = ZonedDateTime.of(LocalDate.of(2021, 5, 1), LocalTime.of(12, 0, 0, 0), ZoneId.of("UTC"));

            utils.when(Utils::now).thenReturn(firstMay);

            final LocalDate expectedStartDate = LocalDate.of(2021, 3, 1);
            final LocalDate expectedEndDate = LocalDate.of(2021, 3, 31);

            // When
            final LocalDate startDate = syncType.getStartDate();
            final LocalDate endDate = syncType.getEndDate();

            // Then
            assertEquals(expectedStartDate, startDate);
            assertEquals(expectedEndDate, endDate);
        }
    }

    @Test
    void testStartAndEndDateAtBeginningOfMonth2() {
        // Given
        SyncType syncType = SyncType.PAST_TWO_MONTHS;

        try (MockedStatic<Utils> utils = Mockito.mockStatic(Utils.class)) {
            final ZonedDateTime firstSep = ZonedDateTime.of(LocalDate.of(2021, 9, 1), LocalTime.of(12, 0, 0, 0), ZoneId.of("UTC"));

            utils.when(Utils::now).thenReturn(firstSep);

            final LocalDate expectedStartDate = LocalDate.of(2021, 7, 1);
            final LocalDate expectedEndDate = LocalDate.of(2021, 7, 31);

            // When
            final LocalDate startDate = syncType.getStartDate();
            final LocalDate endDate = syncType.getEndDate();

            // Then
            assertEquals(expectedStartDate, startDate);
            assertEquals(expectedEndDate, endDate);
        }
    }

    @Test
    void testStartAndEndDateAtEndOfMonth() {
        // Given
        SyncType syncType = SyncType.PAST_ONE_MONTH;

        try (MockedStatic<Utils> utils = Mockito.mockStatic(Utils.class)) {
            final ZonedDateTime finalMay = ZonedDateTime.of(LocalDate.of(2021, 5, 20), LocalTime.of(12, 0, 0, 0), ZoneId.of("UTC"));

            utils.when(Utils::now).thenReturn(finalMay);

            final LocalDate expectedStartDate = LocalDate.of(2021, 4, 1);
            final LocalDate expectedEndDate = LocalDate.of(2021, 4, 30);

            // When
            final LocalDate startDate = syncType.getStartDate();
            final LocalDate endDate = syncType.getEndDate();

            // Then
            assertEquals(expectedStartDate, startDate);
            assertEquals(expectedEndDate, endDate);
        }
    }

    @Test
    void testStartAndEndDateAtEndOfMonth2() {
        // Given
        SyncType syncType = SyncType.PAST_TWO_MONTHS;

        try (MockedStatic<Utils> utils = Mockito.mockStatic(Utils.class)) {
            final ZonedDateTime finalSep = ZonedDateTime.of(LocalDate.of(2021, 9, 20), LocalTime.of(12, 0, 0, 0), ZoneId.of("UTC"));

            utils.when(Utils::now).thenReturn(finalSep);

            final LocalDate expectedStartDate = LocalDate.of(2021, 7, 1);
            final LocalDate expectedEndDate = LocalDate.of(2021, 8, 31);

            // When
            final LocalDate startDate = syncType.getStartDate();
            final LocalDate endDate = syncType.getEndDate();

            // Then
            assertEquals(expectedStartDate, startDate);
            assertEquals(expectedEndDate, endDate);
        }
    }
}