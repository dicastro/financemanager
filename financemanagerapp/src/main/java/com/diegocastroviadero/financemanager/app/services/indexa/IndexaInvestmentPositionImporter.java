package com.diegocastroviadero.financemanager.app.services.indexa;

import com.diegocastroviadero.financemanager.app.configuration.ImportProperties;
import com.diegocastroviadero.financemanager.app.model.Account;
import com.diegocastroviadero.financemanager.app.model.ImportScope;
import com.diegocastroviadero.financemanager.app.model.InvestmentPosition;
import com.diegocastroviadero.financemanager.app.services.AccountService;
import com.diegocastroviadero.financemanager.app.services.InvestmentPositionService;
import com.diegocastroviadero.financemanager.cryptoutils.CsvUtils;
import lombok.extern.slf4j.Slf4j;
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
public class IndexaInvestmentPositionImporter extends AbstractIndexaImporter<InvestmentPosition> {

    private final InvestmentPositionService investmentPositionService;

    public IndexaInvestmentPositionImporter(final ImportProperties properties, final AccountService accountService, final InvestmentPositionService investmentPositionService) {
        super(properties, accountService);
        this.investmentPositionService = investmentPositionService;
    }

    @Override
    public ImportScope getImportScope() {
        return ImportScope.INVESTMENT_VALUES;
    }

    @Override
    protected List<InvestmentPosition> loadElements(final File file, final Account account) throws IOException {
        final List<String[]> rawRows = CsvUtils.readFromCsvFile(file, 1, ';');

        final LinkedList<InvestmentPosition> investmentPositions = rawRows.stream()
                .map(rawRow -> InvestmentPosition.builder()
                        .bank(getBank())
                        .accountId(account.getId())
                        .account(account.getAccountNumber())
                        .date(IndexaUtils.parseMovementDate(rawRow[0]))
                        .inverted(IndexaUtils.parseQuantityToBigDecimal(rawRow[6]))
                        .value(IndexaUtils.parseQuantityToBigDecimal(rawRow[1]))
                        .profitabilityPer(IndexaUtils.parsePercentageToBigDecimal(rawRow[2]))
                        .profitabilityQty(IndexaUtils.parseQuantityToBigDecimal(rawRow[4]))
                        .build())
                .collect(Collectors.toCollection(LinkedList::new));

        final List<InvestmentPosition> reversedInvestmentPositions = new ArrayList<>();
        investmentPositions.descendingIterator()
                .forEachRemaining(reversedInvestmentPositions::add);

        return reversedInvestmentPositions;
    }

    @Override
    protected void persistElements(final char[] password, final UUID accountId, final List<InvestmentPosition> elements) throws IOException {
        investmentPositionService.persistInvestmentPositions(password, accountId, elements);
    }
}
