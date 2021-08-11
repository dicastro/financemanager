package com.diegocastroviadero.financemanager.bankscrapper.model;

import com.diegocastroviadero.financemanager.cryptoutils.CsvSerializationUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.UUID;

@Builder
@Getter
@Setter
@ToString
public class Account {
    private final Bank bank;
    private final UUID id;
    private final String accountNumber;
    private String alias;
    private AccountType type;
    private AccountScope scope;
    private LocalDate balanceDate;
    private Long balance;

    public String[] toStringArray() {
        return new String[] {
                CsvSerializationUtils.serializeEnumToCsv(bank),
                CsvSerializationUtils.serializeUUIDToCsv(id),
                accountNumber,
                alias,
                CsvSerializationUtils.serializeEnumToCsv(type),
                CsvSerializationUtils.serializeEnumToCsv(scope),
                CsvSerializationUtils.serializeDateToCsv(balanceDate),
                CsvSerializationUtils.serializeLongToCsv(balance)
        };
    }

    public static Account fromStringArray(String[] rawData) {
        final Bank bank = CsvSerializationUtils.parseEnumFromCsv(rawData[0], Bank.class);
        final UUID id = CsvSerializationUtils.parseUUIDFromCsv(rawData[1]);
        final String account = rawData[2];
        final String alias = rawData[3];
        final AccountType type = CsvSerializationUtils.parseEnumFromCsv(rawData[4], AccountType.class);
        final AccountScope scope = CsvSerializationUtils.parseEnumFromCsv(rawData[5], AccountScope.class);
        final LocalDate balanceDate = CsvSerializationUtils.parseDateFromCsv(rawData[6]);
        final Long balanceQuantity = CsvSerializationUtils.parseLongFromCsv(rawData[7]);

        return new Account(bank, id, account, alias, type, scope, balanceDate, balanceQuantity);
    }
}