package com.diegocastroviadero.financemanager.bankscrapper.utils;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class Utils {
    private static final ZoneId ZONE_UTC = ZoneId.of("UTC");

    public static ZoneId getZone() {
        return ZONE_UTC;
    }

    public static ZonedDateTime now() {
        return ZonedDateTime.now(ZONE_UTC);
    }
}
