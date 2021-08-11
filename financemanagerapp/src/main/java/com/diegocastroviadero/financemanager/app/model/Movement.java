package com.diegocastroviadero.financemanager.app.model;

import ch.obermuhlner.math.big.BigDecimalMath;
import com.diegocastroviadero.financemanager.app.utils.Utils;
import com.diegocastroviadero.financemanager.cryptoutils.CsvSerializationUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.UUID;

@Builder
@Getter
@Setter
@ToString
public class Movement {
    private Long index;
    private final Bank bank;
    private final UUID accountId;
    private final String account;
    private final LocalDate date;
    private final String concept;
    private final BigDecimal quantity;

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
                concept,
                CsvSerializationUtils.serializeBigDecimalToCsv(quantity)
        };
    }

    public static Movement fromStringArray(final String[] rawData, final Boolean demoMode) {
        final Long index = CsvSerializationUtils.parseLongFromCsv(rawData[0]);
        final Bank bank = CsvSerializationUtils.parseEnumFromCsv(rawData[1], Bank.class);
        final UUID accountId = CsvSerializationUtils.parseUUIDFromCsv(rawData[2]);
        final String account = rawData[3];
        final LocalDate parsedDate = CsvSerializationUtils.parseDateFromCsv(rawData[4]);
        final String concept = rawData[5];

        BigDecimal parsedQuantity = CsvSerializationUtils.parseLongAsBigDecimalFromCsv(rawData[6]);

        if (demoMode) {
            parsedQuantity = Utils.obfuscateBigDecimal(parsedQuantity);
        }

        return new Movement(index, bank, accountId, account, parsedDate, concept, parsedQuantity);
    }
}