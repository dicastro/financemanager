package com.diegocastroviadero.financemanager.app.services.ing;

import com.diegocastroviadero.financemanager.app.model.Account;
import com.diegocastroviadero.financemanager.app.model.AccountPurpose;
import com.diegocastroviadero.financemanager.app.model.Bank;
import com.diegocastroviadero.financemanager.app.model.Movement;
import com.diegocastroviadero.financemanager.app.services.AbstractImporter;
import com.diegocastroviadero.financemanager.app.services.MovementService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class IngAccountMovementImporter extends AbstractImporter<Movement> {
    private final MovementService movementService;

    public IngAccountMovementImporter(final MovementService movementService) {
        this.movementService = movementService;
    }

    @Override
    public boolean applies(final Bank bank, final AccountPurpose purpose) {
        return Bank.ING == bank;
    }

    @Override
    protected List<Movement> loadElements(final InputStream is, final String fileName, final Account account) throws IOException {
        final LinkedList<Movement> movements = new LinkedList<>();

        final HSSFWorkbook wb = new HSSFWorkbook(is);
        final HSSFSheet sheet = wb.getSheetAt(0);

        boolean processingRows = false;

        rowiter: for (Row row : sheet) {
            log.trace("Iterating over row #{} of file '{}'", row.getRowNum(), fileName);

            List<String> columns = new ArrayList<>();

            int blankCellsConsecutive = 0;

            for (Cell cell : row) {
                log.trace("Iterating over cell #{} of row #{} of file '{}'", cell.getColumnIndex(), cell.getRowIndex(), fileName);

                final String stringCellValue = cell.toString();

                if (StringUtils.isBlank(stringCellValue)) {
                    blankCellsConsecutive++;

                    if (columns.isEmpty()) {
                        if (processingRows) {
                            break rowiter;
                        } else {
                            break;
                        }
                    } else {
                        if (blankCellsConsecutive > 3) {
                            break;
                        } else {
                            columns.add(stringCellValue);
                        }
                    }
                } else {
                    if (processingRows || IngUtils.isDate(stringCellValue)) {
                        processingRows = true;
                        columns.add(stringCellValue);
                    } else {
                        break;
                    }
                }
            }

            if (!columns.isEmpty()) {
                log.debug("Read row from file '{}': {}", fileName, String.join(", ", columns));

                movements.add(Movement.builder()
                        .bank(account.getBank())
                        .accountId(account.getId())
                        .account(account.getAccountNumber())
                        .date(IngUtils.parseMovementDate(columns.get(0)))
                        .concept(IngUtils.parseMovementConcept(columns.get(3)))
                        .quantity(IngUtils.parseMovementQuantityToBigDecimal(columns.get(5)))
                        .build());
            }
        }

        final List<Movement> reversedMovements = new ArrayList<>();
        movements.descendingIterator()
                .forEachRemaining(reversedMovements::add);

        return reversedMovements;
    }

    @Override
    protected void persistElements(final char[] password, final UUID accountId, final List<Movement> elements) throws IOException {
        movementService.persistMovements(password, accountId, elements);
    }
}
