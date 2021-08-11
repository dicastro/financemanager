package com.diegocastroviadero.financemanager.bankscrapper.scrapper.kb.keyboard;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class Offset {
    private final int x;
    private final int y;
}
