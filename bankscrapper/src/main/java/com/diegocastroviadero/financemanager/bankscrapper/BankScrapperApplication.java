package com.diegocastroviadero.financemanager.bankscrapper;

import com.diegocastroviadero.financemanager.bankscrapper.model.Bank;
import com.diegocastroviadero.financemanager.bankscrapper.model.SyncType;
import com.diegocastroviadero.financemanager.bankscrapper.scrapper.common.model.BankCredential;
import com.diegocastroviadero.financemanager.bankscrapper.scrapper.common.model.BankCredentialReader;
import com.diegocastroviadero.financemanager.bankscrapper.scrapper.common.model.SecurityContext;
import com.diegocastroviadero.financemanager.bankscrapper.scrapper.kb.service.ScrapperKbService;
import com.diegocastroviadero.financemanager.consoleutils.ArgumentReader;
import com.diegocastroviadero.financemanager.consoleutils.ArgumentReaderProvider;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
@Slf4j
@SpringBootApplication
public class BankScrapperApplication implements CommandLineRunner {
    private static final String SYNC_TYPE_OPTION = "t";
    private static final String WHATIF_MODE_OPTION = "w";
    private static final String BANK_CREDENTIAL_FILE_OPTION = "c";
    private static final String ENCRYPTION_PASSWORD_FILE_OPTION = "e";

    private final ScrapperKbService scrapperKbService;
    private final List<BankCredentialReader> bankCredentialReaders;

    public static void main(String[] args) {
        SpringApplication.run(BankScrapperApplication.class, args);
    }

    @Override
    public void run(String... args) {
        final ArgumentReader argumentReader = ArgumentReaderProvider.getInstance();

        final Options options = new Options();
        options.addOption(SYNC_TYPE_OPTION, "sync-type", Boolean.TRUE, "Sync type");
        options.addOption(WHATIF_MODE_OPTION, "what-if", Boolean.FALSE, "WhatIf mode");
        options.addOption(BANK_CREDENTIAL_FILE_OPTION, "bank-credential", Boolean.TRUE, "Bank credential file");
        options.addOption(ENCRYPTION_PASSWORD_FILE_OPTION, "encryption-password", Boolean.TRUE, "Encryption password file");

        CommandLineParser parser = new DefaultParser();

        CommandLine cmd;

        try {
            cmd = parser.parse( options, args);
        } catch (ParseException e) {
            throw new RuntimeException("Error while parsing arguments", e);
        }

        final boolean whatIfMode = cmd.hasOption(WHATIF_MODE_OPTION);

        SyncType syncType = null;

        if (cmd.hasOption(SYNC_TYPE_OPTION)) {
            final String rawSyncType = cmd.getOptionValue(SYNC_TYPE_OPTION);

            if (EnumUtils.isValidEnum(SyncType.class, rawSyncType)) {
                syncType = EnumUtils.getEnum(SyncType.class, rawSyncType);

                log.debug("Sync type argument received {}", syncType);
            } else {
                log.error("'{}' is not a valid sync type. Admitted values are: {}", rawSyncType, Stream.of(SyncType.values())
                        .map(Enum::name)
                        .collect(Collectors.joining(", ")));
            }
        } else {
            syncType = SyncType.PAST_ONE_MONTH;

            log.warn("Missing sync type argument, default sync type will be used ({})", syncType);
        }

        final Map<Bank, BankCredential> bankCredentials = new HashMap<>();

        if (cmd.hasOption(BANK_CREDENTIAL_FILE_OPTION)) {
            final String[] bankCredentialFiles = cmd.getOptionValues(BANK_CREDENTIAL_FILE_OPTION);

            for (String bankCredentialFile : bankCredentialFiles) {
                String readBankCredential = null;

                try {
                    readBankCredential = Files.readString(Paths.get(bankCredentialFile));
                } catch (IOException e) {
                    log.error("Error while reading bank credential file '{}'", bankCredentialFile, e);
                    System.exit(1);
                }

                final String rawBankCredential = readBankCredential;
                bankCredentialReaders.stream()
                        .filter(bankCredentialReader -> bankCredentialReader.applies(rawBankCredential))
                        .findFirst()
                        .ifPresent(bankCredentialReader -> {
                            try {
                                final BankCredential bankCredential = bankCredentialReader.parseCredentials(rawBankCredential);
                                bankCredentials.put(bankCredential.getBank(), bankCredential);
                            } catch (IOException e) {
                                log.error("Error parsing bank credential file '{}'", bankCredentialFile, e);
                            }
                        });
            }
        }

        final List<Bank> banksWithMissingBankCredentials = Stream.of(Bank.values())
                .filter(b -> !bankCredentials.containsKey(b))
                .collect(Collectors.toList());

        for (Bank bankWithMissingCredentials : banksWithMissingBankCredentials) {
            bankCredentialReaders.stream()
                    .filter(bankCredentialReader -> bankCredentialReader.getBank() == bankWithMissingCredentials)
                    .findFirst()
                    .ifPresent(bankCredentialReader -> {
                        final BankCredential bankCredential = bankCredentialReader.readBankCredentials();
                        bankCredentials.put(bankCredential.getBank(), bankCredential);
                    });
        }

        SecurityContext.setBankCredentials(bankCredentials);

        String encryptionPassword = null;

        if (cmd.hasOption(ENCRYPTION_PASSWORD_FILE_OPTION)) {
            final String encryptionPasswordFile = cmd.getOptionValue(ENCRYPTION_PASSWORD_FILE_OPTION);

            try {
                encryptionPassword = Files.readString(Paths.get(encryptionPasswordFile));
            } catch (IOException e) {
                log.error("Error while reading encryption password file '{}'", encryptionPasswordFile, e);
                System.exit(1);
            }
        }

        if (StringUtils.isBlank(encryptionPassword)) {
            encryptionPassword = argumentReader.readPassword("Encryption password> ");
        }

        SecurityContext.setEncryptionPassword(encryptionPassword);

        if (null != syncType) {
            scrapperKbService.scrap(syncType, whatIfMode);

            System.exit(0);
        } else {
            System.exit(1);
        }
    }
}
