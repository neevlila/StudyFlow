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
 * Main application layout featuring a responsive sidebar drawer,
 * top navigation bar with live date, global "+ Add Task" button,
 * and automatic synchronization with the browser's LocalStorage database.
 */
public class MainLayout extends AppLayout {

    private final TaskService taskService;
    private final H2 viewTitle = new H2("Dashboard");

    public MainLayout(TaskService taskService) {
        this.taskService = taskService;

        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        addHeaderContent();
    }

    /**
     * Initializes bidirectional synchronization with browser LocalStorage.
     * When a user visits StudyFlow on Render or locally, their saved tasks are
     * loaded directly from their client browser.
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

                if (getContent() instanceof Refreshable r) {
                    r.refresh();
                }
            });
        }
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Toggle Menu");

        viewTitle.getStyle()
                .set("font-size", "1.25rem")
                .set("font-weight", "600")
                .set("margin", "0")
                .set("color", "#F8FAFC");

        // Format today's date
        String formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy"));
        Span dateSpan = new Span(formattedDate);
        dateSpan.getStyle()
                .set("color", "#94A3B8")
                .set("font-size", "0.875rem")
                .set("margin-left", "auto")
                .set("padding-right", "1rem");

        // Global New Task Button
        Button newTaskBtn = new Button("New Task", VaadinIcon.PLUS.create(), e -> {
            TaskFormDialog dialog = new TaskFormDialog(taskService, null, task -> {
                if (getContent() instanceof Refreshable r) {
                    r.refresh();
                }
            });
            dialog.open();
        });
        newTaskBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        newTaskBtn.getStyle()
                .set("background", "#6366F1")
                .set("font-weight", "600")
                .set("border-radius", "8px")
                .set("cursor", "pointer");

        HorizontalLayout header = new HorizontalLayout(toggle, viewTitle, dateSpan, newTaskBtn);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        header.addClassNames(
                LumoUtility.Padding.Vertical.SMALL,
                LumoUtility.Padding.Horizontal.MEDIUM
        );
        header.getStyle().set("background", "#131E31");

        addToNavbar(true, header);
    }

    private void addDrawerContent() {
        // Brand / Logo Section
        HorizontalLayout brandLayout = new HorizontalLayout();
        brandLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        brandLayout.setSpacing(true);
        brandLayout.getStyle().set("padding", "1.25rem 1rem 0.75rem 1rem");

        Span iconSpan = new Span(VaadinIcon.ACADEMY_CAP.create());
        iconSpan.getStyle()
                .set("font-size", "1.6rem")
                .set("color", "#6366F1");

        H1 appName = new H1("StudyFlow");
        appName.getStyle()
                .set("font-size", "1.25rem")
                .set("font-weight", "700")
                .set("margin", "0")
                .set("color", "#F8FAFC");

        Span versionBadge = new Span("v1.0");
        versionBadge.getStyle()
                .set("font-size", "0.6875rem")
                .set("color", "#94A3B8")
                .set("background", "#1E293B")
                .set("padding", "0.15rem 0.45rem")
                .set("border-radius", "9999px")
                .set("margin-left", "0.25rem");

        brandLayout.add(iconSpan, appName, versionBadge);

        Paragraph tagline = new Paragraph("Student Planner & Assignment Tracker");
        tagline.getStyle()
                .set("color", "#64748B")
                .set("font-size", "0.75rem")
                .set("margin", "0 0 1rem 1rem");

        // Navigation Menu
        SideNav nav = new SideNav();
        SideNavItem dashboardLink = new SideNavItem("Dashboard", DashboardView.class, VaadinIcon.GRID_BIG.create());
        SideNavItem tasksLink = new SideNavItem("My Tasks", TasksView.class, VaadinIcon.TASKS.create());
        SideNavItem completedLink = new SideNavItem("Completed", CompletedTasksView.class, VaadinIcon.CHECK_CIRCLE.create());

        nav.addItem(dashboardLink, tasksLink, completedLink);
        nav.getStyle().set("padding", "0 0.5rem");

        // Footer in drawer with Storage Engine indicator
        VerticalLayout drawerFooter = new VerticalLayout();
        drawerFooter.setSpacing(false);
        drawerFooter.getStyle()
                .set("padding", "1rem")
                .set("margin-top", "auto")
                .set("border-top", "1px solid #1E293B");

        Span dbTitle = new Span("STORAGE ENGINE");
        dbTitle.getStyle()
                .set("font-size", "0.6875rem")
                .set("color", "#64748B")
                .set("font-weight", "700")
                .set("letter-spacing", "0.05em");

        HorizontalLayout dbStatus = new HorizontalLayout();
        dbStatus.setAlignItems(FlexComponent.Alignment.CENTER);
        dbStatus.setSpacing(true);

        Span statusDot = new Span();
        statusDot.getStyle()
                .set("width", "8px")
                .set("height", "8px")
                .set("border-radius", "50%")
                .set("background", "#10B981")
                .set("display", "inline-block");

        Span statusText = new Span("Browser LocalStorage");
        statusText.getStyle()
                .set("color", "#94A3B8")
                .set("font-size", "0.75rem")
                .set("font-weight", "600");

        dbStatus.add(statusDot, statusText);

        Span renderBadge = new Span("Render & Cloud Ready");
        renderBadge.getStyle()
                .set("color", "#6366F1")
                .set("font-size", "0.7rem")
                .set("font-weight", "500")
                .set("margin-top", "0.25rem");

        drawerFooter.add(dbTitle, dbStatus, renderBadge);

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
    }

    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        return title == null ? "StudyFlow" : title.value();
    }
}
