package com.studyflow.view;

import com.studyflow.components.ConfirmDeleteDialog;
import com.studyflow.components.TaskFormDialog;
import com.studyflow.model.Priority;
import com.studyflow.model.Task;
import com.studyflow.model.TaskStatus;
import com.studyflow.service.TaskService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Tasks Management View.
 * Provides live search, status and priority filtering, full CRUD actions,
 * status toggling, and clean responsive table display.
 */
@Route(value = "tasks", layout = MainLayout.class)
@PageTitle("My Tasks - StudyFlow")
public class TasksView extends VerticalLayout implements Refreshable {

    private final TaskService taskService;

    // Filters
    private final TextField searchField = new TextField();
    private final ComboBox<String> statusFilter = new ComboBox<>();
    private final ComboBox<String> priorityFilter = new ComboBox<>();
    private final Button resetFilterBtn = new Button(VaadinIcon.CLOSE_SMALL.create());

    // Task Grid and Empty State
    private final Grid<Task> taskGrid = new Grid<>(Task.class, false);
    private final VerticalLayout emptyStateLayout = new VerticalLayout();
    private final Span emptyStateTitle = new Span();
    private final Paragraph emptyStateDesc = new Paragraph();

    public TasksView(TaskService taskService) {
        this.taskService = taskService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle().set("background-color", "#0F172A");

        // 1. Toolbar (Search, Filter, New Task button)
        add(createToolbar());

        // 2. Table Grid
        configureGrid();
        add(taskGrid);

        // 3. Empty State Layout
        configureEmptyState();
        add(emptyStateLayout);

        // Initial Data Load
        refresh();
    }

    private HorizontalLayout createToolbar() {
        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setWidthFull();
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        toolbar.setSpacing(true);
        toolbar.getStyle()
                .set("background", "#1E293B")
                .set("padding", "0.85rem 1.25rem")
                .set("border-radius", "12px")
                .set("border", "1px solid #334155");

        // Search Field
        searchField.setPlaceholder("Search assignments or subjects...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.setWidth("280px");
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.setValueChangeTimeout(250);
        searchField.addValueChangeListener(e -> applyFilters());

        // Status Filter
        statusFilter.setPlaceholder("Status");
        statusFilter.setItems("All Statuses", "Pending", "Completed");
        statusFilter.setValue("All Statuses");
        statusFilter.setWidth("150px");
        statusFilter.addValueChangeListener(e -> applyFilters());

        // Priority Filter
        priorityFilter.setPlaceholder("Priority");
        priorityFilter.setItems("All Priorities", "High", "Medium", "Low");
        priorityFilter.setValue("All Priorities");
        priorityFilter.setWidth("150px");
        priorityFilter.addValueChangeListener(e -> applyFilters());

        // Reset Filter Button
        resetFilterBtn.setTooltipText("Reset Filters");
        resetFilterBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        resetFilterBtn.addClickListener(e -> resetFilters());

        // Add Task Action
        Button addTaskBtn = new Button("New Task", VaadinIcon.PLUS.create(), e -> {
            TaskFormDialog dialog = new TaskFormDialog(taskService, null, t -> refresh());
            dialog.open();
        });
        addTaskBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addTaskBtn.getStyle()
                .set("background", "#6366F1")
                .set("margin-left", "auto");

        toolbar.add(searchField, statusFilter, priorityFilter, resetFilterBtn, addTaskBtn);
        return toolbar;
    }

    private void configureGrid() {
        taskGrid.setSizeFull();

        // 1. Status Toggle Column
        taskGrid.addComponentColumn(task -> {
            boolean isCompleted = task.getStatus() == TaskStatus.COMPLETED;
            Button toggleBtn = new Button(isCompleted ? VaadinIcon.CHECK_CIRCLE.create() : VaadinIcon.CIRCLE_THIN.create());
            toggleBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
            toggleBtn.setTooltipText(isCompleted ? "Mark as pending" : "Mark as completed");

            if (isCompleted) {
                toggleBtn.getStyle().set("color", "#10B981");
            } else {
                toggleBtn.getStyle().set("color", "#94A3B8");
            }

            toggleBtn.addClickListener(e -> {
                taskService.toggleStatus(task.getId());
                refresh();
                String msg = isCompleted ? "Task marked as Pending" : "Task marked as Completed!";
                Notification n = Notification.show(msg, 2000, Notification.Position.TOP_END);
                n.addThemeVariants(isCompleted ? NotificationVariant.LUMO_PRIMARY : NotificationVariant.LUMO_SUCCESS);
            });

            return toggleBtn;
        }).setHeader("Status").setWidth("75px").setFlexGrow(0);

        // 2. Title & Description
        taskGrid.addComponentColumn(task -> {
            VerticalLayout vl = new VerticalLayout();
            vl.setPadding(false);
            vl.setSpacing(false);

            Span title = new Span(task.getTitle());
            title.getStyle().set("font-weight", "600").set("color", "#F8FAFC");

            if (task.getStatus() == TaskStatus.COMPLETED) {
                title.getStyle().set("text-decoration", "line-through").set("color", "#64748B");
            }

            if (task.getDescription() != null && !task.getDescription().isEmpty()) {
                Span desc = new Span(task.getDescription());
                desc.getStyle().set("font-size", "0.75rem").set("color", "#94A3B8");
                vl.add(title, desc);
            } else {
                vl.add(title);
            }
            return vl;
        }).setHeader("Assignment Title").setFlexGrow(3);

        // 3. Subject
        taskGrid.addComponentColumn(task -> {
            Span badge = new Span(task.getSubject());
            badge.addClassName("sf-badge-subject");
            return badge;
        }).setHeader("Subject").setWidth("140px").setFlexGrow(0);

        // 4. Due Date with Deadline indicator
        taskGrid.addComponentColumn(task -> {
            HorizontalLayout hl = new HorizontalLayout();
            hl.setAlignItems(FlexComponent.Alignment.CENTER);
            hl.setSpacing(true);

            String formattedDate = task.getDueDate() != null ?
                    task.getDueDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "N/A";
            Span dateSpan = new Span(formattedDate);
            dateSpan.getStyle().set("font-size", "0.85rem");

            if (task.getStatus() == TaskStatus.PENDING) {
                if (task.isOverdue()) {
                    Span badge = new Span("OVERDUE");
                    badge.addClassNames("sf-badge", "sf-badge-overdue");
                    hl.add(dateSpan, badge);
                } else if (task.isDueToday()) {
                    Span badge = new Span("TODAY");
                    badge.addClassNames("sf-badge", "sf-badge-high");
                    hl.add(dateSpan, badge);
                } else if (task.isDueSoon()) {
                    Span badge = new Span("SOON");
                    badge.addClassNames("sf-badge", "sf-badge-medium");
                    hl.add(dateSpan, badge);
                } else {
                    hl.add(dateSpan);
                }
            } else {
                hl.add(dateSpan);
            }
            return hl;
        }).setHeader("Due Date").setWidth("210px").setFlexGrow(1);

        // 5. Priority Badge
        taskGrid.addComponentColumn(task -> {
            Priority p = task.getPriority() != null ? task.getPriority() : Priority.MEDIUM;
            Span badge = new Span(p.getDisplayName());
            badge.addClassName("sf-badge");
            badge.addClassName("sf-badge-" + p.name().toLowerCase());
            return badge;
        }).setHeader("Priority").setWidth("110px").setFlexGrow(0);

        // 6. Action Buttons (Edit & Delete)
        taskGrid.addComponentColumn(task -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(true);

            // Edit Button
            Button editBtn = new Button(VaadinIcon.EDIT.create(), e -> {
                TaskFormDialog dialog = new TaskFormDialog(taskService, task, savedTask -> refresh());
                dialog.open();
            });
            editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editBtn.setTooltipText("Edit task");
            editBtn.getStyle().set("color", "#818CF8");

            // Delete Button
            Button deleteBtn = new Button(VaadinIcon.TRASH.create(), e -> {
                ConfirmDeleteDialog dialog = new ConfirmDeleteDialog(task, () -> {
                    taskService.deleteTask(task.getId());
                    refresh();
                    Notification n = Notification.show("Task deleted.", 2000, Notification.Position.TOP_END);
                    n.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
                });
                dialog.open();
            });
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            deleteBtn.setTooltipText("Delete task");

            actions.add(editBtn, deleteBtn);
            return actions;
        }).setHeader("Actions").setWidth("120px").setFlexGrow(0);
    }

