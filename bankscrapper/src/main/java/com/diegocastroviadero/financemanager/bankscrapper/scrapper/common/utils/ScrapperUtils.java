package com.diegocastroviadero.financemanager.bankscrapper.scrapper.common.utils;

import com.diegocastroviadero.financemanager.bankscrapper.model.SyncType;
import com.diegocastroviadero.financemanager.bankscrapper.scrapper.common.model.DateFilter;

public class ScrapperUtils {
    public static DateFilter newDateFilterFrom(final SyncType syncType) {
        return new DateFilter(syncType.getStartDate(), syncType.getEndDate());
    }
}
