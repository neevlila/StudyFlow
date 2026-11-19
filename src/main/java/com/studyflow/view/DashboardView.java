package com.studyflow.view;

import com.studyflow.components.TaskFormDialog;
import com.studyflow.model.Priority;
import com.studyflow.model.Task;
import com.studyflow.model.TaskStatus;
import com.studyflow.service.TaskService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Main StudyFlow Dashboard View.
 * Displays key summary statistics cards, progress completion bar,
 * and upcoming deadlines sorted by urgency.
 */
@Route(value = "", layout = MainLayout.class)
@PageTitle("Dashboard - StudyFlow")
public class DashboardView extends VerticalLayout implements Refreshable {

    private final TaskService taskService;

    // Stat card value labels
    private final Span totalVal = new Span("0");
    private final Span pendingVal = new Span("0");
    private final Span completedVal = new Span("0");
    private final Span overdueVal = new Span("0");

    // Progress components
    private final Span progressPercentText = new Span("0%");
    private final Span progressRatioText = new Span("0 of 0 completed");
    private final ProgressBar progressBar = new ProgressBar();

    // Upcoming tasks grid
    private final Grid<Task> upcomingGrid = new Grid<>(Task.class, false);
    private final Div emptyUpcomingNotice = new Div();

    public DashboardView(TaskService taskService) {
        this.taskService = taskService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle().set("background-color", "#0F172A");

        // 1. Welcome Header Banner
        add(createWelcomeBanner());

        // 2. Summary Statistics Cards
        add(createStatCardsLayout());

        // 3. Progress Completion Section
        add(createProgressSection());

        // 4. Upcoming Deadlines Section
        add(createUpcomingSection());

        // Initial Data Load
        refresh();
    }

    private Component createWelcomeBanner() {
        HorizontalLayout banner = new HorizontalLayout();
        banner.setWidthFull();
        banner.addClassName("sf-welcome-banner");
        banner.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        banner.setAlignItems(FlexComponent.Alignment.CENTER);
        banner.getStyle()
                .set("background", "linear-gradient(135deg, #1E293B 0%, #162032 100%)")
                .set("padding", "1.25rem 1.5rem")
                .set("border-radius", "12px")
                .set("border", "1px solid #334155")
                .set("margin-bottom", "0.5rem");

        VerticalLayout textLayout = new VerticalLayout();
        textLayout.setPadding(false);
        textLayout.setSpacing(false);

        H2 greeting = new H2("Academic Overview & Tasks");
        greeting.getStyle()
                .set("font-size", "1.35rem")
                .set("font-weight", "700")
                .set("color", "#F8FAFC")
                .set("margin", "0 0 0.25rem 0");

        Paragraph sub = new Paragraph("Track deadlines, prioritize coursework, and monitor study progress.");
        sub.getStyle()
                .set("color", "#94A3B8")
                .set("font-size", "0.875rem")
                .set("margin", "0");

        textLayout.add(greeting, sub);

        HorizontalLayout actions = new HorizontalLayout();
        actions.addClassName("sf-banner-actions");
        Button addTaskBtn = new Button("Add Assignment", VaadinIcon.PLUS.create(), e -> {
            TaskFormDialog dialog = new TaskFormDialog(taskService, null, t -> refresh());
            dialog.open();
        });
        addTaskBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addTaskBtn.getStyle().set("background", "#6366F1");

        actions.add(addTaskBtn);

        banner.add(textLayout, actions);
        return banner;
    }

    private Component createStatCardsLayout() {
        Div statsGrid = new Div();
        statsGrid.addClassName("sf-stats-grid");

        statsGrid.add(
                buildStatCard("Total Tasks", totalVal, "All registered coursework", "total", VaadinIcon.TASKS, "#6366F1"),
                buildStatCard("Pending", pendingVal, "Active study workload", "pending", VaadinIcon.HOURGLASS_START, "#F59E0B"),
                buildStatCard("Completed", completedVal, "Submitted & verified", "completed", VaadinIcon.CHECK_CIRCLE, "#10B981"),
                buildStatCard("Overdue", overdueVal, "Requires immediate attention", "overdue", VaadinIcon.EXCLAMATION_CIRCLE, "#EF4444")
        );

        return statsGrid;
    }

