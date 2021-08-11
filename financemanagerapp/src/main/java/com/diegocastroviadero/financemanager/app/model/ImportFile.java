package com.diegocastroviadero.financemanager.app.model;

import com.diegocastroviadero.financemanager.app.services.Importer;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Builder
@Getter
@ToString
public class ImportFile {
    private final File file;
    @Singular
    private final List<Importer> importers;
    private final Bank bank;
    private final String accountNumber;
    private final char[] password;

    public boolean isImportable() {
        return null != importers && !importers.isEmpty();
    }

    public Bank getBank() {
        // All importers will belong to the same bank
        return importers.get(0).getBank();
    }

    public String getImportScope() {
        final Set<ImportScope> importScopes = importers.stream()
                .map(Importer::getImportScope)
                .collect(Collectors.toSet());

        return importScopes.stream()
                .map(ImportScope::name)
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.joining(", "));
    }

    public ImportedFile doImport() {
        final List<String> importErrors = importers.stream()
                .map(importer -> importer.doImport(password, file, bank, accountNumber))
                .flatMap(importedFile -> importedFile.getErrorCauses().stream())
                .collect(Collectors.toList());

        return ImportedFile.builder()
                .file(file)
                .errorCauses(importErrors)
                .build();
    }
}