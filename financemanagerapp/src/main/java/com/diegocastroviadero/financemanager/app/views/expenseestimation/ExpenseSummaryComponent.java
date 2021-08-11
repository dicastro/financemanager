package com.diegocastroviadero.financemanager.app.views.expenseestimation;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.math.BigDecimal;

public class ExpenseSummaryComponent extends VerticalLayout {
    private BigDecimal quantity;

    private final Span quantitySpan = new Span();

    public ExpenseSummaryComponent() {
        addClassName("expense-estimation-summary");

        this.quantity = BigDecimal.ZERO;

        final HorizontalLayout horizontalLayout = new HorizontalLayout(new H2("Total"), quantitySpan);
        horizontalLayout.setDefaultVerticalComponentAlignment(Alignment.BASELINE);

        refresh();

        add(horizontalLayout);
    }

    public void setQuantity(final BigDecimal quantity) {
        this.quantity = quantity;
        refresh();
    }

    private void refresh() {
        quantitySpan.setText(quantity.toString());
    }
}
