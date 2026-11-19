package com.studyflow.model;

/**
 * Represents the current progress state of a task.
 * Supports PENDING and COMPLETED.
 */
public enum TaskStatus {
    PENDING("Pending", "#6366F1"),
    COMPLETED("Completed", "#10B981");

    private final String displayName;
    private final String colorCode;

    TaskStatus(String displayName, String colorCode) {
        this.displayName = displayName;
        this.colorCode = colorCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorCode() {
        return colorCode;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
