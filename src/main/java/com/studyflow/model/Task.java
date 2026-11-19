package com.studyflow.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Task Entity representing an academic assignment or study item.
 * Clean, simple POJO with basic getters, setters, and constructors.
 */
public class Task {

    private Long id;
    private String title;
    private String subject;
    private String description;
    private LocalDate dueDate;
    private Priority priority;
    private TaskStatus status;
    private LocalDateTime createdAt;

    // Default constructor required for frameworks and empty instantiation
    public Task() {
        this.status = TaskStatus.PENDING;
        this.priority = Priority.MEDIUM;
        this.dueDate = LocalDate.now();
        this.createdAt = LocalDateTime.now();
    }

    // Parameterized constructor for easy creation
    public Task(String title, String subject, String description, LocalDate dueDate, Priority priority) {
        this.title = title;
        this.subject = subject;
        this.description = description;
        this.dueDate = dueDate;
        this.priority = priority;
        this.status = TaskStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    // Full constructor including ID and status (used when loading from database)
    public Task(Long id, String title, String subject, String description, LocalDate dueDate,
                Priority priority, TaskStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.subject = subject;
        this.description = description;
        this.dueDate = dueDate;
        this.priority = priority;
        this.status = status;
        this.createdAt = createdAt;
    }

    // --- Helper Methods for UI & Viva Explanation ---

    /**
     * Checks if the task is pending and past its due date.
     */
    public boolean isOverdue() {
        if (status == TaskStatus.COMPLETED || dueDate == null) {
            return false;
        }
        return dueDate.isBefore(LocalDate.now());
    }

    /**
     * Checks if the task is pending and due today.
     */
    public boolean isDueToday() {
        if (status == TaskStatus.COMPLETED || dueDate == null) {
            return false;
        }
        return dueDate.isEqual(LocalDate.now());
    }

    /**
     * Checks if the task is pending and due within the next 3 days.
     */
    public boolean isDueSoon() {
        if (status == TaskStatus.COMPLETED || dueDate == null) {
            return false;
        }
        LocalDate today = LocalDate.now();
        return !dueDate.isBefore(today) && dueDate.isBefore(today.plusDays(3));
    }

    // --- Standard Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", subject='" + subject + '\'' +
                ", dueDate=" + dueDate +
                ", priority=" + priority +
                ", status=" + status +
                '}';
    }
}
