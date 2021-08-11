package com.diegocastroviadero.financemanager.crypter;

import com.diegocastroviadero.financemanager.consoleutils.ArgumentReader;
import com.diegocastroviadero.financemanager.consoleutils.ArgumentReaderProvider;
import com.diegocastroviadero.financemanager.crypter.model.CrypterContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.shell.jline.PromptProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@SpringBootApplication
public class CrypterApplication {
    private static final String ENCRYPTION_PASSWORD_FILE_OPTION = "e";
    private static final String WORKDIR_OPTION = "w";

    public static void main(String[] args) {
        final ArgumentReader argumentReader = ArgumentReaderProvider.getInstance();

        final Options options = new Options();
        options.addOption(ENCRYPTION_PASSWORD_FILE_OPTION, "encryption-password", Boolean.TRUE, "Encryption password file");
        options.addOption(WORKDIR_OPTION, "work-dir", Boolean.TRUE, "Working directory");

        CommandLineParser parser = new DefaultParser();

        CommandLine cmd;

        try {
            cmd = parser.parse( options, args);
        } catch (ParseException e) {
            throw new RuntimeException("Error while parsing arguments", e);
        }

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

        CrypterContext.setEncryptionPassword(encryptionPassword);

        Path workdir = null;

        if (cmd.hasOption(WORKDIR_OPTION)) {
            workdir = Paths.get(cmd.getOptionValue(WORKDIR_OPTION));

            if (!workdir.toFile().exists()) {
                log.error("Workdir '{}' does not exist", workdir);
                System.exit(1);
            }
        }

        if (null == workdir) {
            workdir = argumentReader.readDir("Workdir> ");
        }

        CrypterContext.setWorkdir(workdir);

        SpringApplication.run(CrypterApplication.class, args);
    }

    @Bean
    public PromptProvider myPromptProvider() {
        final String promptText = String.format("(%s) crypter:> ", CrypterContext.getWorkdir());

        return () -> new AttributedString(promptText, AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
    }
}
