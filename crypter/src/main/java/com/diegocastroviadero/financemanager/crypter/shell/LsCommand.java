package com.diegocastroviadero.financemanager.crypter.shell;

import com.diegocastroviadero.financemanager.crypter.model.CrypterContext;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.io.File;
import java.util.stream.Stream;

@ShellComponent
public class LsCommand {

    @ShellMethod("List files in workdir")
    public String ls() {
        final File[] allFiles = CrypterContext.getWorkdir().toFile().listFiles();

        if (null != allFiles && allFiles.length > 0) {
            System.out.printf("Following files were found:%n%n");

            Stream.of(allFiles).forEach(file -> System.out.printf("  - %s%n", file.getName()));

            return "";
        } else {
            return "No files in workdir";
        }
    }
}