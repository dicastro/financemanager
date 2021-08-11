package com.diegocastroviadero.financemanager.app.services.ing;

import com.diegocastroviadero.financemanager.app.configuration.ImportProperties;
import com.diegocastroviadero.financemanager.app.model.Account;
import com.diegocastroviadero.financemanager.app.model.Bank;
import com.diegocastroviadero.financemanager.app.model.ImportScope;
import com.diegocastroviadero.financemanager.app.model.Movement;
import com.diegocastroviadero.financemanager.app.services.AbstractImporter;
import com.diegocastroviadero.financemanager.app.services.AccountService;
import com.diegocastroviadero.financemanager.app.services.MovementService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class IngAccountMovementImporter extends AbstractImporter<Movement> {
    private static final String FILENAME_REGEX = "movimientos_ing_(ES\\d{2} \\d{4} \\d{2} \\d{14}).xls";

    private final MovementService movementService;

    public IngAccountMovementImporter(final ImportProperties properties, final AccountService accountService, final MovementService movementService) {
        super(properties, accountService);
        this.movementService = movementService;
    }

    @Override
    public boolean applies(final File file) {
        return file.getName().matches(FILENAME_REGEX);
    }

    @Override
    public Bank getBank() {
        return Bank.ING;
    }

    @Override
    public ImportScope getImportScope() {
        return ImportScope.ACCOUNT;
    }

    @Override
    public String getAccountNumber(final File file) {
        final Pattern pattern = Pattern.compile(FILENAME_REGEX);
        final Matcher matcher = pattern.matcher(file.getName());

        String id = null;

        if (matcher.matches()) {
            final String iban = matcher.group(1);

            final String[] ibanParts = iban.split(" ");

            id = String.format("%s %s%s %s %s", ibanParts[1], ibanParts[2], ibanParts[3].substring(0, 2), ibanParts[3].substring(2, 4), ibanParts[3].substring(4));
        }

        return id;
    }

    @Override
    protected List<Movement> loadElements(final File file, final Account account) throws IOException {
        final LinkedList<Movement> movements = new LinkedList<>();

        final FileInputStream fis = new FileInputStream(file);

        final HSSFWorkbook wb = new HSSFWorkbook(fis);
        final HSSFSheet sheet = wb.getSheetAt(0);

        boolean processingRows = false;

        rowiter: for (Row row : sheet) {
            log.trace("Iterating over row #{} of file '{}'", row.getRowNum(), file.getName());

            List<String> columns = new ArrayList<>();

            int blankCellsConsecutive = 0;

            for (Cell cell : row) {
                log.trace("Iterating over cell #{} of row #{} of file '{}'", cell.getColumnIndex(), cell.getRowIndex(), file.getName());

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
                log.debug("Read row from file '{}': {}", file.getName(), String.join(", ", columns));

                movements.add(Movement.builder()
                        .bank(getBank())
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
