package com.diegocastroviadero.financemanager.app.model;

import com.diegocastroviadero.financemanager.cryptoutils.CsvSerializationUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.Month;
import java.util.UUID;

@Builder(toBuilder = true)
@Getter
@Setter
@ToString
public class PlannedExpense {
    private final UUID id;
    private String concept;
    private Scope scope;
    private Month month;
    private BigDecimal quantity;

    public String[] toStringArray() {
        return new String[] {
                CsvSerializationUtils.serializeUUIDToCsv(id),
                concept,
                CsvSerializationUtils.serializeEnumToCsv(scope),
                CsvSerializationUtils.serializeEnumToCsv(month),
                CsvSerializationUtils.serializeBigDecimalToCsv(quantity)
        };
    }

    public static PlannedExpense fromStringArray(String[] rawData) {
        final UUID id = CsvSerializationUtils.parseUUIDFromCsv(rawData[0]);
        final String description = rawData[1];
        final Scope scope = CsvSerializationUtils.parseEnumFromCsv(rawData[2], Scope.class);
        final Month month = CsvSerializationUtils.parseEnumFromCsv(rawData[3], Month.class);
        final BigDecimal quantity = CsvSerializationUtils.parseLongAsBigDecimalFromCsv(rawData[4]);

        return new PlannedExpense(id, description, scope, month, quantity);
    }
}