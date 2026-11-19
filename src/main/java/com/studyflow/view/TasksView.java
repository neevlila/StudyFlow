package com.studyflow.view;

import com.studyflow.components.ConfirmDeleteDialog;
import com.studyflow.components.TaskFormDialog;
import com.studyflow.model.Priority;
import com.studyflow.model.Task;
import com.studyflow.model.TaskStatus;
import com.studyflow.service.TaskService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Tasks Management View.
 * Provides instant segmented quick filter chips, live search, course & priority filtering,
 * full CRUD actions, status toggling, and fully responsive layout for phones, tablets, and desktop.
 */
@Route(value = "tasks", layout = MainLayout.class)
@PageTitle("My Tasks - StudyFlow")
public class TasksView extends VerticalLayout implements Refreshable {

    private final TaskService taskService;

    // Quick filter chips
    private String activeQuickFilter = "ALL";
    private final Div chipsContainer = new Div();
    private final Div chipAll = new Div();
    private final Div chipPending = new Div();
    private final Div chipToday = new Div();
    private final Div chipOverdue = new Div();
    private final Div chipCompleted = new Div();

    // Filters
    private final TextField searchField = new TextField();
    private final ComboBox<String> priorityFilter = new ComboBox<>();
    private final ComboBox<String> subjectFilter = new ComboBox<>();
    private final Button resetFilterBtn = new Button(VaadinIcon.CLOSE_SMALL.create());

    // Task Grid and Empty State
    private final Grid<Task> taskGrid = new Grid<>(Task.class, false);
    private final VerticalLayout emptyStateLayout = new VerticalLayout();
    private final Span emptyStateTitle = new Span();
    private final Paragraph emptyStateDesc = new Paragraph();
    private final Button emptyStateActionBtn = new Button();

    public TasksView(TaskService taskService) {
        this.taskService = taskService;

        setWidthFull();
        setPadding(false);
        setSpacing(false);
        getStyle()
                .set("background-color", "#0B1120")
                .set("padding", "1rem 1.5rem")
                .set("gap", "1rem")
                .set("box-sizing", "border-box");

        // 1. Quick Filter Segmented Chips (One-tap filtering)
        add(createQuickFilterChips());

        // 2. Toolbar (Search, Filter, New Task button)
        add(createToolbar());

        // 3. Table Grid with mobile-responsive wrapper
        configureGrid();
        Div gridWrapper = new Div(taskGrid);
        gridWrapper.addClassName("sf-grid-wrapper");
        gridWrapper.setSizeFull();

        add(gridWrapper);

        // 4. Empty State Layout
        configureEmptyState();
        add(emptyStateLayout);

        // Initial Data Load
        refresh();
    }

    private Component createQuickFilterChips() {
        chipsContainer.addClassName("sf-filter-chips-container");

        setupChip(chipAll, "ALL", "All Tasks", 0);
        setupChip(chipPending, "PENDING", "Pending", 0);
        setupChip(chipToday, "TODAY", "Due Today", 0);
        setupChip(chipOverdue, "OVERDUE", "Overdue", 0);
        setupChip(chipCompleted, "COMPLETED", "Completed", 0);

        chipsContainer.add(chipAll, chipPending, chipToday, chipOverdue, chipCompleted);
        return chipsContainer;
    }

    private void setupChip(Div chip, String filterCode, String label, int count) {
        chip.addClassName("sf-filter-chip");
        if (filterCode.equals(activeQuickFilter)) {
            chip.addClassName("active");
        }

        chip.addClickListener(e -> {
            activeQuickFilter = filterCode;
            updateChipsActiveState();
            applyFilters();
        });

        renderChipContent(chip, label, count);
    }

    private void renderChipContent(Div chip, String label, int count) {
        chip.removeAll();
        Span labelSpan = new Span(label);
        Span countSpan = new Span(String.valueOf(count));
        countSpan.addClassName("sf-chip-count");
        chip.add(labelSpan, countSpan);
    }

    private void updateChipsActiveState() {
        chipAll.removeClassName("active");
        chipPending.removeClassName("active");
        chipToday.removeClassName("active");
        chipOverdue.removeClassName("active");
        chipCompleted.removeClassName("active");

        switch (activeQuickFilter) {
            case "PENDING" -> chipPending.addClassName("active");
            case "TODAY" -> chipToday.addClassName("active");
            case "OVERDUE" -> chipOverdue.addClassName("active");
            case "COMPLETED" -> chipCompleted.addClassName("active");
            default -> chipAll.addClassName("active");
        }
    }

    private void updateChipsCounters() {
        int total = taskService.getTotalCount();
        int pending = taskService.getPendingCount();
        int today = taskService.getDueTodayCount();
        int overdue = taskService.getOverdueCount();
        int completed = taskService.getCompletedCount();

        renderChipContent(chipAll, "All Tasks", total);
        renderChipContent(chipPending, "Pending", pending);
        renderChipContent(chipToday, "Due Today", today);
        renderChipContent(chipOverdue, "Overdue", overdue);
        renderChipContent(chipCompleted, "Completed", completed);
    }

