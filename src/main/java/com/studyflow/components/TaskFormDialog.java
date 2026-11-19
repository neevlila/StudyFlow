package com.studyflow.components;

import com.studyflow.model.Priority;
import com.studyflow.model.Task;
import com.studyflow.model.TaskStatus;
import com.studyflow.service.TaskService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;

import java.time.LocalDate;
import java.util.function.Consumer;

/**
 * Modal form dialog for creating and editing study assignments.
 * Demonstrates basic validation, event handling, and clean component layout.
 */
public class TaskFormDialog extends Dialog {

    private final TaskService taskService;
    private final Task task;
    private final Consumer<Task> onSaveCallback;

    private final TextField titleField = new TextField("Task Title");
    private final TextField subjectField = new TextField("Subject / Course");
    private final DatePicker dueDateField = new DatePicker("Due Date");
    private final RadioButtonGroup<Priority> priorityGroup = new RadioButtonGroup<>("Priority");
    private final TextArea descriptionField = new TextArea("Description (Optional)");

    public TaskFormDialog(TaskService taskService, Task existingTask, Consumer<Task> onSaveCallback) {
        this.taskService = taskService;
        this.task = existingTask != null ? existingTask : new Task();
        this.onSaveCallback = onSaveCallback;

        boolean isEdit = existingTask != null && existingTask.getId() != null;
        setHeaderTitle(isEdit ? "Edit Assignment" : "New Study Assignment");

        configureFields();
        populateFields();

        FormLayout formLayout = new FormLayout();
        formLayout.add(titleField, 2);
        formLayout.add(subjectField, 1);
        formLayout.add(dueDateField, 1);
        formLayout.add(priorityGroup, 2);
        formLayout.add(descriptionField, 2);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        add(formLayout);

        // Action buttons
        Button saveBtn = new Button(isEdit ? "Save Changes" : "Create Task",
                isEdit ? VaadinIcon.CHECK.create() : VaadinIcon.PLUS.create());
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.getStyle().set("background", "#6366F1");
        saveBtn.addClickListener(e -> handleSave());

        Button cancelBtn = new Button("Cancel", e -> close());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        getFooter().add(cancelBtn, saveBtn);

        setWidth("560px");
        setModal(true);
    }

    private void configureFields() {
        titleField.setRequired(true);
        titleField.setPlaceholder("e.g. Complete DAA Assignment");
        titleField.setClearButtonVisible(true);
        titleField.setMaxLength(200);

        subjectField.setRequired(true);
        subjectField.setPlaceholder("e.g. DAA, Java, DBMS, Networks");
        subjectField.setClearButtonVisible(true);
        subjectField.setMaxLength(100);

        dueDateField.setRequired(true);
        dueDateField.setValue(LocalDate.now());

        priorityGroup.setItems(Priority.values());
        priorityGroup.setItemLabelGenerator(Priority::getDisplayName);
        priorityGroup.setRenderer(new ComponentRenderer<>(p -> {
            Span badge = new Span(p.getDisplayName());
            String color = switch (p) {
                case HIGH -> "#EF4444";
                case MEDIUM -> "#F59E0B";
                case LOW -> "#10B981";
            };
            badge.getStyle()
                    .set("color", color)
                    .set("font-weight", "600")
                    .set("font-size", "0.85rem");
            return badge;
        }));
        priorityGroup.setValue(Priority.MEDIUM);

        descriptionField.setPlaceholder("Add assignment notes, submission links, or steps...");
        descriptionField.setMaxLength(1000);
        descriptionField.setMinHeight("100px");
    }

    private void populateFields() {
        if (task.getTitle() != null) {
            titleField.setValue(task.getTitle());
        }
        if (task.getSubject() != null) {
            subjectField.setValue(task.getSubject());
        }
        if (task.getDueDate() != null) {
            dueDateField.setValue(task.getDueDate());
        }
        if (task.getPriority() != null) {
            priorityGroup.setValue(task.getPriority());
        }
        if (task.getDescription() != null) {
            descriptionField.setValue(task.getDescription());
        }
    }

    /**
     * Validates user input and saves the record into the H2 database via TaskService.
     */
    private void handleSave() {
        String title = titleField.getValue().trim();
        String subject = subjectField.getValue().trim();
        LocalDate dueDate = dueDateField.getValue();
        Priority priority = priorityGroup.getValue();
        String description = descriptionField.getValue();

        // 1. Validation checks
        boolean hasError = false;

        if (title.isEmpty()) {
            titleField.setInvalid(true);
            titleField.setErrorMessage("Task title cannot be empty.");
            hasError = true;
        } else {
            titleField.setInvalid(false);
        }

        if (subject.isEmpty()) {
            subjectField.setInvalid(true);
            subjectField.setErrorMessage("Subject cannot be empty.");
            hasError = true;
        } else {
            subjectField.setInvalid(false);
        }

        if (dueDate == null) {
            dueDateField.setInvalid(true);
            dueDateField.setErrorMessage("Please select a due date.");
            hasError = true;
        } else {
            dueDateField.setInvalid(false);
        }

        if (hasError) {
            return;
        }

        // 2. Populate Task object
        task.setTitle(title);
        task.setSubject(subject);
        task.setDueDate(dueDate);
        task.setPriority(priority != null ? priority : Priority.MEDIUM);
        task.setDescription(description != null ? description.trim() : "");
        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.PENDING);
        }

        // 3. Persist via Service
        try {
            Task saved = taskService.saveTask(task);

            Notification notification = Notification.show(
                    task.getId() != null ? "Task updated successfully!" : "Task created successfully!",
                    3000,
                    Notification.Position.TOP_END
            );
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            if (onSaveCallback != null) {
                onSaveCallback.accept(saved);
            }
            close();
        } catch (Exception ex) {
            Notification errorNote = Notification.show(
                    "Error saving task: " + ex.getMessage(),
                    4000,
                    Notification.Position.TOP_CENTER
            );
            errorNote.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
