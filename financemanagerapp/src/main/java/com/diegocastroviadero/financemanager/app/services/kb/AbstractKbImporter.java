package com.diegocastroviadero.financemanager.app.services.kb;

import com.diegocastroviadero.financemanager.app.configuration.ImportProperties;
import com.diegocastroviadero.financemanager.app.model.Account;
import com.diegocastroviadero.financemanager.app.model.Bank;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public abstract class AbstractKbImporter extends AbstractImporter<Movement> {

    protected final MovementService movementService;

    public AbstractKbImporter(final ImportProperties properties, final AccountService accountService, final MovementService movementService) {
        super(properties, accountService);
        this.movementService = movementService;
    }

    @Override
    public Bank getBank() {
        return Bank.KB;
    }

    @Override
    public String getAccountNumber(final File file) {
        final Pattern pattern = Pattern.compile(getFilenameRegex());
        final Matcher matcher = pattern.matcher(file.getName());

        String id = null;

        if (matcher.matches()) {
            id = matcher.group(1);
        }

        return id;
    }

    @Override
    public boolean applies(final File file) {
        return file.getName().matches(getFilenameRegex());
    }

    protected abstract String getFilenameRegex();

    @Override
    protected List<Movement> loadElements(final File file, final Account account) throws IOException {
        final List<Movement> movements = new ArrayList<>();

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
                    if (processingRows || KbUtils.isDate(stringCellValue)) {
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
                        .date(KbUtils.parseMovementDate(columns.get(0)))
                        .concept(KbUtils.parseMovementConcept(columns.get(1)))
                        .quantity(KbUtils.parseMovementQuantityToBigDecimal(columns.get(3)))
                        .build());
            }
        }

        return movements;
    }

    @Override
    protected void persistElements(final char[] password, final UUID accountId, final List<Movement> elements) throws IOException {
        movementService.persistMovements(password, accountId, elements);
    }
}
