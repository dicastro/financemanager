package com.diegocastroviadero.financemanager.app.services.indexa;

import com.diegocastroviadero.financemanager.app.configuration.ImportProperties;
import com.diegocastroviadero.financemanager.app.model.*;
import com.diegocastroviadero.financemanager.app.services.AccountService;
import com.diegocastroviadero.financemanager.app.services.MovementService;
import com.diegocastroviadero.financemanager.cryptoutils.CsvUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class IndexaAccountMovementImporter extends AbstractIndexaImporter<Movement> {
    private final MovementService movementService;

    public IndexaAccountMovementImporter(final ImportProperties properties, final AccountService accountService, final MovementService movementService) {
        super(properties, accountService);
        this.movementService = movementService;
    }

    @Override
    public ImportScope getImportScope() {
        return ImportScope.ACCOUNT;
    }

    @Override
    protected List<Movement> loadElements(final File file, final Account account) throws IOException {
        final List<String[]> rawRows = CsvUtils.readFromCsvFile(file, 1, ';');

        final LinkedList<Movement> movements = rawRows.stream()
                .filter(rawRow -> !StringUtils.equals("0,00€", rawRow[5]))
                .map(rawRow -> Movement.builder()
                        .bank(getBank())
                        .accountId(account.getId())
                        .account(account.getAccountNumber())
                        .date(IndexaUtils.parseMovementDate(rawRow[0]))
                        .concept(IndexaUtils.parseMovementConcept(rawRow[5]))
                        .quantity(IndexaUtils.parseQuantityToBigDecimal(rawRow[5]))
                        .build())
                .collect(Collectors.toCollection(LinkedList::new));

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
