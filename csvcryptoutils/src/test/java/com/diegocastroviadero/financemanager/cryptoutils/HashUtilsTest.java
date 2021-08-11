package com.diegocastroviadero.financemanager.cryptoutils;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class HashUtilsTest {

    @Test
    void givenSameDataOnSameStructure_whenGetHash_thenSameHashIsCalculated() throws IOException {
        // Given
        final String[] row1 = new String[] {"1A", "1B", "1C", "1D", "1E"};
        final String[] row2 = new String[] {"2A", "2B", "2C", "2D", "2E"};

        final List<String[]> element1a = Arrays.asList(row1, row2);
        final List<String[]> element1b = Arrays.asList(row1, row2);

        // When
        final String hash1a = HashUtils.getHash(element1a);
        final String hash1b = HashUtils.getHash(element1b);

        // Then
        Assertions.assertEquals(hash1a, hash1b);
    }

    @Test
    void givenSameDataOnDifferentStructure_whenGetHash_thenSameHashIsCalculated() throws IOException {
        // Given
        final String[] row1 = new String[] {"1A", "1B", "1C", "1D", "1E"};
        final String[] row2 = new String[] {"2A", "2B", "2C", "2D", "2E"};

        final List<String[]> element1a = Arrays.asList(row1, row2);
        final List<String[]> element1b = new LinkedList<>();
        element1b.add(row1);
        element1b.add(row2);

        // When
        final String hash1a = HashUtils.getHash(element1a);
        final String hash1b = HashUtils.getHash(element1b);

        // Then
        Assertions.assertEquals(hash1a, hash1b);
    }

    @Test
    void givenDifferentData_whenGetHash_thenDifferentHashesAreCalculated() throws IOException {
        // Given
        final String[] row1 = new String[] {"1A", "1B", "1C", "1D", "1E"};
        final String[] row2a = new String[] {"2A", "2B", "2C", "2D", "2E"};
        final String[] row2b = new String[] {"2A", "2B", "2C", "2D", "2F"};

        final List<String[]> element1a = Arrays.asList(row1, row2a);
        final List<String[]> element1b = Arrays.asList(row1, row2b);

        // When
        final String hash1a = HashUtils.getHash(element1a);
        final String hash1b = HashUtils.getHash(element1b);

        // Then
        Assertions.assertNotEquals(hash1a, hash1b);
    }
}