    private HorizontalLayout createToolbar() {
        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setWidthFull();
        toolbar.addClassName("sf-toolbar");
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        toolbar.setSpacing(true);

        // Search Field
        searchField.addClassName("sf-search-field");
        searchField.setPlaceholder("Search assignments or topics...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.setValueChangeTimeout(200);
        searchField.addValueChangeListener(e -> applyFilters());

        // Subject Filter
        subjectFilter.addClassName("sf-filter-select");
        subjectFilter.setPlaceholder("Subject");
        subjectFilter.setClearButtonVisible(true);
        subjectFilter.addValueChangeListener(e -> applyFilters());

        // Priority Filter
        priorityFilter.addClassName("sf-filter-select");
        priorityFilter.setPlaceholder("Priority");
        priorityFilter.setItems("All Priorities", "High", "Medium", "Low");
        priorityFilter.setValue("All Priorities");
        priorityFilter.addValueChangeListener(e -> applyFilters());

        // Reset Filter Button
        resetFilterBtn.setTooltipText("Reset Filters");
        resetFilterBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        resetFilterBtn.addClickListener(e -> resetFilters());

        HorizontalLayout filterGroup = new HorizontalLayout(subjectFilter, priorityFilter, resetFilterBtn);
        filterGroup.addClassName("sf-filter-group");
        filterGroup.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        // Add Task Action
        Button addTaskBtn = new Button("New Task", VaadinIcon.PLUS.create(), e -> {
            TaskFormDialog dialog = new TaskFormDialog(taskService, null, t -> {
                refresh();
                updateParentLayout();
            });
            dialog.open();
        });
        addTaskBtn.addClassNames("sf-toolbar-add-btn", "sf-primary-btn");
        addTaskBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addTaskBtn.getStyle().set("margin-left", "auto");

        toolbar.add(searchField, filterGroup, addTaskBtn);
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
                updateParentLayout();
                String msg = isCompleted ? "Task moved to Pending." : "Task marked as Completed!";
                Notification n = Notification.show(msg, 2000, Notification.Position.TOP_END);
                n.addThemeVariants(isCompleted ? NotificationVariant.LUMO_PRIMARY : NotificationVariant.LUMO_SUCCESS);
            });

