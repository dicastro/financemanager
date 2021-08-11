package com.diegocastroviadero.financemanager.app.services.indexa;

import com.diegocastroviadero.financemanager.app.model.Account;
import com.diegocastroviadero.financemanager.app.model.AccountPosition;
import com.diegocastroviadero.financemanager.app.model.Bank;
import com.diegocastroviadero.financemanager.app.model.InvestmentPosition;
import com.diegocastroviadero.financemanager.app.services.AccountPositionCalculator;
import com.diegocastroviadero.financemanager.app.services.InvestmentPositionService;
import com.diegocastroviadero.financemanager.cryptoutils.exception.CsvCryptoIOException;
import lombok.AllArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.time.YearMonth;

@Service
@AllArgsConstructor
@Order(value = Ordered.LOWEST_PRECEDENCE - 100)
public class IndexaAccountPositionCalculator implements AccountPositionCalculator {

    private final InvestmentPositionService investmentPositionService;

    @Override
    public boolean applies(Account account) {
        return Bank.IC == account.getBank();
    }

    @Override
    public AccountPosition getAccountPosition(char[] password, Account account) throws CsvCryptoIOException {
        final InvestmentPosition lastInvestmentPosition = investmentPositionService.getLastInvestmentPositionByAccount(password, account.getId());

        return AccountPosition.builder()
                .bank(account.getBank())
                .accountId(account.getId())
                .alias(account.getAlias())
                .scope(account.getScope())
                .type(account.getPurpose())
                .balance(lastInvestmentPosition.getValue())
                .extra(String.format("(%.2f%% - %.2f)", lastInvestmentPosition.getProfitabilityPer(), lastInvestmentPosition.getProfitabilityQty()))
                .balanceDate(YearMonth.from(lastInvestmentPosition.getDate()).atEndOfMonth())
                .monthFrom(account.getBalanceDateYearMonth())
                .initialBalance(account.getBalance())
                .build();
    }
}
