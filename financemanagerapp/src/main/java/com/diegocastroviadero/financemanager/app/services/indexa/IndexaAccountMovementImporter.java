package com.diegocastroviadero.financemanager.app.services.indexa;

import com.diegocastroviadero.financemanager.app.model.Account;
import com.diegocastroviadero.financemanager.app.model.Movement;
import com.diegocastroviadero.financemanager.app.services.MovementService;
import com.diegocastroviadero.financemanager.cryptoutils.CsvUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class IndexaAccountMovementImporter extends AbstractIndexaImporter<Movement> {
    private final MovementService movementService;

    public IndexaAccountMovementImporter(final MovementService movementService) {
        this.movementService = movementService;
    }

    @Override
    protected List<Movement> loadElements(final InputStream is, final String fileName, final Account account) throws IOException {
        final List<String[]> rawRows = CsvUtils.readFromCsvFile(is, fileName, 1, ';');

        final LinkedList<Movement> movements = rawRows.stream()
                .filter(rawRow -> !StringUtils.equals("0,00€", rawRow[5]))
                .map(rawRow -> Movement.builder()
                        .bank(account.getBank())
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
