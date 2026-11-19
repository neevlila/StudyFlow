package com.studyflow.components;

import com.studyflow.model.Task;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Clean confirmation modal dialog shown before deleting a task.
 * Prevents accidental data loss and provides a beginner-friendly UI pattern.
 */
public class ConfirmDeleteDialog extends Dialog {

    public ConfirmDeleteDialog(Task task, Runnable onConfirm) {
        setHeaderTitle("Delete Assignment");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        HorizontalLayout warningHeader = new HorizontalLayout();
        warningHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        
        Span iconSpan = new Span(VaadinIcon.WARNING.create());
        iconSpan.getStyle().set("color", "#EF4444");
        
        Paragraph warningText = new Paragraph("Are you sure you want to permanently delete this task?");
        warningText.getStyle().set("font-weight", "600").set("margin", "0");
        warningHeader.add(iconSpan, warningText);

        Paragraph taskDetails = new Paragraph(
                "\"" + task.getTitle() + "\" (" + task.getSubject() + ")"
        );
        taskDetails.getStyle()
                .set("color", "#94A3B8")
                .set("background", "#0F172A")
                .set("padding", "0.75rem 1rem")
                .set("border-radius", "8px")
                .set("border", "1px solid #334155")
                .set("margin", "0");

        Paragraph note = new Paragraph("This action cannot be undone. Dashboard statistics will update automatically.");
        note.getStyle().set("color", "#64748B").set("font-size", "0.85rem").set("margin", "0");

        content.add(warningHeader, taskDetails, note);
        add(content);

        Button deleteBtn = new Button("Delete Task", VaadinIcon.TRASH.create(), e -> {
            onConfirm.run();
            close();
        });
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        Button cancelBtn = new Button("Cancel", e -> close());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        getFooter().add(cancelBtn, deleteBtn);
        setModal(true);
        setWidth("460px");
    }
}
