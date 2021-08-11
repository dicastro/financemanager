package com.diegocastroviadero.financemanager.bankscrapper.model;

import com.diegocastroviadero.financemanager.cryptoutils.CsvSerializationUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.UUID;

@Builder
@Getter
@ToString
public class Movement {
    private final Bank bank;
    private final UUID accountId;
    private final String account;
    private final LocalDate date;
    private final String concept;
    private final LocalDate dateValue;
    private final Long quantity;

    public String[] toStringArray() {
        return new String[] {
                CsvSerializationUtils.serializeEnumToCsv(bank),
                CsvSerializationUtils.serializeUUIDToCsv(accountId),
                account,
                CsvSerializationUtils.serializeDateToCsv(date),
                concept,
                CsvSerializationUtils.serializeDateToCsv(dateValue),
                CsvSerializationUtils.serializeLongToCsv(quantity)
        };
    }

    public static Movement fromStringArray(String[] rawData) {
        final Bank bank = CsvSerializationUtils.parseEnumFromCsv(rawData[0], Bank.class);
        final UUID accountId = CsvSerializationUtils.parseUUIDFromCsv(rawData[1]);
        final String account = rawData[2];
        final LocalDate parsedDate = CsvSerializationUtils.parseDateFromCsv(rawData[3]);
        final String concept = rawData[4];
        final LocalDate parsedDateValue = CsvSerializationUtils.parseDateFromCsv(rawData[5]);
        final Long parsedQuantity = CsvSerializationUtils.parseLongFromCsv(rawData[6]);

        return new Movement(bank, accountId, account, parsedDate, concept, parsedDateValue, parsedQuantity);
    }
}