            return toggleBtn;
        }).setHeader("Status").setWidth("65px").setFlexGrow(0);

        // 2. Title & Description + Mobile Metadata Embedded Row
        taskGrid.addComponentColumn(task -> {
            VerticalLayout vl = new VerticalLayout();
            vl.setPadding(false);
            vl.setSpacing(false);

            Span title = new Span(task.getTitle());
            title.getStyle().set("font-weight", "600").set("color", "#F8FAFC").set("font-size", "0.92rem");

            if (task.getStatus() == TaskStatus.COMPLETED) {
                title.getStyle().set("text-decoration", "line-through").set("color", "#64748B");
            }

            vl.add(title);

            if (task.getDescription() != null && !task.getDescription().isEmpty()) {
                Span desc = new Span(task.getDescription());
                desc.getStyle().set("font-size", "0.76rem").set("color", "#94A3B8").set("margin-top", "0.15rem");
                vl.add(desc);
            }

            // Embedded metadata row for mobile screen layout
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
        }).setHeader("Assignment").setFlexGrow(3);

        // 3. Subject (Desktop column)
        taskGrid.addComponentColumn(task -> {
            Span badge = new Span(task.getSubject());
            badge.addClassName("sf-badge-subject");
            return badge;
        }).setHeader("Subject").setWidth("140px").setFlexGrow(0).addClassNames("sf-col-desktop");

        // 4. Due Date with Deadline indicator (Desktop column)
        taskGrid.addComponentColumn(task -> {
            HorizontalLayout hl = new HorizontalLayout();
            hl.setAlignItems(FlexComponent.Alignment.CENTER);
            hl.setSpacing(true);

            String formattedDate = task.getDueDate() != null ?
                    task.getDueDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "N/A";
            Span dateSpan = new Span(formattedDate);
            dateSpan.getStyle().set("font-size", "0.85rem");

            if (task.getStatus() == TaskStatus.PENDING) {
                Span urgencyBadge = createUrgencyBadge(task);
                if (urgencyBadge != null) {
                    hl.add(dateSpan, urgencyBadge);
                } else {
                    hl.add(dateSpan);
                }
            } else {
                hl.add(dateSpan);
            }
            return hl;
        }).setHeader("Due Date").setWidth("210px").setFlexGrow(1).addClassNames("sf-col-desktop");

        // 5. Priority Badge (Desktop column)
        taskGrid.addComponentColumn(task -> {
            Priority p = task.getPriority() != null ? task.getPriority() : Priority.MEDIUM;
            Span badge = new Span(p.getDisplayName());
            badge.addClassName("sf-badge");
            badge.addClassName("sf-badge-" + p.name().toLowerCase());
            return badge;
        }).setHeader("Priority").setWidth("110px").setFlexGrow(0).addClassNames("sf-col-desktop");

        // 6. Action Buttons (Edit & Delete)
        taskGrid.addComponentColumn(task -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(true);

            // Edit Button
            Button editBtn = new Button(VaadinIcon.EDIT.create(), e -> {
                TaskFormDialog dialog = new TaskFormDialog(taskService, task, savedTask -> {
                    refresh();
                    updateParentLayout();
                });
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
                    updateParentLayout();
                    Notification n = Notification.show("Task deleted.", 2000, Notification.Position.TOP_END);
                    n.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
                });
                dialog.open();
            });
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            deleteBtn.setTooltipText("Delete task");

            actions.add(editBtn, deleteBtn);
            return actions;
        }).setHeader("Actions").setWidth("110px").setFlexGrow(0);
    }

    private Span createDateBadge(Task task) {
        String formattedDate = task.getDueDate() != null ?
                task.getDueDate().format(DateTimeFormatter.ofPattern("MMM d")) : "No date";

        Span badge = new Span(formattedDate);
        if (task.getStatus() == TaskStatus.COMPLETED) {
            badge.addClassNames("sf-badge", "sf-badge-completed");
            badge.setText("DONE");
        } else if (task.isOverdue()) {
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

    private void configureEmptyState() {
        emptyStateLayout.setSizeFull();
        emptyStateLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        emptyStateLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        emptyStateLayout.setVisible(false);

        Span icon = new Span(VaadinIcon.INBOX.create());
        icon.getStyle()
                .set("font-size", "3.2rem")
                .set("color", "#475569")
                .set("margin-bottom", "0.75rem");

        emptyStateTitle.getStyle()
                .set("font-size", "1.25rem")
                .set("font-weight", "700")
                .set("color", "#F8FAFC");

        emptyStateDesc.getStyle()
                .set("color", "#94A3B8")
                .set("font-size", "0.9rem")
                .set("margin-top", "0.25rem")
                .set("text-align", "center")
                .set("max-width", "400px");

        emptyStateActionBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        emptyStateActionBtn.addClassName("sf-primary-btn");
        emptyStateActionBtn.getStyle().set("margin-top", "1rem");

        emptyStateLayout.add(icon, emptyStateTitle, emptyStateDesc, emptyStateActionBtn);
    }

    private void applyFilters() {
        String query = searchField.getValue();
        String priority = priorityFilter.getValue();
        String selectedSubject = subjectFilter.getValue();

        String priorityCode = "ALL";
        if ("High".equalsIgnoreCase(priority)) priorityCode = "HIGH";
        if ("Medium".equalsIgnoreCase(priority)) priorityCode = "MEDIUM";
        if ("Low".equalsIgnoreCase(priority)) priorityCode = "LOW";

        // Query service with active quick filter
        List<Task> results = taskService.searchAndFilter(query, activeQuickFilter, priorityCode);

        // Secondary Subject filter if specified
        if (selectedSubject != null && !selectedSubject.trim().isEmpty() && !selectedSubject.equalsIgnoreCase("All Subjects")) {
            results = results.stream()
                    .filter(t -> t.getSubject() != null && t.getSubject().equalsIgnoreCase(selectedSubject.trim()))
                    .toList();
        }

        taskGrid.setItems(results);

        boolean hasResults = !results.isEmpty();
        taskGrid.setVisible(hasResults);
        emptyStateLayout.setVisible(!hasResults);

        if (!hasResults) {
            if (taskService.getTotalCount() == 0) {
                emptyStateTitle.setText("No Tasks Found");
                emptyStateDesc.setText("Your study list is empty. Create your first assignment to get started!");
                emptyStateActionBtn.setText("Add First Task");
                emptyStateActionBtn.setIcon(VaadinIcon.PLUS.create());
                emptyStateActionBtn.addClickListener(e -> {
                    TaskFormDialog dialog = new TaskFormDialog(taskService, null, t -> refresh());
                    dialog.open();
                });
            } else {
                emptyStateTitle.setText("No Matching Assignments");
                emptyStateDesc.setText("No assignments match your active filters. Try adjusting search terms or resetting filters.");
                emptyStateActionBtn.setText("Reset All Filters");
                emptyStateActionBtn.setIcon(VaadinIcon.REFRESH.create());
                emptyStateActionBtn.addClickListener(e -> resetFilters());
            }
        }
    }

    private void resetFilters() {
        searchField.clear();
        priorityFilter.setValue("All Priorities");
        subjectFilter.clear();
        activeQuickFilter = "ALL";
        updateChipsActiveState();
        applyFilters();
    }

    private void updateParentLayout() {
        if (getParent().isPresent() && getParent().get() instanceof MainLayout ml) {
            ml.updateNavBadges();
        }
    }

    @Override
    public void refresh() {
        updateChipsCounters();

        // Update subjects in dropdown
        List<String> subjects = new ArrayList<>();
        subjects.add("All Subjects");
        subjects.addAll(taskService.getAllSubjects());
        subjectFilter.setItems(subjects);

        applyFilters();
    }
}
