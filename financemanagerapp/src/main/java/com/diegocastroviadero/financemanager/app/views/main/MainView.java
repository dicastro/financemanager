package com.diegocastroviadero.financemanager.app.views.main;

import com.diegocastroviadero.financemanager.app.configuration.CacheProperties;
import com.diegocastroviadero.financemanager.app.services.AuthCleanerService;
import com.diegocastroviadero.financemanager.app.services.AuthCleanerThread;
import com.diegocastroviadero.financemanager.app.services.UserConfigService;
import com.diegocastroviadero.financemanager.app.views.accounts.AccountsView;
import com.diegocastroviadero.financemanager.app.views.administration.AdministrationView;
import com.diegocastroviadero.financemanager.app.views.expenseestimation.ExpenseEstimationView;
import com.diegocastroviadero.financemanager.app.views.imports.ImportView;
import com.diegocastroviadero.financemanager.app.views.movements.MovementsView;
import com.diegocastroviadero.financemanager.app.views.plannedbudgets.PlannedBudgetsView;
import com.diegocastroviadero.financemanager.app.views.plannedexpenses.PlannedExpensesView;
import com.diegocastroviadero.financemanager.app.views.position.PositionView;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.TabsVariant;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.Theme;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

@Slf4j
@PWA(name = "FinanceManager", shortName = "FM", enableInstallPrompt = false)
@Theme(themeFolder = "financemanager")
public class MainView extends AppLayout {

    private final CacheProperties cacheProperties;
    private final UserConfigService userConfigService;
    private final AuthCleanerService authCleanerService;
    private final ApplicationEventPublisher applicationEventPublisher;

    private final Tabs menu;
    private final H1 viewTitle;

    public MainView(final CacheProperties cacheProperties, final UserConfigService userConfigService, final AuthCleanerService authCleanerService, final ApplicationEventPublisher applicationEventPublisher) {
        this.cacheProperties = cacheProperties;
        this.userConfigService = userConfigService;
        this.authCleanerService = authCleanerService;
        this.applicationEventPublisher = applicationEventPublisher;

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

        final DrawerToggle drawerToggle = new DrawerToggle();
        drawerToggle.addClickListener(event -> applicationEventPublisher.publishEvent(new DrawerToggleEvent(this, isDrawerOpened())));

        layout.add(drawerToggle);

        layout.add(viewTitle);
        layout.add(new Avatar());

        final Anchor logout = new Anchor("logout", new Icon(VaadinIcon.SIGN_OUT));
        logout.setClassName("logout-link");

        layout.add(logout);

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
                createTab("Import", ImportView.class),
                createTab("Administration", AdministrationView.class)
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
        logoLayout.add(new Image("images/logo.png", "Finance Manager logo"));
        logoLayout.add(new H1("Finance Manager"));

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
    protected void onAttach(final AttachEvent attachEvent) {
        final VaadinSession session = attachEvent.getSession();

        session.access(() -> {
            if (null == session.getAttribute(AuthCleanerThread.class)) {
                session.getSession()
                        .setMaxInactiveInterval(-1);

                final AuthCleanerThread authCleanerThread = new AuthCleanerThread(cacheProperties, authCleanerService, session);
                session.setAttribute(AuthCleanerThread.class, authCleanerThread);
                authCleanerThread.start();

                log.info("Clean of session '{}' scheduled each {} millis", session.getSession().getId(), cacheProperties.getCleanInterval());
            }
        });
    }

    @Override
    protected void onDetach(final DetachEvent detachEvent) {
        final VaadinSession session = detachEvent.getSession();

        session.access(() -> {
            final AuthCleanerThread authCleanerThread = session.getAttribute(AuthCleanerThread.class);

            if (null != authCleanerThread) {
                authCleanerThread.interrupt();
                session.setAttribute(AuthCleanerThread.class, null);
            }
        });
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
        final PageTitle pageTitle = getContent().getClass().getAnnotation(PageTitle.class);

        final String title;

        if (null == pageTitle) {
            title = "";
        } else {
            title = pageTitle.value().split("\\|")[0].trim();
        }

        return String.format("%s%s", userConfigService.isDemoMode() ? "[DEMO] " : "", title);
    }
}
