package com.diegocastroviadero.financemanager.bankscrapper.persistence.service;

import com.diegocastroviadero.financemanager.bankscrapper.configuration.PersistenceProperties;
import com.diegocastroviadero.financemanager.bankscrapper.model.Account;
import com.diegocastroviadero.financemanager.bankscrapper.model.Bank;
import com.diegocastroviadero.financemanager.bankscrapper.scrapper.common.model.SecurityContext;
import com.diegocastroviadero.financemanager.cryptoutils.CsvCryptoUtils;
import com.diegocastroviadero.financemanager.cryptoutils.exception.CsvCryptoIOException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service
public class AccountInventoryService extends AbstractPersistenceService {
    private static final String ACCOUNT_INVENTORY_FILENAME = "account_inventory.ecsv";

    public AccountInventoryService(final PersistenceProperties persistenceProperties) {
        super(persistenceProperties);
    }

    public List<Account> registerAccounts(final Bank bank, final List<String> newAccounts) throws CsvCryptoIOException {
        final List<Account> existingAccounts = getAllAccounts();

        final AtomicBoolean newAccountsToPersist = new AtomicBoolean(Boolean.FALSE);

        newAccounts.forEach(newAccount -> {
            final boolean accountIsNew = existingAccounts.stream()
                    .noneMatch(a -> StringUtils.equals(a.getAccountNumber(), newAccount));

            if (accountIsNew) {
                existingAccounts.add(Account.builder()
                        .bank(bank)
                        .accountNumber(newAccount)
                        .id(UUID.randomUUID())
                        .balance(0L)
                        .build());

                newAccountsToPersist.set(Boolean.TRUE);
            }
        });

        if (newAccountsToPersist.get()) {
            persistAccounts(existingAccounts);
        }

        return existingAccounts.stream()
                .filter(a -> newAccounts.contains(a.getAccountNumber()))
                .collect(Collectors.toList());
    }

    private List<Account> getAllAccounts() throws CsvCryptoIOException {
        final File file = getFile(getFilename());

        List<Account> accounts;

        if (file.exists()) {
            final List<String[]> rawCsvData = CsvCryptoUtils.decryptFromCsvFile(SecurityContext.getEncryptionPassword().toCharArray(), file);

            accounts = rawCsvData.stream()
                    .map(Account::fromStringArray)
                    .collect(Collectors.toList());

        } else {
            accounts = new ArrayList<>();
        }

        return accounts;
    }

    private void persistAccounts(final List<Account> accounts) throws CsvCryptoIOException {
        final File file = getFile(getFilename());

        final List<String[]> sortedRawAccounts = accounts.stream()
                .sorted(Comparator
                        .comparing(Account::getBank)
                        .thenComparing(Account::getAccountNumber))
                .map(Account::toStringArray)
                .collect(Collectors.toList());

        CsvCryptoUtils.encryptToCsvFile(sortedRawAccounts, SecurityContext.getEncryptionPassword().toCharArray(), file);
    }

    private String getFilename() {
        return ACCOUNT_INVENTORY_FILENAME;
    }
}
