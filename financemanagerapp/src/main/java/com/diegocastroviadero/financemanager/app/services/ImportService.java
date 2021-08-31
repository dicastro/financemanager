package com.diegocastroviadero.financemanager.app.services;

import com.diegocastroviadero.financemanager.app.configuration.ImportProperties;
import com.diegocastroviadero.financemanager.app.model.ImportFile;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@AllArgsConstructor
@Service
public class ImportService {
    private final ImportProperties importProperties;
    private final List<Importer> importers;

    public String extractAccountNumberFromFilename(final String filename) {
        final Pattern p = Pattern.compile("(?:import_|movimientos_)?(?:[a-z]+_)?((?:ES)?[0-9 ]+)(?:_[0-9]{6})?(?:\\.csv|\\.xls)");
        final Matcher m = p.matcher(filename);

        String accountNumber = null;

        if (m.matches()) {
            accountNumber = m.group(1);
        }

        return accountNumber;
    }

    public List<ImportFile> getFilesToImport(final char[] password) {
        final List<ImportFile> result;

        final File[] filesInImportPath = importProperties.getBasePath().toFile().listFiles(File::isFile);

        if (null == filesInImportPath) {
            result = Collections.emptyList();
        } else {
            result = new ArrayList<>();

            for (File file : filesInImportPath) {
                ImportFile.ImportFileBuilder importFileBuilder = null;

                for (Importer importer : importers) {
                    if (importer.applies(file)) {
                        if (null == importFileBuilder) {
                            importFileBuilder = ImportFile.builder()
                                    .file(file)
                                    .bank(importer.getBank())
                                    .accountNumber(importer.getAccountNumber(file))
                                    .password(password)
                                    .importer(importer);
                        } else {
                            importFileBuilder
                                    .importer(importer);
                        }
                    }
                }

                final ImportFile importFile;

                if (null == importFileBuilder) {
                    importFile = ImportFile.builder()
                            .file(file)
                            .build();
                } else {
                    importFile = importFileBuilder
                            .build();
                }

                result.add(importFile);
            }
        }

        return result;
    }
}
