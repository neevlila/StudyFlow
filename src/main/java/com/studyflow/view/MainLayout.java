package com.studyflow.view;

import com.studyflow.components.TaskFormDialog;
import com.studyflow.service.TaskService;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Main application layout featuring a modern responsive sidebar drawer,
 * sticky glassmorphism navbar, live counters, and dual storage sync.
 */
public class MainLayout extends AppLayout {

    private final TaskService taskService;
    private final H2 viewTitle = new H2("Dashboard");

    // Dynamic Sidenav badge indicators
    private final Span tasksBadge = new Span("0");
    private final Span completedBadge = new Span("0");
    private final Span headerPendingPill = new Span();

    public MainLayout(TaskService taskService) {
        this.taskService = taskService;

        setPrimarySection(Section.NAVBAR);
        addHeaderContent();
        addDrawerContent();
    }

    /**
     * Initializes bidirectional synchronization with browser LocalStorage.
     */
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        if (!taskService.isSessionLoaded()) {
            attachEvent.getUI().getPage().executeJs(
                    "return window.localStorage.getItem('studyflow_tasks') || '';"
            ).then(String.class, json -> {
                if (json != null && !json.trim().isEmpty() && !json.trim().equals("[]")) {
                    taskService.loadSessionTasksFromJson(json);
                } else {
                    // First-time visit: seed initial sample tasks and save to browser LocalStorage
                    String initialJson = taskService.exportSessionTasksToJson();
                    attachEvent.getUI().getPage().executeJs(
                            "window.localStorage.setItem('studyflow_tasks', $0);", initialJson
                    );
                }
                taskService.setSessionLoaded(true);
                updateNavBadges();

                if (getContent() instanceof Refreshable r) {
                    r.refresh();
                }
            });
        } else {
            updateNavBadges();
        }
    }

    public void updateNavBadges() {
        int pending = taskService.getPendingCount();
        int completed = taskService.getCompletedCount();

        tasksBadge.setText(String.valueOf(pending));
        completedBadge.setText(String.valueOf(completed));

        if (pending > 0) {
            headerPendingPill.setText(pending + " Pending");
            headerPendingPill.setVisible(true);
        } else {
            headerPendingPill.setText("All Clear");
            headerPendingPill.setVisible(true);
        }
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Toggle Menu");
        toggle.getStyle().set("cursor", "pointer");

        viewTitle.addClassName("sf-view-title");
        viewTitle.getStyle()
                .set("font-size", "1.2rem")
                .set("font-weight", "700")
                .set("margin", "0")
                .set("color", "#F8FAFC")
                .set("letter-spacing", "-0.01em");

        // Format today's date
        String formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM d"));
        Span dateSpan = new Span(formattedDate);
        dateSpan.addClassName("sf-header-date");
        dateSpan.getStyle()
                .set("color", "#94A3B8")
                .set("font-size", "0.85rem")
                .set("font-weight", "500")
                .set("margin-left", "auto")
                .set("padding-right", "0.75rem");

        // Header Pending workload pill
        headerPendingPill.addClassNames("sf-badge", "sf-badge-pending", "sf-header-date");
        headerPendingPill.getStyle()
                .set("font-size", "0.75rem")
                .set("margin-right", "0.75rem")
                .set("padding", "0.25rem 0.65rem");

        // Global New Task Button
        Button newTaskBtn = new Button("New Task", VaadinIcon.PLUS.create(), e -> {
            TaskFormDialog dialog = new TaskFormDialog(taskService, null, task -> {
                updateNavBadges();
                if (getContent() instanceof Refreshable r) {
                    r.refresh();
                }
            });
            dialog.open();
        });
        newTaskBtn.addClassNames("sf-header-new-task-btn", "sf-primary-btn");
        newTaskBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        newTaskBtn.setTooltipText("Create a new coursework task (Ctrl+N)");

        HorizontalLayout header = new HorizontalLayout(toggle, viewTitle, dateSpan, headerPendingPill, newTaskBtn);
        header.addClassName("sf-header-layout");
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        header.addClassNames(
                LumoUtility.Padding.Vertical.SMALL,
                LumoUtility.Padding.Horizontal.MEDIUM
        );
        header.getStyle()
                .set("margin", "0")
                .set("box-sizing", "border-box");

        addToNavbar(header);
    }

    private void addDrawerContent() {
        // Brand / Logo Section
        HorizontalLayout brandLayout = new HorizontalLayout();
        brandLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        brandLayout.setSpacing(true);
        brandLayout.getStyle().set("padding", "1.25rem 1.25rem 0.5rem 1.25rem");

        Span iconSpan = new Span(VaadinIcon.ACADEMY_CAP.create());
        iconSpan.getStyle()
                .set("font-size", "1.6rem")
                .set("color", "#6366F1");

        H1 appName = new H1("StudyFlow");
        appName.getStyle()
                .set("font-size", "1.3rem")
                .set("font-weight", "800")
                .set("margin", "0")
                .set("color", "#F8FAFC")
                .set("letter-spacing", "-0.02em");

        Span versionBadge = new Span("v1.0");
        versionBadge.getStyle()
                .set("font-size", "0.68rem")
                .set("color", "#94A3B8")
                .set("background", "#1E293B")
                .set("padding", "0.15rem 0.45rem")
                .set("border-radius", "9999px")
                .set("margin-left", "0.25rem");

        brandLayout.add(iconSpan, appName, versionBadge);

        Paragraph tagline = new Paragraph("Student Planner & Coursework Tracker");
        tagline.getStyle()
                .set("color", "#64748B")
                .set("font-size", "0.75rem")
                .set("margin", "0 0 1rem 1.25rem");

        // Dynamic badge counters
        tasksBadge.addClassNames("sf-nav-badge", "pending");
        completedBadge.addClassNames("sf-nav-badge", "completed");

        // Navigation Menu
        SideNav nav = new SideNav();
        SideNavItem dashboardLink = new SideNavItem("Dashboard", DashboardView.class, VaadinIcon.GRID_BIG.create());
        SideNavItem tasksLink = new SideNavItem("My Tasks", TasksView.class, VaadinIcon.TASKS.create());
        tasksLink.setSuffixComponent(tasksBadge);

        SideNavItem completedLink = new SideNavItem("Completed", CompletedTasksView.class, VaadinIcon.CHECK_CIRCLE.create());
        completedLink.setSuffixComponent(completedBadge);

        nav.addItem(dashboardLink, tasksLink, completedLink);
        nav.getStyle().set("padding", "0 0.5rem");

        // Footer in drawer with Storage Engine indicator and Demo Data Reset
        VerticalLayout drawerFooter = new VerticalLayout();
        drawerFooter.setSpacing(true);
        drawerFooter.getStyle()
                .set("padding", "1rem 1.25rem")
                .set("margin-top", "auto")
                .set("border-top", "1px solid #1E293B");

        HorizontalLayout dbStatus = new HorizontalLayout();
        dbStatus.setAlignItems(FlexComponent.Alignment.CENTER);
        dbStatus.setSpacing(true);

        Span statusDot = new Span();
        statusDot.addClassName("sf-status-dot-active");

        Span statusText = new Span("LocalStorage Sync");
        statusText.getStyle()
                .set("color", "#94A3B8")
                .set("font-size", "0.75rem")
                .set("font-weight", "600");

        dbStatus.add(statusDot, statusText);

        Button resetBtn = new Button("Reset Sample Tasks", VaadinIcon.REFRESH.create(), e -> {
            taskService.resetToSampleData();
            updateNavBadges();
            if (getContent() instanceof Refreshable r) {
                r.refresh();
            }
            Notification n = Notification.show("Sample coursework data restored!", 2500, Notification.Position.BOTTOM_START);
            n.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
        });
        resetBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        resetBtn.setWidthFull();
        resetBtn.getStyle()
                .set("color", "#64748B")
                .set("font-size", "0.72rem")
                .set("justify-content", "flex-start")
                .set("padding", "0");

        drawerFooter.add(dbStatus, resetBtn);

        VerticalLayout drawerLayout = new VerticalLayout(brandLayout, tagline, nav, drawerFooter);
        drawerLayout.setSizeFull();
        drawerLayout.setPadding(false);
        drawerLayout.setSpacing(false);

        addToDrawer(new Scroller(drawerLayout));
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        String title = getCurrentPageTitle();
        viewTitle.setText(title);
        updateNavBadges();
    }

    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        if (title == null) return "StudyFlow";
        String val = title.value();
        if (val.contains(" - ")) {
            return val.split(" - ")[0];
        }
        return val;
    }
}