    private Component buildStatCard(String label, Span valueSpan, String subtext, String cssClass, VaadinIcon icon, String color) {
        VerticalLayout card = new VerticalLayout();
        card.addClassNames("sf-stat-card", cssClass);
        card.setPadding(false);
        card.setSpacing(false);

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        Span labelSpan = new Span(label);
        labelSpan.addClassName("sf-stat-label");

        Span iconSpan = new Span(icon.create());
        iconSpan.getStyle().set("color", color);

        header.add(labelSpan, iconSpan);

        valueSpan.addClassName("sf-stat-value");
        valueSpan.getStyle().set("margin", "0.35rem 0");

        Span sub = new Span(subtext);
        sub.addClassName("sf-stat-subtext");

        card.add(header, valueSpan, sub);
        return card;
    }

    private Component createProgressSection() {
        VerticalLayout progressCard = new VerticalLayout();
        progressCard.addClassName("sf-card");
        progressCard.setWidthFull();
        progressCard.setPadding(true);
        progressCard.setSpacing(true);

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        HorizontalLayout titleArea = new HorizontalLayout();
        titleArea.setAlignItems(FlexComponent.Alignment.CENTER);
        Span icon = new Span(VaadinIcon.CHART_TIMELINE.create());
        icon.getStyle().set("color", "#10B981");

        H3 title = new H3("Study Completion Progress");
        title.getStyle().set("margin", "0").set("font-size", "1.1rem").set("color", "#F8FAFC");
        titleArea.add(icon, title);

        HorizontalLayout metricArea = new HorizontalLayout();
        metricArea.setAlignItems(FlexComponent.Alignment.BASELINE);
        metricArea.setSpacing(true);

        progressPercentText.getStyle()
                .set("font-size", "1.5rem")
                .set("font-weight", "700")
                .set("color", "#10B981");

        progressRatioText.getStyle()
                .set("color", "#94A3B8")
                .set("font-size", "0.85rem");

        metricArea.add(progressPercentText, progressRatioText);

        header.add(titleArea, metricArea);

        progressBar.setWidthFull();
        progressBar.setMin(0);
        progressBar.setMax(100);
        progressBar.getStyle().set("height", "10px").set("border-radius", "6px");

        progressCard.add(header, progressBar);
        return progressCard;
    }

    private Component createUpcomingSection() {
        VerticalLayout section = new VerticalLayout();
        section.addClassName("sf-card");
        section.setWidthFull();
        section.setPadding(true);
        section.setSpacing(true);

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        HorizontalLayout titleArea = new HorizontalLayout();
        titleArea.setAlignItems(FlexComponent.Alignment.CENTER);
        Span icon = new Span(VaadinIcon.CALENDAR_CLOCK.create());
        icon.getStyle().set("color", "#6366F1");

        H3 title = new H3("Upcoming Deadlines");
        title.getStyle().set("margin", "0").set("font-size", "1.1rem").set("color", "#F8FAFC");
        titleArea.add(icon, title);

        Button viewAllBtn = new Button("View All Tasks", VaadinIcon.ARROW_RIGHT.create(), e -> {
            getUI().ifPresent(ui -> ui.navigate(TasksView.class));
        });
        viewAllBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        viewAllBtn.getStyle().set("color", "#818CF8");

        header.add(titleArea, viewAllBtn);

        configureUpcomingGrid();

        emptyUpcomingNotice.setText("All caught up! You have no upcoming pending assignments.");
        emptyUpcomingNotice.getStyle()
                .set("color", "#94A3B8")
                .set("padding", "2rem")
                .set("text-align", "center")
                .set("font-style", "italic");
        emptyUpcomingNotice.setVisible(false);

        section.add(header, upcomingGrid, emptyUpcomingNotice);
        return section;
    }

