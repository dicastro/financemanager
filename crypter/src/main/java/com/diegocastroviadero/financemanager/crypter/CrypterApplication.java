package com.diegocastroviadero.financemanager.crypter;

import com.diegocastroviadero.financemanager.consoleutils.ArgumentReader;
import com.diegocastroviadero.financemanager.consoleutils.ArgumentReaderProvider;
import com.diegocastroviadero.financemanager.crypter.model.CrypterContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.shell.jline.PromptProvider;

import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@SpringBootApplication
public class CrypterApplication {
    private static final String ENCRYPTION_PASSWORD_FILE_SHORT_OPTION = "e";
    private static final String ENCRYPTION_PASSWORD_FILE_LONG_OPTION = "encryption-password";
    private static final String WORKDIR_SHORT_OPTION = "w";
    private static final String WORKDIR_LONG_OPTION = "work-dir";

    public static void main(String[] args) {
        final ArgumentReader argumentReader = ArgumentReaderProvider.getInstance();

        final Options options = new Options();
        options.addOption(ENCRYPTION_PASSWORD_FILE_SHORT_OPTION, ENCRYPTION_PASSWORD_FILE_LONG_OPTION, Boolean.TRUE, "Encryption password file");
        options.addOption(WORKDIR_SHORT_OPTION, WORKDIR_LONG_OPTION, Boolean.TRUE, "Working directory");

        CommandLineParser parser = new DefaultParser();

        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            throw new RuntimeException("Error while parsing arguments", e);
        }

        if (cmd.hasOption(ENCRYPTION_PASSWORD_FILE_SHORT_OPTION)) {
            CrypterContext.setEncryptionPasswordFile(cmd.getOptionValue(ENCRYPTION_PASSWORD_FILE_SHORT_OPTION));
        } else {
            log.error("Missing required parameter '-{}' (--{})", ENCRYPTION_PASSWORD_FILE_SHORT_OPTION, ENCRYPTION_PASSWORD_FILE_LONG_OPTION);
            System.exit(1);
        }

        Path workdir = null;

        if (cmd.hasOption(WORKDIR_SHORT_OPTION)) {
            workdir = Paths.get(cmd.getOptionValue(WORKDIR_SHORT_OPTION));

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
