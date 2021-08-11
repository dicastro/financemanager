package com.diegocastroviadero.financemanager.bankscrapper.model;

import com.diegocastroviadero.financemanager.bankscrapper.utils.Utils;
import com.diegocastroviadero.financemanager.cryptoutils.CsvSerializationUtils;
import lombok.*;

import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@ToString
public class AccountSyncLog {
    private final Bank bank;
    private final UUID accountId;
    private final YearMonth yearMonth;
    private SyncStatus syncStatus;
    private ZonedDateTime syncStatusTimestamp;
    private String syncErrorMessage;

    public String[] toStringArray() {
        return new String[] {
                CsvSerializationUtils.serializeEnumToCsv(bank),
                CsvSerializationUtils.serializeUUIDToCsv(accountId),
                CsvSerializationUtils.serializeYearMonthToCsv(yearMonth),
                CsvSerializationUtils.serializeEnumToCsv(syncStatus),
                CsvSerializationUtils.serializeDateToCsv(syncStatusTimestamp),
                syncErrorMessage
        };
    }

    public static AccountSyncLog fromStringArray(String[] rawData) {
        final Bank bank = CsvSerializationUtils.parseEnumFromCsv(rawData[0], Bank.class);
        final UUID accountId = CsvSerializationUtils.parseUUIDFromCsv(rawData[1]);
        final YearMonth yearMonth = CsvSerializationUtils.parseYearMonthFromCsv(rawData[2]);
        final SyncStatus syncStatus = CsvSerializationUtils.parseEnumFromCsv(rawData[3], SyncStatus.class);
        final ZonedDateTime syncStatusTimestamp = CsvSerializationUtils.parseTimestampFromCsv(rawData[4], Utils.getZone());
        final String syncErrorMessage = rawData[5];

        return new AccountSyncLog(bank, accountId, yearMonth, syncStatus, syncStatusTimestamp, syncErrorMessage);
    }
}
