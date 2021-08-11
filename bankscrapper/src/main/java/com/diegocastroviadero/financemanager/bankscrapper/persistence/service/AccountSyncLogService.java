package com.diegocastroviadero.financemanager.bankscrapper.persistence.service;

import com.diegocastroviadero.financemanager.bankscrapper.configuration.PersistenceProperties;
import com.diegocastroviadero.financemanager.bankscrapper.model.AccountSyncLog;
import com.diegocastroviadero.financemanager.bankscrapper.model.Bank;
import com.diegocastroviadero.financemanager.bankscrapper.model.SyncStatus;
import com.diegocastroviadero.financemanager.cryptoutils.CsvUtils;
import com.diegocastroviadero.financemanager.cryptoutils.exception.CsvIOException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@Service
public class AccountSyncLogService extends AbstractPersistenceService {
    private static final String ACCOUNT_SYNC_LOG_FILENAME_PATTERN = "account_sync_log_%d.csv";

    public AccountSyncLogService(final PersistenceProperties persistenceProperties) {
        super(persistenceProperties);
    }

    public void markAccountAsSyncing(final Bank bank, final UUID accountId, final Map<Year, List<Month>> monthsByYear, final ZonedDateTime timestamp) throws CsvIOException {
        updateSyncStatus(bank, accountId, monthsByYear, SyncStatus.SYNCING, timestamp);
    }

    public void markAccountAsSynced(final Bank bank, final UUID accountId, final Map<Year, List<Month>> monthsByYear, final ZonedDateTime timestamp) throws CsvIOException {
        updateSyncStatus(bank, accountId, monthsByYear, SyncStatus.SYNCED, timestamp);
    }

    public void markBankSyncingAccountsAsSyncError(final Bank bank, final Map<Year, List<Month>> monthsByYear, final String errorMessage) throws CsvIOException {
        for (Year year : monthsByYear.keySet()) {
            final List<AccountSyncLog> accountsSyncInfo = getAllAccountSyncLog(year);

            final List<AccountSyncLog> bankAccountsSyncInfo = accountsSyncInfo.stream()
                    .filter(accountSyncLog -> accountSyncLog.getBank() == bank && accountSyncLog.getSyncStatus() == SyncStatus.SYNCING)
                    .peek(accountSyncLog -> {
                        accountSyncLog.setSyncStatus(SyncStatus.SYNC_ERROR);
                        accountSyncLog.setSyncErrorMessage(errorMessage);
                    })
                    .collect(Collectors.toList());

            if (!bankAccountsSyncInfo.isEmpty()) {
                persistAccountSyncLog(year, accountsSyncInfo);
            }
        }
    }

    private void updateSyncStatus(final Bank bank, final UUID accountId, final Map<Year, List<Month>> monthsByYear, final SyncStatus syncStatus, final ZonedDateTime timestamp) throws CsvIOException {
        for (Entry<Year, List<Month>> yearMonthEntry : monthsByYear.entrySet()) {
            final Year year = yearMonthEntry.getKey();
            final List<Month> months = yearMonthEntry.getValue();

            final List<AccountSyncLog> accountsSyncInfo = getAllAccountSyncLog(year);

            final List<AccountSyncLog> foundAccountSyncInfo = accountsSyncInfo.stream()
                    .filter(accountSyncLog -> accountSyncLog.getAccountId().equals(accountId)
                            && months.contains(accountSyncLog.getYearMonth().getMonth()))
                    .collect(Collectors.toList());

            // It is assumed that when one AccountSyncLog is found for a YearMonth, all AccountSyncLog are found for the rest of YearMonth elements
            // The AccountSyncLog are persisted in block
            if (foundAccountSyncInfo.isEmpty()) {
                months.forEach(month -> accountsSyncInfo.add(AccountSyncLog.builder()
                        .bank(bank)
                        .accountId(accountId)
                        .yearMonth(YearMonth.of(year.getValue(), month))
                        .syncStatus(syncStatus)
                        .syncStatusTimestamp(timestamp)
                        .build()));
            } else {
                foundAccountSyncInfo.forEach(existingAccountSyncLog -> {
                    existingAccountSyncLog.setSyncStatus(syncStatus);
                    existingAccountSyncLog.setSyncErrorMessage(null);
                });
            }

            persistAccountSyncLog(year, accountsSyncInfo);
        }
    }

    private List<AccountSyncLog> getAllAccountSyncLog(final Year year) throws CsvIOException {
        final File file = getFile(getFilename(year));

        final List<AccountSyncLog> accountSyncLog;

        if (file.exists()) {
            final List<String[]> rawCsvData = CsvUtils.readFromCsvFile(file);

            accountSyncLog = rawCsvData.stream()
                    .map(AccountSyncLog::fromStringArray)
                    .collect(Collectors.toList());
        } else {
            accountSyncLog = new ArrayList<>();
        }

        return accountSyncLog;
    }

    private void persistAccountSyncLog(final Year year, final List<AccountSyncLog> yearAccountsSyncInfo) throws CsvIOException {
        final File file = getFile(getFilename(year));

        final List<String[]> sortedAccounts = yearAccountsSyncInfo.stream()
                .sorted(Comparator
                        .comparing(AccountSyncLog::getBank)
                        .thenComparing(AccountSyncLog::getAccountId)
                        .thenComparing(AccountSyncLog::getYearMonth))
                .map(AccountSyncLog::toStringArray)
                .collect(Collectors.toList());

        CsvUtils.persistToCsvFile(sortedAccounts, file);
    }

    private String getFilename(final Year year) {
        return String.format(ACCOUNT_SYNC_LOG_FILENAME_PATTERN, year.getValue());
    }
}
