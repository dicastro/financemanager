package com.diegocastroviadero.financemanager.cryptoutils;

import com.diegocastroviadero.financemanager.cryptoutils.exception.CsvIOException;
import com.opencsv.*;
import com.opencsv.exceptions.CsvException;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.List;

@Slf4j
public class CsvUtils {
    public static List<String[]> readFromCsvFile(final File csvFile) throws CsvIOException {
        return readFromCsvFile(csvFile, CSVReader.DEFAULT_SKIP_LINES, ICSVParser.DEFAULT_SEPARATOR);
    }

    public static List<String[]> readFromCsvFile(final File csvFile, final int skipLines, final char separator) throws CsvIOException {
        final CSVParser csvParser = new CSVParserBuilder()
                .withSeparator(separator)
                .build();

        try (CSVReader reader = new CSVReaderBuilder(new InputStreamReader(new FileInputStream(csvFile))).withSkipLines(skipLines).withCSVParser(csvParser).build()) {
            final List<String[]> elements = reader.readAll();

            log.debug("Read {} elements from file '{}'", elements.size(), csvFile.getName());

            return elements;
        } catch (IOException | CsvException e) {
            throw new CsvIOException(String.format("Error while reading elements from plain file '%s'", csvFile), e);
        }
    }

    public static void persistToCsvFile(final List<String[]> elements, final File csvFile) throws CsvIOException {
        try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(csvFile)))) {
            for (String[] element : elements) {
                writer.writeNext(element);
            }

            log.debug("Persisted all elements to file '{}'", csvFile.getName());
        } catch (IOException e) {
            throw new CsvIOException(String.format("Error while persisting elements to plain file '%s'", csvFile), e);
        }
    }
}
