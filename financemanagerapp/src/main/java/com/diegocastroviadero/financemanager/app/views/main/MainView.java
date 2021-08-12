package com.diegocastroviadero.financemanager.app.views.main;

import com.diegocastroviadero.financemanager.app.services.UserConfigService;
import com.diegocastroviadero.financemanager.app.views.expenseestimation.ExpenseEstimationView;
import com.diegocastroviadero.financemanager.app.views.imports.ImportsView;
import com.diegocastroviadero.financemanager.app.views.movements.MovementsView;
import com.diegocastroviadero.financemanager.app.views.plannedbudgets.PlannedBudgetsView;
import com.diegocastroviadero.financemanager.app.views.plannedexpenses.PlannedExpensesView;
import com.diegocastroviadero.financemanager.app.views.position.PositionView;
import com.diegocastroviadero.financemanager.app.views.accounts.AccountsView;
import com.diegocastroviadero.financemanager.app.views.userconfig.UserConfigView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.TabsVariant;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;

import java.util.Optional;

@PWA(name = "FinanceManager", shortName = "FinanceManager", enableInstallPrompt = false)
@Theme(themeFolder = "financemanager")
public class MainView extends AppLayout {

    private final UserConfigService userConfigService;

    private final Tabs menu;
    private final H1 viewTitle;

    public MainView(final UserConfigService userConfigService) {
        this.userConfigService = userConfigService;

        menu = createMenu();
        viewTitle = new H1();

        setPrimarySection(Section.DRAWER);
        addToNavbar(true, createHeaderContent());
        addToDrawer(createDrawerContent());
    }

    private Component createHeaderContent() {
        final HorizontalLayout layout = new HorizontalLayout();
        layout.setId("header");
        layout.getThemeList().set("dark", true);
        layout.setWidthFull();
        layout.setSpacing(false);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.add(new DrawerToggle());
        layout.add(viewTitle);
        layout.add(new Avatar());

        return layout;
    }

    private Tabs createMenu() {
        final Tabs tabs = new Tabs();
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        tabs.addThemeVariants(TabsVariant.LUMO_MINIMAL);
        tabs.setId("tabs");
        tabs.add(createMenuItems());

        return tabs;
    }

    private Component[] createMenuItems() {
        return new Tab[]{
                createTab("Expense Estimation", ExpenseEstimationView.class),
                createTab("Planned Expenses", PlannedExpensesView.class),
                createTab("Planned Budgets", PlannedBudgetsView.class),
                createTab("Position", PositionView.class),
                createTab("Movements", MovementsView.class),
                createTab("Accounts", AccountsView.class),
                createTab("Imports", ImportsView.class),
                createTab("Configuration", UserConfigView.class)
        };
    }

    private static Tab createTab(final String text, final Class<? extends Component> navigationTarget) {
        final Tab tab = new Tab();
        tab.add(new RouterLink(text, navigationTarget));
        ComponentUtil.setData(tab, Class.class, navigationTarget);

        return tab;
    }

    private Component createDrawerContent() {
        final HorizontalLayout logoLayout = new HorizontalLayout();
        logoLayout.setId("logo");
        logoLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        logoLayout.add(new Image("images/logo.png", "FinanceManager logo"));
        logoLayout.add(new H1("FinanceManager"));

        final VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.getThemeList().set("spacing-s", true);
        layout.setAlignItems(FlexComponent.Alignment.STRETCH);
        layout.add(logoLayout, menu);

        return layout;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        getTabForComponent(getContent())
                .ifPresent(menu::setSelectedTab);

        viewTitle.setText(getCurrentPageTitle());
    }

    private Optional<Tab> getTabForComponent(Component component) {
        return menu.getChildren()
                .filter(tab -> ComponentUtil.getData(tab, Class.class).equals(component.getClass()))
                .findFirst()
                .map(Tab.class::cast);
    }

    private String getCurrentPageTitle() {
        final PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);

        return String.format("%s%s", userConfigService.isDemoMode() ? "[DEMO] " : "", title != null ? title.value() : "");
    }
}
