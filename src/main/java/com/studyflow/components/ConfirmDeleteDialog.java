package com.studyflow.components;

import com.studyflow.model.Task;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Clean confirmation modal dialog shown before deleting a task.
 * High-contrast safety alert and responsive layout preventing accidental data loss.
 */
public class ConfirmDeleteDialog extends Dialog {

    public ConfirmDeleteDialog(Task task, Runnable onConfirm) {
        setHeaderTitle("Delete Assignment");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        HorizontalLayout warningHeader = new HorizontalLayout();
        warningHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        warningHeader.setSpacing(true);

        Span iconSpan = new Span(VaadinIcon.WARNING.create());
        iconSpan.getStyle()
                .set("color", "#EF4444")
                .set("font-size", "1.25rem");

        Paragraph warningText = new Paragraph("Are you sure you want to permanently delete this task?");
        warningText.getStyle()
                .set("font-weight", "600")
                .set("color", "#F8FAFC")
                .set("margin", "0");
        warningHeader.add(iconSpan, warningText);

        Paragraph taskDetails = new Paragraph(
                "\"" + task.getTitle() + "\" • " + task.getSubject()
        );
        taskDetails.getStyle()
                .set("color", "#CBD5E1")
                .set("background", "#0F172A")
                .set("padding", "0.75rem 1rem")
                .set("border-radius", "8px")
                .set("border", "1px solid #334155")
                .set("font-weight", "500")
                .set("margin", "0.25rem 0");

        Paragraph note = new Paragraph("This action cannot be undone. All dashboard metrics will update immediately.");
        note.getStyle()
                .set("color", "#64748B")
                .set("font-size", "0.82rem")
                .set("margin", "0");

        content.add(warningHeader, taskDetails, note);
        add(content);

        Button deleteBtn = new Button("Delete Task", VaadinIcon.TRASH.create(), e -> {
            onConfirm.run();
            close();
        });
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        deleteBtn.getStyle().set("background", "#EF4444");

        Button cancelBtn = new Button("Cancel", e -> close());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        getFooter().add(cancelBtn, deleteBtn);
        setModal(true);
        setWidth("min(460px, 94vw)");
    }
}
