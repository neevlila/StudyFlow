package com.studyflow.model;

/**
 * Represents the priority level of an academic task.
 * Simple enum with display helpers for beginner clarity.
 */
public enum Priority {
    LOW("Low", "#10B981", "success"),
    MEDIUM("Medium", "#F59E0B", "warning"),
    HIGH("High", "#EF4444", "error");

    private final String displayName;
    private final String colorCode;
    private final String badgeTheme;

    Priority(String displayName, String colorCode, String badgeTheme) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.badgeTheme = badgeTheme;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorCode() {
        return colorCode;
    }

    public String getBadgeTheme() {
        return badgeTheme;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
