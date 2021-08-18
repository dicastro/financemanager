package com.diegocastroviadero.financemanager.app.model;

import com.diegocastroviadero.financemanager.app.utils.Utils;
import com.diegocastroviadero.financemanager.cryptoutils.CsvSerializationUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
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
    private AccountPurpose purpose;
    private Scope scope;
    private LocalDate balanceDate;
    private BigDecimal balance;
    private Account link;

    public String getLabel() {
        return null == alias ? accountNumber : alias;
    }

    public YearMonth getBalanceDateYearMonth() {
        YearMonth balanceYearMonth = null;

        if (null != balanceDate) {
            balanceYearMonth = YearMonth.from(balanceDate);
        }

        return balanceYearMonth;
    }

    public String[] toStringArray() {
        return new String[] {
                CsvSerializationUtils.serializeEnumToCsv(bank),
                CsvSerializationUtils.serializeUUIDToCsv(id),
                accountNumber,
                alias,
                CsvSerializationUtils.serializeEnumToCsv(purpose),
                CsvSerializationUtils.serializeEnumToCsv(scope),
                CsvSerializationUtils.serializeDateToCsv(balanceDate),
                CsvSerializationUtils.serializeBigDecimalToCsv(balance),
                CsvSerializationUtils.serializeUUIDToCsv(link == null ? null : link.getId())
        };
    }

    public static Account fromStringArray(final String[] rawData, final Boolean demoMode) {
        final Bank bank = CsvSerializationUtils.parseEnumFromCsv(rawData[0], Bank.class);
        final UUID id = CsvSerializationUtils.parseUUIDFromCsv(rawData[1]);

        String account = rawData[2];

        if (demoMode) {
            account = account.replaceAll("\\d", "X");
        }

        final String alias = rawData[3];
        final AccountPurpose type = CsvSerializationUtils.parseEnumFromCsv(rawData[4], AccountPurpose.class);
        final Scope scope = CsvSerializationUtils.parseEnumFromCsv(rawData[5], Scope.class);
        final LocalDate balanceDate = CsvSerializationUtils.parseDateFromCsv(rawData[6]);

        BigDecimal balanceQuantity = CsvSerializationUtils.parseLongAsBigDecimalFromCsv(rawData[7]);

        if (demoMode) {
            balanceQuantity = Utils.obfuscateBigDecimal(balanceQuantity);
        }

        final UUID linkedAccountId = CsvSerializationUtils.parseUUIDFromCsv(rawData[8]);
        final Account linkedAccount = linkedAccountId == null ? null : Account.builder().id(linkedAccountId).build();

        return new Account(bank, id, account, alias, type, scope, balanceDate, balanceQuantity, linkedAccount);
    }
}