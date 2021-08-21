package com.diegocastroviadero.financemanager.app.views.position;

import com.diegocastroviadero.financemanager.app.model.Account;
import com.diegocastroviadero.financemanager.app.model.AccountPosition;
import com.diegocastroviadero.financemanager.app.model.AccountPositionHistory;
import com.diegocastroviadero.financemanager.app.services.AccountService;
import com.diegocastroviadero.financemanager.app.services.AuthService;
import com.diegocastroviadero.financemanager.app.utils.IconUtils;
import com.diegocastroviadero.financemanager.app.utils.Utils;
import com.diegocastroviadero.financemanager.app.views.common.AuthDialog;
import com.diegocastroviadero.financemanager.app.views.main.MainView;
import com.diegocastroviadero.financemanager.cryptoutils.exception.CsvCryptoIOException;
import com.diegocastroviadero.financemanager.cryptoutils.exception.WrongEncryptionPasswordException;
import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.builder.*;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.chart.builder.ZoomBuilder;
import com.github.appreciated.apexcharts.config.legend.HorizontalAlign;
import com.github.appreciated.apexcharts.config.stroke.Curve;
import com.github.appreciated.apexcharts.config.subtitle.Align;
import com.github.appreciated.apexcharts.config.xaxis.XAxisType;
import com.github.appreciated.apexcharts.helper.Series;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Route(value = "position", layout = MainView.class)
@PageTitle("Position | Finance Manager")
public class PositionView extends VerticalLayout {

    private final AuthService authService;
    private final AccountService accountService;

    private final Grid<AccountPosition> positionsGrid = new Grid<>(AccountPosition.class);
    private final Div chartRegion = new Div();

    public PositionView(final AuthService authService, final AccountService accountService) {
        this.authService = authService;
        this.accountService = accountService;

        addClassName("position-view");

        add(new H1("Position"));

        configurePositionsGrid();

        final AuthDialog authDialog = authService.configureAuth(this);

        add(positionsGrid, chartRegion, authDialog);

        updateValues();
    }

    private void updateValues() {
        authService.authenticate(this, password -> {
            List<Account> accounts = null;

            try {
                accounts = accountService.getAllAccounts(password);
            } catch (WrongEncryptionPasswordException e) {
                final String errorMessage = "Accounts could not be read because provided encryption password is wrong";
                log.error(errorMessage);
                authService.forgetPassword();
                Notification.show(errorMessage, 5000, Notification.Position.MIDDLE);
            } catch (CsvCryptoIOException e) {
                final String errorMessage = "Accounts could not be read because an unexpected error";
                log.error(errorMessage, e);
                Notification.show(errorMessage, 5000, Notification.Position.MIDDLE);
            }

            final List<AccountPosition> positions = new ArrayList<>();

            if (null != accounts) {
                for (Account account : accounts) {
                    try {
                        positions.add(accountService.getAccountPosition(password, account));
                    } catch (WrongEncryptionPasswordException e) {
                        final String errorMessage = String.format("Balance of account '%s' could not be calculated because provided encryption password is wrong", account.getId());
                        log.error(errorMessage);
                        authService.forgetPassword();
                        Notification.show(errorMessage, 5000, Notification.Position.MIDDLE);
                    } catch (CsvCryptoIOException e) {
                        final String errorMessage = String.format("Balance of account '%s' could not be calculated because an unexpected error", account.getId());
                        log.error(errorMessage, e);
                        Notification.show(errorMessage, 5000, Notification.Position.MIDDLE);
                    }

                }
            }

            positionsGrid.setItems(positions);
        });
    }

    private void configurePositionsGrid() {
        positionsGrid.addClassName("account-position-grid");

        positionsGrid.removeAllColumns();

        positionsGrid.addComponentColumn(IconUtils::getBankIcon);
        positionsGrid.addComponentColumn(IconUtils::getScopeIcon);
        positionsGrid.addColumn(AccountPosition::getAlias)
                .setHeader("Alias");
        positionsGrid.addColumn(accountPosition -> Utils.tableFormatDate(accountPosition.getBalanceDate()))
                .setHeader("Balance date");
        positionsGrid.addColumn(accountPosition -> Utils.tableFormatMoney(accountPosition.getBalance()))
                .setHeader("Balance")
                .setTextAlign(ColumnTextAlign.END);
        positionsGrid.addColumn(AccountPosition::getExtra)
                .setHeader("");

        positionsGrid.getColumns().forEach(column -> column.setAutoWidth(Boolean.TRUE));
        positionsGrid.asSingleSelect().addValueChangeListener(event -> showPositionHistory(event.getValue()));
        positionsGrid.setHeightByRows(Boolean.TRUE);
    }

    private void showPositionHistory(final AccountPosition accountPosition) {
        chartRegion.removeAll();

        if (null != accountPosition) {
            authService.authenticate(this, password -> {
                try {
                    final AccountPositionHistory accountPositionHistory = accountService.getAccountPositionHistory(password, accountPosition);

                    final ApexCharts chart = getChart(accountPositionHistory.getLabels(), getSeriesFromValues(accountPositionHistory));
                    chartRegion.add(chart);
                } catch (WrongEncryptionPasswordException e) {
                    final String errorMessage = String.format("History of account '%s' cannot be shown because provided encryption password is wrong", accountPosition.getAccountId());
                    log.error(errorMessage);
                    authService.forgetPassword();
                    Notification.show(errorMessage, 5000, Notification.Position.MIDDLE);
                    positionsGrid.deselectAll();
                } catch (CsvCryptoIOException e) {
                    final String errorMessage = String.format("History of account '%s' cannot be shown because of an unexpected error", accountPosition.getAccountId());
                    log.error(errorMessage, e);
                    Notification.show(errorMessage, 5000, Notification.Position.MIDDLE);
                    positionsGrid.deselectAll();
                }
            });
        }
    }

    @SafeVarargs
    private ApexCharts getChart(final String[] labels, final Series<Double>... series) {
        return ApexChartsBuilder.get()
                .withChart(ChartBuilder.get()
                        .withType(Type.area)
                        .withZoom(ZoomBuilder.get()
                                .withEnabled(false)
                                .build())
                        .build())
                .withDataLabels(DataLabelsBuilder.get()
                        .withEnabled(false)
                        .build())
                .withStroke(StrokeBuilder.get().withCurve(Curve.straight).build())
                .withTitle(TitleSubtitleBuilder.get()
                        .withText("Historical account balance")
                        .withAlign(Align.left).build())
                .withSeries(series)
                .withLabels(labels)
                .withXaxis(XAxisBuilder.get()
                        .withType(XAxisType.categories).build())
                .withYaxis(YAxisBuilder.get()
                        .withOpposite(true).build())
                .withLegend(LegendBuilder.get().withHorizontalAlign(HorizontalAlign.left).build())
                .build();
    }

    @SuppressWarnings("unchecked")
    private Series<Double>[] getSeriesFromValues(final AccountPositionHistory accountPositionHistory) {
        return accountPositionHistory.getSeries().stream()
                .map(aphs -> new Series<>(aphs.getAlias(), Stream.of(aphs.getValues())
                        .map(BigDecimal::doubleValue)
                        .toArray(Double[]::new)))
                .toArray(Series[]::new);
    }
}
