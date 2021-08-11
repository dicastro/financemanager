package com.diegocastroviadero.financemanager.consoleutils;

public class ArgumentReaderProvider {
    public static ArgumentReader getInstance() {
        final ArgumentReader instance;

        if (ConsoleArgumentReader.isAvailable()) {
            instance = new ConsoleArgumentReader();
        } else {
            instance = new ScannerArgumentReader();
        }

        return instance;
    }
}
