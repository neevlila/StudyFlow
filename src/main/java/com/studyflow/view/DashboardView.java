package com.studyflow.view;

import com.studyflow.components.TaskFormDialog;
import com.studyflow.model.Priority;
import com.studyflow.model.Task;
import com.studyflow.service.TaskService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
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
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Modern StudyFlow Dashboard View.
 * Features an academic overview banner, interactive summary stat cards,
 * visual progress indicator with dynamic encouragement, and responsive upcoming deadlines.
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
    private final Paragraph progressEncouragement = new Paragraph();
    private final Div progressFill = new Div();

    // Upcoming tasks grid
    private final Grid<Task> upcomingGrid = new Grid<>(Task.class, false);
    private final Div emptyUpcomingNotice = new Div();

    public DashboardView(TaskService taskService) {
        this.taskService = taskService;

        setWidthFull();
        setPadding(false);
        setSpacing(false);
        getStyle()
                .set("background-color", "#0B1120")
                .set("padding", "1.15rem clamp(1rem, 2vw, 2rem) 2rem")
                .set("gap", "1rem")
                .set("box-sizing", "border-box");

        // 2. Summary Statistics Cards (Interactive & Responsive)
        add(createStatCardsLayout());

        // 3. Progress Completion Section
        add(createProgressSection());

        // 4. Upcoming Deadlines Section
        add(createUpcomingSection());

        // Initial Data Load
        refresh();
    }

    private Component createStatCardsLayout() {
        Div statsGrid = new Div();
        statsGrid.addClassName("sf-stats-grid");
        statsGrid.setWidthFull();

        statsGrid.add(
                buildStatCard("Total Tasks", totalVal, "All registered coursework", "total", VaadinIcon.TASKS, "View tasks →", () -> UI.getCurrent().navigate(TasksView.class)),
                buildStatCard("Pending", pendingVal, "Active study workload", "pending", VaadinIcon.HOURGLASS_START, "Filter pending →", () -> UI.getCurrent().navigate(TasksView.class)),
                buildStatCard("Completed", completedVal, "Submitted & verified", "completed", VaadinIcon.CHECK_CIRCLE, "View archive →", () -> UI.getCurrent().navigate(CompletedTasksView.class)),
                buildStatCard("Overdue", overdueVal, "Requires immediate attention", "overdue", VaadinIcon.EXCLAMATION_CIRCLE, "Review urgent →", () -> UI.getCurrent().navigate(TasksView.class))
        );

        return statsGrid;
    }

    private Component buildStatCard(String label, Span valueSpan, String subtext, String cssClass, VaadinIcon icon, String actionText, Runnable onClick) {
        Div card = new Div();
        card.addClassNames("sf-stat-card", cssClass);
        card.addClickListener(e -> onClick.run());

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        Span labelSpan = new Span(label);
        labelSpan.addClassName("sf-stat-label");

        Div iconWrapper = new Div(icon.create());
        iconWrapper.addClassNames("sf-stat-icon-wrapper", cssClass);

        header.add(labelSpan, iconWrapper);

        valueSpan.addClassName("sf-stat-value");
        valueSpan.getStyle().set("display", "block").set("margin", "0.65rem 0 0.35rem");

        Paragraph sub = new Paragraph(subtext);
        sub.addClassName("sf-stat-subtext");
        sub.getStyle().set("margin", "0");

        Span actionHint = new Span(actionText);
        actionHint.addClassName("sf-stat-action-hint");

        card.add(header, valueSpan, sub, actionHint);
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
        icon.getStyle().set("color", "#10B981").set("font-size", "1.25rem");

        H3 title = new H3("Semester Progress & Completion");
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "1.1rem")
                .set("font-weight", "700")
                .set("color", "#F8FAFC");
        titleArea.add(icon, title);

        HorizontalLayout metricArea = new HorizontalLayout();
        metricArea.setAlignItems(FlexComponent.Alignment.BASELINE);
        metricArea.setSpacing(true);

        progressPercentText.getStyle()
                .set("font-size", "1.6rem")
                .set("font-weight", "800")
                .set("color", "#10B981");

        progressRatioText.getStyle()
                .set("color", "#94A3B8")
                .set("font-size", "0.85rem")
                .set("font-weight", "500");

        metricArea.add(progressPercentText, progressRatioText);

        header.add(titleArea, metricArea);

        // Custom Styled Progress Bar
        Div progressTrack = new Div();
        progressTrack.addClassName("sf-progress-track");

        progressFill.addClassName("sf-progress-fill");
        progressFill.getStyle().set("width", "0%");
        progressTrack.add(progressFill);

        progressEncouragement.getStyle()
                .set("color", "#64748B")
                .set("font-size", "0.8rem")
                .set("margin", "0.25rem 0 0 0")
                .set("font-style", "italic");

        progressCard.add(header, progressTrack, progressEncouragement);
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
        icon.getStyle().set("color", "#6366F1").set("font-size", "1.2rem");

        H3 title = new H3("Upcoming Deadlines");
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "1.1rem")
                .set("font-weight", "700")
                .set("color", "#F8FAFC");
        titleArea.add(icon, title);

        Button viewAllBtn = new Button("View All Tasks", VaadinIcon.ARROW_RIGHT.create(), e -> {
            UI.getCurrent().navigate(TasksView.class);
        });
        viewAllBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        viewAllBtn.getStyle()
                .set("color", "#818CF8")
                .set("font-weight", "600");

        header.add(titleArea, viewAllBtn);

        configureUpcomingGrid();

        Div gridWrapper = new Div(upcomingGrid);
        gridWrapper.addClassName("sf-grid-wrapper");
        gridWrapper.setWidthFull();

        emptyUpcomingNotice.setText("All caught up! You have no upcoming pending assignments.");
        emptyUpcomingNotice.getStyle()
                .set("color", "#94A3B8")
                .set("padding", "2.5rem 1rem")
                .set("text-align", "center")
                .set("font-style", "italic")
                .set("font-size", "0.95rem");
        emptyUpcomingNotice.setVisible(false);

        section.add(header, gridWrapper, emptyUpcomingNotice);
        return section;
    }

    private void configureUpcomingGrid() {
        upcomingGrid.addClassName("sf-upcoming-grid");
        upcomingGrid.setAllRowsVisible(true);

        // Status checkbox / quick toggle
        upcomingGrid.addComponentColumn(task -> {
            Button checkBtn = new Button(VaadinIcon.CHECK.create(), e -> {
                taskService.toggleStatus(task.getId());
                refresh();
                updateParentLayout();
                Notification n = Notification.show("Task marked as completed!", 2000, Notification.Position.TOP_END);
                n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            });
            checkBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            checkBtn.setTooltipText("Click to mark as done");
            checkBtn.getStyle()
                    .set("color", "#64748B")
                    .set("border", "1px solid #334155")
                    .set("border-radius", "6px")
                    .set("padding", "0.3rem");
            return checkBtn;
        }).setHeader("Done").setWidth("78px").setFlexGrow(0);

        // Task Title & Description + Responsive Mobile Metadata embedded row
        upcomingGrid.addComponentColumn(task -> {
            VerticalLayout vl = new VerticalLayout();
            vl.setPadding(false);
            vl.setSpacing(false);

            Span title = new Span(task.getTitle());
            title.getStyle()
                    .set("font-weight", "600")
                    .set("color", "#F8FAFC")
                    .set("font-size", "0.92rem");

            vl.add(title);

            if (task.getDescription() != null && !task.getDescription().isEmpty()) {
                Span desc = new Span(task.getDescription());
                desc.getStyle()
                        .set("font-size", "0.76rem")
                        .set("color", "#94A3B8")
                        .set("margin-top", "0.15rem");
                vl.add(desc);
            }

            // Mobile-only metadata badges row (Subject, Date, Priority)
            HorizontalLayout mobileMeta = new HorizontalLayout();
            mobileMeta.addClassName("sf-mobile-meta");
            mobileMeta.setSpacing(true);

            Span subjectBadge = new Span(task.getSubject());
            subjectBadge.addClassName("sf-badge-subject");

            Span dateBadge = createDateBadge(task);

            Priority p = task.getPriority() != null ? task.getPriority() : Priority.MEDIUM;
            Span priorityBadge = new Span(p.getDisplayName());
            priorityBadge.addClassNames("sf-badge", "sf-badge-" + p.name().toLowerCase());

            mobileMeta.add(subjectBadge, dateBadge, priorityBadge);
            vl.add(mobileMeta);

            return vl;
        }).setHeader("Task Title").setFlexGrow(3);

        // Subject (Desktop column)
        upcomingGrid.addComponentColumn(task -> {
            Span subject = new Span(task.getSubject());
            subject.addClassName("sf-badge-subject");
            return subject;
        }).setHeader("Subject").setWidth("140px").setFlexGrow(0).addClassNames("sf-col-desktop");

        // Due Date & urgency badge (Desktop column)
        upcomingGrid.addComponentColumn(task -> {
            HorizontalLayout hl = new HorizontalLayout();
            hl.setAlignItems(FlexComponent.Alignment.CENTER);
            hl.setSpacing(true);

            String formattedDate = task.getDueDate() != null ?
                    task.getDueDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "No date";
            Span dateSpan = new Span(formattedDate);
            dateSpan.getStyle().set("font-size", "0.85rem");

            Span urgencyBadge = createUrgencyBadge(task);
            if (urgencyBadge != null) {
                hl.add(dateSpan, urgencyBadge);
            } else {
                hl.add(dateSpan);
            }
            return hl;
        }).setHeader("Due Date").setWidth("210px").setFlexGrow(1).addClassNames("sf-col-desktop");

        // Priority Badge (Desktop column)
        upcomingGrid.addComponentColumn(task -> {
            Priority p = task.getPriority() != null ? task.getPriority() : Priority.MEDIUM;
            Span badge = new Span(p.getDisplayName());
            badge.addClassName("sf-badge");
            badge.addClassName("sf-badge-" + p.name().toLowerCase());
            return badge;
        }).setHeader("Priority").setWidth("110px").setFlexGrow(0).addClassNames("sf-col-desktop");
    }

    private Span createDateBadge(Task task) {
        String formattedDate = task.getDueDate() != null ?
                task.getDueDate().format(DateTimeFormatter.ofPattern("MMM d")) : "No date";

        Span badge = new Span(formattedDate);
        if (task.isOverdue()) {
            badge.setText("OVERDUE (" + formattedDate + ")");
            badge.addClassNames("sf-badge", "sf-badge-overdue");
        } else if (task.isDueToday()) {
            badge.setText("TODAY");
            badge.addClassNames("sf-badge", "sf-badge-today");
        } else if (task.isDueSoon()) {
            badge.setText("SOON (" + formattedDate + ")");
            badge.addClassNames("sf-badge", "sf-badge-soon");
        } else {
            badge.addClassName("sf-badge-subject");
        }
        return badge;
    }

    private Span createUrgencyBadge(Task task) {
        if (task.isOverdue()) {
            Span overdueBadge = new Span("OVERDUE");
            overdueBadge.addClassNames("sf-badge", "sf-badge-overdue");
            return overdueBadge;
        } else if (task.isDueToday()) {
            Span todayBadge = new Span("TODAY");
            todayBadge.addClassNames("sf-badge", "sf-badge-today");
            return todayBadge;
        } else if (task.isDueSoon()) {
            Span soonBadge = new Span("SOON");
            soonBadge.addClassNames("sf-badge", "sf-badge-soon");
            return soonBadge;
        }
        return null;
    }

    private void updateParentLayout() {
        if (getParent().isPresent() && getParent().get() instanceof MainLayout ml) {
            ml.updateNavBadges();
        }
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
        int dueToday = taskService.getDueTodayCount();

        totalVal.setText(String.valueOf(total));
        pendingVal.setText(String.valueOf(pending));
        completedVal.setText(String.valueOf(completed));
        overdueVal.setText(String.valueOf(overdue));

        progressPercentText.setText(percentage + "%");
        progressRatioText.setText(completed + " of " + total + " tasks completed");
        progressFill.getStyle().set("width", percentage + "%");

        if (percentage == 100) {
            progressEncouragement.setText("🎉 Perfect score! Every registered assignment is submitted.");
        } else if (percentage >= 70) {
            progressEncouragement.setText("🚀 Almost there! Keep up this great momentum.");
        } else if (percentage >= 40) {
            progressEncouragement.setText("📚 Solid progress! Stay on track with upcoming deadlines.");
        } else {
            progressEncouragement.setText("⚡ Time to dive in! Complete urgent assignments first.");
        }

        List<Task> upcoming = taskService.getUpcomingTasks(6);
        upcomingGrid.setItems(upcoming);

        boolean hasUpcoming = !upcoming.isEmpty();
        upcomingGrid.setVisible(hasUpcoming);
        emptyUpcomingNotice.setVisible(!hasUpcoming);
    }
}
