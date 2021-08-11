package com.diegocastroviadero.financemanager.bankscrapper.scrapper.kb.console;

import com.diegocastroviadero.financemanager.bankscrapper.model.Bank;
import com.diegocastroviadero.financemanager.bankscrapper.scrapper.common.model.BankCredential;
import com.diegocastroviadero.financemanager.bankscrapper.scrapper.common.model.BankCredentialReader;
import com.diegocastroviadero.financemanager.consoleutils.ArgumentReader;
import com.diegocastroviadero.financemanager.consoleutils.ArgumentReaderProvider;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class KbBankCredentialReader implements BankCredentialReader {

    private final ArgumentReader argumentReader;

    public KbBankCredentialReader() {
        argumentReader = ArgumentReaderProvider.getInstance();
    }

    @Override
    public Bank getBank() {
        return Bank.KB;
    }

    @Override
    public boolean applies(String rawCredentials) {
        final Pattern pattern = Pattern.compile("(\\w+):(\\w+):(.+)");
        final Matcher matcher = pattern.matcher(rawCredentials);

        return matcher.matches()
                && EnumUtils.isValidEnum(Bank.class, matcher.group(1))
                && EnumUtils.getEnum(Bank.class, matcher.group(1)) == getBank();
    }

    @Override
    public BankCredential parseCredentials(final String rawCredentials) throws IOException {
        final Pattern pattern = Pattern.compile("(\\w+):(\\w+):(.+)");
        final Matcher matcher = pattern.matcher(rawCredentials);

        if (matcher.matches()) {
            final String username = matcher.group(2);
            final String password = matcher.group(3);

            return new KbBankCredential(getBank(), username, password);
        } else {
            throw new IOException("Credential format is not valid. Format is: <BANK>:<USER>:<PASSWORD>");
        }
    }

    @Override
    public BankCredential readBankCredentials() {
        final String username = argumentReader.readString(String.format("Username for '%s' > ", getBank()));
        final String password = argumentReader.readPassword(String.format("Password for '%s' > ", getBank()));

        return new KbBankCredential(getBank(), username, password);
    }
}
