package com.diegocastroviadero.financemanager.app.services;

import com.diegocastroviadero.financemanager.app.configuration.PersistenceProperties;
import com.diegocastroviadero.financemanager.app.model.*;
import com.diegocastroviadero.financemanager.cryptoutils.exception.CsvCryptoIOException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AccountService extends AbstractPersistenceService {
    private static final String ACCOUNT_INVENTORY_FILENAME = "account_inventory.ecsv";

    private final UserConfigService userConfigService;
    private final List<AccountPositionCalculator> accountPositionCalculators;
    private final List<AccountPositionHistoryCalculator> accountPositionHistoryCalculators;


    public AccountService(final PersistenceProperties properties, final CacheService cacheService, final UserConfigService userConfigService, final List<AccountPositionCalculator> accountPositionCalculators, final List<AccountPositionHistoryCalculator> accountPositionHistoryCalculators) {
        super(properties, cacheService);
        this.userConfigService = userConfigService;
        this.accountPositionCalculators = accountPositionCalculators;
        this.accountPositionHistoryCalculators = accountPositionHistoryCalculators;
    }

    public Account registerAccount(final char[] password, final Bank bank, final String newAccount) throws CsvCryptoIOException {
        final List<Account> existingAccounts = getAllAccounts(password);

        Account foundAccount = existingAccounts.stream()
                .filter(a -> StringUtils.equals(a.getAccountNumber(), newAccount))
                .findFirst()
                .orElse(null);

        if (null == foundAccount) {
            foundAccount = Account.builder()
                    .bank(bank)
                    .accountNumber(newAccount)
                    .id(UUID.randomUUID())
                    .balance(BigDecimal.ZERO)
                    .build();

            existingAccounts.add(foundAccount);

            persistAccounts(password, existingAccounts);
        }

        return foundAccount;
    }

    public List<Account> getAllAccounts(final char[] password) throws CsvCryptoIOException {
        final File file = getFile(getFilename());

        List<Account> accounts;

        if (file.exists()) {
            final List<String[]> rawCsvData = loadData(password, file);

            accounts = rawCsvData.stream()
                    .map(rawAccount -> Account.fromStringArray(rawAccount, userConfigService.isDemoMode()))
                    .collect(Collectors.toList());

            accounts.forEach(account -> {
                if (null != account.getLink() && null != account.getLink().getId()) {
                    account.setLink(accounts.stream()
                            .filter(candidate -> candidate.getId().equals(account.getLink().getId()))
                            .findFirst()
                            .orElse(null));
                }
            });

        } else {
            accounts = new ArrayList<>();
        }

        return accounts;
    }

    public List<Account> getAllLinkableAccounts(final char[] password) throws CsvCryptoIOException {
        return getAllAccounts(password).stream()
                .filter(a -> a.getPurpose() != AccountPurpose.CREDIT)
                .collect(Collectors.toList());
    }

    public void persistAccounts(final char[] password, final List<Account> accounts) throws CsvCryptoIOException {
        final File file = getFile(getFilename());

        final List<String[]> sortedRawAccounts = accounts.stream()
                .sorted(Comparator
                        .comparing(Account::getBank)
                        .thenComparing(Account::getAccountNumber))
                .map(Account::toStringArray)
                .collect(Collectors.toList());

        persistData(sortedRawAccounts, password, file);
    }

    private String getFilename() {
        return ACCOUNT_INVENTORY_FILENAME;
    }

    public AccountPosition getAccountPosition(final char[] password, final Account account) throws CsvCryptoIOException {
        final AccountPositionCalculator accountPositionCalculator = accountPositionCalculators.stream()
                .filter(apc -> apc.applies(account))
                .findFirst()
                .get(); // DefaultAccountPositionCalculator always applies and it is the last element of accountPositionCalculators list, so always one AccountPositionCalculator will be found

        return accountPositionCalculator.getAccountPosition(password, account);
    }

    public AccountPositionHistory getAccountPositionHistory(final char[] password, final AccountPosition accountPosition) throws CsvCryptoIOException {
        final AccountPositionHistoryCalculator accountPositionHistoryCalculator = accountPositionHistoryCalculators.stream()
                .filter(apc -> apc.applies(accountPosition))
                .findFirst()
                .get(); // DefaultAccountPositionHistoryCalculator always applies and it is the last element of accountPositionHistoryCalculators list, so always one AccountPositionHistoryCalculator will be found

        return accountPositionHistoryCalculator.getAccountPositionHistory(password, accountPosition);
    }
}