    private void configureUpcomingGrid() {
        upcomingGrid.setAllRowsVisible(true);

        // Status checkbox / quick toggle
        upcomingGrid.addComponentColumn(task -> {
            Button checkBtn = new Button(VaadinIcon.CHECK.create(), e -> {
                taskService.toggleStatus(task.getId());
                refresh();
                Notification n = Notification.show("Task marked as completed!", 2000, Notification.Position.TOP_END);
                n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            });
            checkBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            checkBtn.setTooltipText("Mark as completed");
            checkBtn.getStyle().set("color", "#64748B");
            return checkBtn;
        }).setHeader("Done").setWidth("70px").setFlexGrow(0);

        // Task Title & Description
        upcomingGrid.addComponentColumn(task -> {
            VerticalLayout vl = new VerticalLayout();
            vl.setPadding(false);
            vl.setSpacing(false);

            Span title = new Span(task.getTitle());
            title.getStyle().set("font-weight", "600").set("color", "#F8FAFC");

            if (task.getDescription() != null && !task.getDescription().isEmpty()) {
                Span desc = new Span(task.getDescription());
                desc.getStyle().set("font-size", "0.75rem").set("color", "#94A3B8");
                vl.add(title, desc);
            } else {
                vl.add(title);
            }
            return vl;
        }).setHeader("Task Title").setFlexGrow(3);

        // Subject
        upcomingGrid.addComponentColumn(task -> {
            Span subject = new Span(task.getSubject());
            subject.addClassName("sf-badge-subject");
            return subject;
        }).setHeader("Subject").setWidth("130px").setFlexGrow(0);

        // Due Date & urgency badge
        upcomingGrid.addComponentColumn(task -> {
            HorizontalLayout hl = new HorizontalLayout();
            hl.setAlignItems(FlexComponent.Alignment.CENTER);
            hl.setSpacing(true);

            String formattedDate = task.getDueDate() != null ?
                    task.getDueDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "No date";
            Span dateSpan = new Span(formattedDate);
            dateSpan.getStyle().set("font-size", "0.85rem");

            if (task.isOverdue()) {
                Span overdueBadge = new Span("OVERDUE");
                overdueBadge.addClassNames("sf-badge", "sf-badge-overdue");
                hl.add(dateSpan, overdueBadge);
            } else if (task.isDueToday()) {
                Span todayBadge = new Span("TODAY");
                todayBadge.addClassNames("sf-badge", "sf-badge-high");
                hl.add(dateSpan, todayBadge);
            } else if (task.isDueSoon()) {
                Span soonBadge = new Span("SOON");
                soonBadge.addClassNames("sf-badge", "sf-badge-medium");
                hl.add(dateSpan, soonBadge);
            } else {
                hl.add(dateSpan);
            }
            return hl;
        }).setHeader("Due Date").setWidth("210px").setFlexGrow(1);

        // Priority Badge
        upcomingGrid.addComponentColumn(task -> {
            Priority p = task.getPriority() != null ? task.getPriority() : Priority.MEDIUM;
            Span badge = new Span(p.getDisplayName());
            badge.addClassName("sf-badge");
            badge.addClassName("sf-badge-" + p.name().toLowerCase());
            return badge;
        }).setHeader("Priority").setWidth("110px").setFlexGrow(0);
    }

    /**
     * Refreshes all statistics, recalculates completion metrics,
     * and reloads the upcoming tasks list.
     */
    @Override
    public void refresh() {
        int total = taskService.getTotalCount();
        int pending = taskService.getPendingCount();
        int completed = taskService.getCompletedCount();
        int overdue = taskService.getOverdueCount();
        int percentage = taskService.getCompletionPercentage();

        totalVal.setText(String.valueOf(total));
        pendingVal.setText(String.valueOf(pending));
        completedVal.setText(String.valueOf(completed));
        overdueVal.setText(String.valueOf(overdue));

        progressPercentText.setText(percentage + "%");
        progressRatioText.setText(completed + " of " + total + " tasks completed");
        progressBar.setValue(percentage);

        List<Task> upcoming = taskService.getUpcomingTasks(6);
        upcomingGrid.setItems(upcoming);

        boolean hasUpcoming = !upcoming.isEmpty();
        upcomingGrid.setVisible(hasUpcoming);
        emptyUpcomingNotice.setVisible(!hasUpcoming);
    }
}
