package com.studyflow.view;

import com.studyflow.components.ConfirmDeleteDialog;
import com.studyflow.model.Task;
import com.studyflow.model.TaskStatus;
import com.studyflow.service.TaskService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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
 * Completed Assignments Archive View.
 * Displays all successfully completed study tasks with restore and delete actions.
 */
@Route(value = "completed", layout = MainLayout.class)
@PageTitle("Completed Tasks - StudyFlow")
public class CompletedTasksView extends VerticalLayout implements Refreshable {

    private final TaskService taskService;

    private final TextField searchField = new TextField();
    private final Grid<Task> grid = new Grid<>(Task.class, false);
    private final VerticalLayout emptyLayout = new VerticalLayout();
    private final Span totalCompletedBadge = new Span("0 Completed");

    public CompletedTasksView(TaskService taskService) {
        this.taskService = taskService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle().set("background-color", "#0F172A");

        // 1. Header Toolbar
        add(createToolbar());

        // 2. Table Grid with mobile-responsive wrapper
        configureGrid();
        Div gridWrapper = new Div(grid);
        gridWrapper.addClassName("sf-grid-wrapper");
        gridWrapper.setSizeFull();

        Span swipeHint = new Span("← Swipe table to view all columns →");
        swipeHint.addClassName("sf-swipe-hint");

        add(swipeHint, gridWrapper);

        // 3. Empty State
        configureEmptyState();
        add(emptyLayout);

        // Initial Data Load
        refresh();
    }

    private HorizontalLayout createToolbar() {
        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setWidthFull();
        toolbar.addClassName("sf-toolbar");
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        toolbar.getStyle()
                .set("background", "#1E293B")
                .set("padding", "0.85rem 1.25rem")
                .set("border-radius", "12px")
                .set("border", "1px solid #334155");

        searchField.addClassName("sf-search-field");
        searchField.setPlaceholder("Search completed assignments...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> loadData());

        totalCompletedBadge.addClassName("sf-completed-badge");
        totalCompletedBadge.getStyle()
                .set("background", "rgba(16, 185, 129, 0.15)")
                .set("color", "#34D399")
                .set("font-weight", "600")
                .set("font-size", "0.85rem")
                .set("padding", "0.35rem 0.85rem")
                .set("border-radius", "9999px")
                .set("border", "1px solid rgba(16, 185, 129, 0.3)")
                .set("margin-left", "auto");

        toolbar.add(searchField, totalCompletedBadge);
        return toolbar;
    }

    private void configureGrid() {
        grid.setSizeFull();

        // 1. Restore action (Checkmark)
        grid.addComponentColumn(task -> {
            Button uncheckBtn = new Button(VaadinIcon.CHECK_CIRCLE.create(), e -> {
                taskService.toggleStatus(task.getId());
                refresh();
                Notification n = Notification.show("Task restored to pending list.", 2000, Notification.Position.TOP_END);
                n.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
            });
            uncheckBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
            uncheckBtn.setTooltipText("Click to move back to Pending");
            uncheckBtn.getStyle().set("color", "#10B981");
            return uncheckBtn;
        }).setHeader("Restore").setWidth("85px").setFlexGrow(0);

        // 2. Title & Description
        grid.addComponentColumn(task -> {
            VerticalLayout vl = new VerticalLayout();
            vl.setPadding(false);
            vl.setSpacing(false);

            Span title = new Span(task.getTitle());
            title.getStyle()
                    .set("font-weight", "500")
                    .set("color", "#94A3B8")
                    .set("text-decoration", "line-through");

            if (task.getDescription() != null && !task.getDescription().isEmpty()) {
                Span desc = new Span(task.getDescription());
                desc.getStyle().set("font-size", "0.75rem").set("color", "#64748B");
                vl.add(title, desc);
            } else {
                vl.add(title);
            }
            return vl;
        }).setHeader("Assignment Title").setFlexGrow(3);

        // 3. Subject
        grid.addComponentColumn(task -> {
            Span badge = new Span(task.getSubject());
            badge.addClassName("sf-badge-subject");
            return badge;
        }).setHeader("Subject").setWidth("140px").setFlexGrow(0);

        // 4. Completed Due Date
        grid.addComponentColumn(task -> {
            String date = task.getDueDate() != null ?
                    task.getDueDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "N/A";
            Span s = new Span(date);
            s.getStyle().set("color", "#94A3B8").set("font-size", "0.85rem");
            return s;
        }).setHeader("Original Due Date").setWidth("170px").setFlexGrow(0);

        // 5. Actions: Permanent Delete
        grid.addComponentColumn(task -> {
            Button deleteBtn = new Button(VaadinIcon.TRASH.create(), e -> {
                ConfirmDeleteDialog dialog = new ConfirmDeleteDialog(task, () -> {
                    taskService.deleteTask(task.getId());
                    refresh();
                    Notification n = Notification.show("Task deleted from archive.", 2000, Notification.Position.TOP_END);
                    n.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
                });
                dialog.open();
            });
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            deleteBtn.setTooltipText("Delete permanently");
            return deleteBtn;
        }).setHeader("Delete").setWidth("90px").setFlexGrow(0);
    }

    private void configureEmptyState() {
        emptyLayout.setSizeFull();
        emptyLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        emptyLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        emptyLayout.setVisible(false);

        Span icon = new Span(VaadinIcon.CHECK_CIRCLE_O.create());
        icon.getStyle()
                .set("font-size", "3.5rem")
                .set("color", "#334155")
                .set("margin-bottom", "1rem");

        H3 title = new H3("No Completed Assignments Yet");
        title.getStyle().set("color", "#F8FAFC").set("margin", "0");

        Paragraph desc = new Paragraph("Tasks you complete will appear here. Go to 'My Tasks' to check off your work!");
        desc.getStyle().set("color", "#94A3B8").set("margin-top", "0.25rem");

        emptyLayout.add(icon, title, desc);
    }

    private void loadData() {
        String query = searchField.getValue();
        List<Task> completed = taskService.searchAndFilter(query, "COMPLETED", "ALL");
        grid.setItems(completed);

        totalCompletedBadge.setText(taskService.getCompletedCount() + " Completed");

        boolean hasItems = !completed.isEmpty();
        grid.setVisible(hasItems);
        emptyLayout.setVisible(!hasItems);
    }

    @Override
    public void refresh() {
        loadData();
    }
}
