package com.diegocastroviadero.financemanager.app.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.List;

@Builder
@Getter
public class ImportedFile {
    private final File file;
    @Singular
    private final List<String> errorCauses;

    public boolean hasError() {
        return null != errorCauses && !errorCauses.isEmpty();
    }
}
