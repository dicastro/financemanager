package com.diegocastroviadero.financemanager.app.model;

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
public class InvestmentPosition {
    private Long index;
    private final Bank bank;
    private final UUID accountId;
    private final String account;
    private final LocalDate date;
    private final BigDecimal inverted;
    private final BigDecimal value;
    private final BigDecimal profitabilityPer;
    private final BigDecimal profitabilityQty;

    public YearMonth getDateYearMonth() {
        YearMonth dateYearMonth = null;

        if (null != date) {
            dateYearMonth = YearMonth.from(date);
        }

        return dateYearMonth;
    }

    public String[] toStringArray() {
        return new String[] {
                CsvSerializationUtils.serializeLongToCsv(index),
                CsvSerializationUtils.serializeEnumToCsv(bank),
                CsvSerializationUtils.serializeUUIDToCsv(accountId),
                account,
                CsvSerializationUtils.serializeDateToCsv(date),
                CsvSerializationUtils.serializeBigDecimalToCsv(inverted),
                CsvSerializationUtils.serializeBigDecimalToCsv(value),
                CsvSerializationUtils.serializeBigDecimalToCsv(profitabilityPer),
                CsvSerializationUtils.serializeBigDecimalToCsv(profitabilityQty)
        };
    }

    public static InvestmentPosition fromStringArray(String[] rawData) {
        final Long index = CsvSerializationUtils.parseLongFromCsv(rawData[0]);
        final Bank bank = CsvSerializationUtils.parseEnumFromCsv(rawData[1], Bank.class);
        final UUID accountId = CsvSerializationUtils.parseUUIDFromCsv(rawData[2]);
        final String account = rawData[3];
        final LocalDate parsedDate = CsvSerializationUtils.parseDateFromCsv(rawData[4]);
        final BigDecimal parsedInverted = CsvSerializationUtils.parseLongAsBigDecimalFromCsv(rawData[5]);
        final BigDecimal parsedValue = CsvSerializationUtils.parseLongAsBigDecimalFromCsv(rawData[6]);
        final BigDecimal parsedProfitabilityPer = CsvSerializationUtils.parseLongAsBigDecimalFromCsv(rawData[7]);
        final BigDecimal parsedProfitabilityQty = CsvSerializationUtils.parseLongAsBigDecimalFromCsv(rawData[8]);

        return new InvestmentPosition(index, bank, accountId, account, parsedDate, parsedInverted, parsedValue, parsedProfitabilityPer, parsedProfitabilityQty);
    }
}