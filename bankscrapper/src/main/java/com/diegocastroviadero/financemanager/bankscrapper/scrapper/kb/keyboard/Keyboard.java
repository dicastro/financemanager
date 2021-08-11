package com.diegocastroviadero.financemanager.bankscrapper.scrapper.kb.keyboard;

import lombok.Builder;

import java.util.Map;
import java.util.stream.Stream;

@Builder
public class Keyboard {
    private final Map<Integer, Offset> digits;

    public Stream<Offset> getSequenceOfPassword(final String password) {
        return Stream.of(password.split(""))
                .map(Integer::parseInt)
                .map(digits::get);
    }
}
