package com.diegocastroviadero.financemanager.app.views.position;

import com.diegocastroviadero.financemanager.app.model.AccountPosition;
import com.diegocastroviadero.financemanager.app.model.AccountPositionHistory;
import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.builder.*;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.chart.builder.ZoomBuilder;
import com.github.appreciated.apexcharts.config.legend.HorizontalAlign;
import com.github.appreciated.apexcharts.config.stroke.Curve;
import com.github.appreciated.apexcharts.config.subtitle.Align;
import com.github.appreciated.apexcharts.config.xaxis.XAxisType;
import com.github.appreciated.apexcharts.config.xaxis.builder.LabelsBuilder;
import com.github.appreciated.apexcharts.helper.Series;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class PositionChart extends VerticalLayout {
    private AccountPositionHistory accountPositionHistory;

    private ApexCharts chart;

    final Div chartRegion = new Div();
    final Button close = new Button("Close");

    final List<ShortcutRegistration> shortcuts = new ArrayList<>();

    public PositionChart() {
        addClassName("position-chart");

        chartRegion.setSizeFull();

        add(chartRegion, createButtonsLayout());
    }

    public void show(final AccountPositionHistory accountPositionHistory) {
        this.accountPositionHistory = accountPositionHistory;

        setVisible(Boolean.TRUE);

        shortcuts.add(close.addClickShortcut(Key.ESCAPE));

        update();
    }

    public void hide() {
        this.accountPositionHistory = null;

        setVisible(Boolean.FALSE);

        shortcuts.forEach(ShortcutRegistration::remove);
        shortcuts.clear();

        update();
    }

    public void rerender() {
        update();
    }

    private void update() {
        if (null != chart) {
            chartRegion.remove(chart);
        }

        if (null != accountPositionHistory) {
            chart = getChart(accountPositionHistory.getLabels(), getSeriesFromValues(accountPositionHistory));
            chartRegion.add(chart);
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
                        .withType(XAxisType.categories)
                        .withLabels(LabelsBuilder.get()
                                .withRotate(-90.0)
                                .withRotateAlways(Boolean.TRUE)
                                .build())
                        .build())
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

    private HorizontalLayout createButtonsLayout() {
        close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        close.addClickListener(event -> fireEvent(new PositionChart.CloseEvent(this)));

        return new HorizontalLayout(close);
    }

    @Override
    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType, ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }

    @Getter
    public static abstract class PositionChartEvent extends ComponentEvent<PositionChart> {
        protected PositionChartEvent(final PositionChart source) {
            super(source, false);
        }
    }

    public static class CloseEvent extends PositionChart.PositionChartEvent {
        CloseEvent(final PositionChart source) {
            super(source);
        }
    }
}
