package com.diegocastroviadero.financemanager.bankscrapper.scrapper.common.model;

import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@ToString
public class DateFilter {
    private final LocalDate from;
    private final String fromD;
    private final String fromM;
    private final String fromY;
    private final LocalDate to;
    private final String toD;
    private final String toM;
    private final String toY;
    private final Map<Year, List<Month>> monthsGroupedByYear;

    public DateFilter(final LocalDate start, final LocalDate end) {
        from = start;
        fromD = Integer.toString(start.getDayOfMonth());
        fromM = Integer.toString(start.getMonthValue());
        fromY = Integer.toString(start.getYear());
        to = end;
        toD = Integer.toString(end.getDayOfMonth());
        toM = Integer.toString(end.getMonthValue());;
        toY = Integer.toString(end.getYear());

        final List<YearMonth> yearMonthList = getYearMonthList(start, end);

        monthsGroupedByYear = yearMonthList.stream()
                .collect(Collectors.groupingBy((ym) -> Year.of(ym.getYear()), Collectors.mapping(YearMonth::getMonth, Collectors.toList())));
    }

    private List<YearMonth> getYearMonthList(final LocalDate start, final LocalDate end) {
        YearMonth currentYearMonth = YearMonth.from(start);
        final YearMonth endYearMonth = YearMonth.from(end);

        final List<YearMonth> yearMonthList = new ArrayList<>();

        do {
            yearMonthList.add(currentYearMonth);
            currentYearMonth = currentYearMonth.plusMonths(1);
        } while (currentYearMonth.isBefore(endYearMonth) || currentYearMonth.equals(endYearMonth));

        return yearMonthList;
    }
}
