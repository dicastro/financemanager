package com.diegocastroviadero.financemanager.consoleutils;

import java.nio.file.Path;

public interface ArgumentReader {
    String readString(final String message);
    String readPassword(final String message);
    Path readDir(final String message) throws IllegalArgumentException;
}