    private void configureEmptyState() {
        emptyStateLayout.setSizeFull();
        emptyStateLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        emptyStateLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        emptyStateLayout.setVisible(false);

        Span icon = new Span(VaadinIcon.INBOX.create());
        icon.getStyle()
                .set("font-size", "3rem")
                .set("color", "#475569")
                .set("margin-bottom", "1rem");

        emptyStateTitle.getStyle()
                .set("font-size", "1.25rem")
                .set("font-weight", "600")
                .set("color", "#F8FAFC");

        emptyStateDesc.getStyle()
                .set("color", "#94A3B8")
                .set("font-size", "0.9rem")
                .set("margin-top", "0.25rem");

        Button emptyActionBtn = new Button("Add First Task", VaadinIcon.PLUS.create(), e -> {
            TaskFormDialog dialog = new TaskFormDialog(taskService, null, t -> refresh());
            dialog.open();
        });
        emptyActionBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        emptyActionBtn.getStyle().set("background", "#6366F1").set("margin-top", "1rem");

        emptyStateLayout.add(icon, emptyStateTitle, emptyStateDesc, emptyActionBtn);
    }

    private void applyFilters() {
        String query = searchField.getValue();
        String status = statusFilter.getValue();
        String priority = priorityFilter.getValue();

        // Convert UI labels to filter codes
        String statusCode = "ALL";
        if ("Pending".equalsIgnoreCase(status)) statusCode = "PENDING";
        if ("Completed".equalsIgnoreCase(status)) statusCode = "COMPLETED";

        String priorityCode = "ALL";
        if ("High".equalsIgnoreCase(priority)) priorityCode = "HIGH";
        if ("Medium".equalsIgnoreCase(priority)) priorityCode = "MEDIUM";
        if ("Low".equalsIgnoreCase(priority)) priorityCode = "LOW";

        List<Task> results = taskService.searchAndFilter(query, statusCode, priorityCode);
        taskGrid.setItems(results);

        boolean hasResults = !results.isEmpty();
        taskGrid.setVisible(hasResults);
        emptyStateLayout.setVisible(!hasResults);

        if (!hasResults) {
            if (taskService.getTotalCount() == 0) {
                emptyStateTitle.setText("No Tasks Found");
                emptyStateDesc.setText("Your study list is empty. Create your first assignment to get started!");
            } else {
                emptyStateTitle.setText("No Matching Assignments");
                emptyStateDesc.setText("Try adjusting your search keywords or resetting the status and priority filters.");
            }
        }
    }

    private void resetFilters() {
        searchField.clear();
        statusFilter.setValue("All Statuses");
        priorityFilter.setValue("All Priorities");
        applyFilters();
    }

    @Override
    public void refresh() {
        applyFilters();
    }
}
