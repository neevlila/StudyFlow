package com.studyflow.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.studyflow.model.Priority;
import com.studyflow.model.Task;
import com.studyflow.model.TaskStatus;
import com.studyflow.repository.TaskRepository;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * TaskService contains the core business logic of StudyFlow.
 * 
 * Supports Dual-Storage Architecture:
 * 1. Browser LocalStorage: Primary client database. Ensures every student has private,
 *    persistent data on cloud deployments (e.g. Render) without data loss on container sleep.
 * 2. H2 Database: Backing in-memory relational SQL store for viva demonstration and testing.
 */
@Service
public class TaskService {

    private static final String SESSION_TASKS_ATTR = "STUDYFLOW_TASKS";
    private static final String SESSION_LOADED_ATTR = "STUDYFLOW_LOADED";

    private final TaskRepository taskRepository;
    private final ObjectMapper objectMapper;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Seeds initial sample academic tasks into H2 if the repository is empty.
     */
    @PostConstruct
    public void initSampleData() {
        if (taskRepository.count() == 0) {
            List<Task> samples = createDefaultSampleTasks();
            for (Task t : samples) {
                taskRepository.save(t);
            }
        }
    }

    /**
     * Creates default academic coursework assignments for new users.
     */
    public List<Task> createDefaultSampleTasks() {
        LocalDate today = LocalDate.now();
        List<Task> list = new ArrayList<>();

        // 1. High Priority - Web Tech Project due today
        Task t1 = new Task(1L, "Complete Web Technology Project", "Web Technology",
                "Finalize dashboard UI components, responsive layout, and test all CRUD flows.",
                today, Priority.HIGH, TaskStatus.PENDING, today.atStartOfDay());
        list.add(t1);

        // 2. High Priority - DAA Assignment due in 2 days
        Task t2 = new Task(2L, "Complete DAA Assignment", "DAA",
                "Implement Dijkstra's Algorithm and write the time complexity analysis report.",
                today.plusDays(2), Priority.HIGH, TaskStatus.PENDING, today.atStartOfDay());
        list.add(t2);

        // 3. Medium Priority - Java Practical prep due tomorrow
        Task t3 = new Task(3L, "Prepare Java Practical", "Java",
                "Review OOP concepts, collections, generics, and sample viva questions.",
                today.plusDays(1), Priority.MEDIUM, TaskStatus.PENDING, today.atStartOfDay());
        list.add(t3);

        // 4. Low Priority - DBMS Notes review due in 5 days
        Task t4 = new Task(4L, "Review Database Notes", "DBMS",
                "Study B-Trees, transaction isolation levels, and ACID properties.",
                today.plusDays(5), Priority.LOW, TaskStatus.PENDING, today.atStartOfDay());
        list.add(t4);

        // 5. Overdue sample task to demonstrate overdue highlighting
        Task t5 = new Task(5L, "Submit Computer Networks Lab File", "Networks",
                "Compile Wireshark packet capture screenshots and subnetting calculations.",
                today.minusDays(2), Priority.MEDIUM, TaskStatus.PENDING, today.minusDays(5).atStartOfDay());
        list.add(t5);

        // 6. Completed sample task to demonstrate completion metrics
        Task t6 = new Task(6L, "Submit Software Engineering SRS Document", "Software Engg",
                "Requirements specification document uploaded to university portal.",
                today.minusDays(4), Priority.LOW, TaskStatus.COMPLETED, today.minusDays(7).atStartOfDay());
        list.add(t6);

        return list;
    }

    /**
     * Gets tasks associated with the current user's browser session.
     * Returns null if running outside a Vaadin session (e.g. Unit tests).
     */
    private List<Task> getSessionTasks() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            @SuppressWarnings("unchecked")
            List<Task> tasks = (List<Task>) session.getAttribute(SESSION_TASKS_ATTR);
            if (tasks == null) {
                tasks = new ArrayList<>(createDefaultSampleTasks());
                session.setAttribute(SESSION_TASKS_ATTR, tasks);
            }
            return tasks;
        }
        return null;
    }

    /**
     * Checks if the session has successfully loaded tasks from the browser's LocalStorage.
     */
    public boolean isSessionLoaded() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            Boolean loaded = (Boolean) session.getAttribute(SESSION_LOADED_ATTR);
            return loaded != null && loaded;
        }
        return false;
    }

    /**
     * Sets the session's LocalStorage synchronization flag.
     */
    public void setSessionLoaded(boolean loaded) {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            session.setAttribute(SESSION_LOADED_ATTR, loaded);
        }
    }

    /**
     * Loads tasks parsed from the client's browser LocalStorage JSON string.
     */
    public void loadSessionTasksFromJson(String json) {
        if (json == null || json.trim().isEmpty() || json.trim().equals("[]")) {
            return;
        }
        try {
            List<Task> loaded = objectMapper.readValue(json, new TypeReference<List<Task>>() {});
            if (loaded != null && !loaded.isEmpty()) {
                VaadinSession session = VaadinSession.getCurrent();
                if (session != null) {
                    session.setAttribute(SESSION_TASKS_ATTR, new ArrayList<>(loaded));
                    session.setAttribute(SESSION_LOADED_ATTR, true);
                }
                // Mirror into H2 for SQL demonstration
                for (Task t : loaded) {
                    try {
                        taskRepository.save(t);
                    } catch (Exception ignored) {}
                }
            }
        } catch (Exception e) {
            System.err.println("Notice: Could not parse LocalStorage tasks: " + e.getMessage());
        }
    }

    /**
     * Exports the current task list as a JSON string for browser LocalStorage.
     */
    public String exportSessionTasksToJson() {
        List<Task> tasks = getAllTasks();
        try {
            return objectMapper.writeValueAsString(tasks);
        } catch (Exception e) {
            return "[]";
        }
    }

    /**
     * Synchronizes current task state to the user's browser LocalStorage.
     */
    public void syncToLocalStorage() {
        UI ui = UI.getCurrent();
        if (ui != null) {
            String json = exportSessionTasksToJson();
            ui.getPage().executeJs("window.localStorage.setItem('studyflow_tasks', $0);", json);
        }
    }

    /**
     * Returns all tasks sorted by deadline.
     */
    public List<Task> getAllTasks() {
        List<Task> sessionTasks = getSessionTasks();
        if (sessionTasks != null) {
            List<Task> copy = new ArrayList<>(sessionTasks);
            copy.sort(Comparator.comparing(Task::getDueDate));
            return copy;
        }
        return taskRepository.findAll();
    }

    /**
     * Finds a single task by ID.
     */
    public Optional<Task> getTaskById(Long id) {
        List<Task> sessionTasks = getSessionTasks();
        if (sessionTasks != null) {
            for (Task task : sessionTasks) {
                if (task.getId() != null && task.getId().equals(id)) {
                    return Optional.of(task);
                }
            }
            return Optional.empty();
        }
        return taskRepository.findById(id);
    }

    /**
     * Validates and saves a task (create or update).
     *
     * @throws IllegalArgumentException if required fields are missing
     */
    public Task saveTask(Task task) {
        if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Task title is required.");
        }
        if (task.getSubject() == null || task.getSubject().trim().isEmpty()) {
            throw new IllegalArgumentException("Subject is required.");
        }
        if (task.getDueDate() == null) {
            throw new IllegalArgumentException("Due date is required.");
        }
        if (task.getPriority() == null) {
            task.setPriority(Priority.MEDIUM);
        }
        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.PENDING);
        }

        task.setTitle(task.getTitle().trim());
        task.setSubject(task.getSubject().trim());
        if (task.getDescription() != null) {
            task.setDescription(task.getDescription().trim());
        }

        List<Task> sessionTasks = getSessionTasks();
        if (sessionTasks != null) {
            if (task.getId() == null) {
                // Generate unique client-side ID
                long newId = System.currentTimeMillis();
                task.setId(newId);
                sessionTasks.add(0, task);
            } else {
                boolean replaced = false;
                for (int i = 0; i < sessionTasks.size(); i++) {
                    if (sessionTasks.get(i).getId() != null && sessionTasks.get(i).getId().equals(task.getId())) {
                        sessionTasks.set(i, task);
                        replaced = true;
                        break;
                    }
                }
                if (!replaced) {
                    sessionTasks.add(task);
                }
            }
            try {
                taskRepository.save(task);
            } catch (Exception ignored) {}
            syncToLocalStorage();
            return task;
        }

        return taskRepository.save(task);
    }

    /**
     * Deletes a task by ID.
     */
    public boolean deleteTask(Long id) {
        List<Task> sessionTasks = getSessionTasks();
        if (sessionTasks != null) {
            boolean removed = sessionTasks.removeIf(t -> t.getId() != null && t.getId().equals(id));
            try {
                taskRepository.deleteById(id);
            } catch (Exception ignored) {}
            syncToLocalStorage();
            return removed;
        }
        return taskRepository.deleteById(id);
    }

    /**
     * Toggles a task status between PENDING and COMPLETED.
     */
    public Task toggleStatus(Long id) {
        Optional<Task> optionalTask = getTaskById(id);
        if (optionalTask.isPresent()) {
            Task task = optionalTask.get();
            if (task.getStatus() == TaskStatus.COMPLETED) {
                task.setStatus(TaskStatus.PENDING);
            } else {
                task.setStatus(TaskStatus.COMPLETED);
            }

            List<Task> sessionTasks = getSessionTasks();
            if (sessionTasks != null) {
                try {
                    taskRepository.save(task);
                } catch (Exception ignored) {}
                syncToLocalStorage();
                return task;
            } else {
                return taskRepository.save(task);
            }
        }
        return null;
    }

    // --- Statistics and Calculations ---

    /**
     * Total number of tasks.
     */
    public int getTotalCount() {
        return getAllTasks().size();
    }

    /**
     * Count of tasks currently pending.
     */
    public int getPendingCount() {
        List<Task> tasks = getAllTasks();
        int pending = 0;
        for (Task task : tasks) {
            if (task.getStatus() == TaskStatus.PENDING) {
                pending++;
            }
        }
        return pending;
    }

    /**
     * Count of tasks completed.
     */
    public int getCompletedCount() {
        List<Task> tasks = getAllTasks();
        int completed = 0;
        for (Task task : tasks) {
            if (task.getStatus() == TaskStatus.COMPLETED) {
                completed++;
            }
        }
        return completed;
    }

    /**
     * Count of overdue tasks (pending and due date before today).
     */
    public int getOverdueCount() {
        List<Task> tasks = getAllTasks();
        int overdue = 0;
        for (Task task : tasks) {
            if (task.isOverdue()) {
                overdue++;
            }
        }
        return overdue;
    }

    /**
     * Calculates completion percentage: (Completed / Total) * 100.
     * Safely handles total == 0 to prevent division by zero.
     */
    public int getCompletionPercentage() {
        List<Task> tasks = getAllTasks();
        int total = tasks.size();
        if (total == 0) {
            return 0;
        }

        int completed = 0;
        for (Task task : tasks) {
            if (task.getStatus() == TaskStatus.COMPLETED) {
                completed++;
            }
        }

        double percentage = ((double) completed / (double) total) * 100.0;
        return (int) Math.round(percentage);
    }

    /**
     * Returns upcoming pending tasks, up to the requested limit.
     */
    public List<Task> getUpcomingTasks(int limit) {
        List<Task> all = getAllTasks();
        List<Task> pending = new ArrayList<>();

        for (Task task : all) {
            if (task.getStatus() == TaskStatus.PENDING) {
                pending.add(task);
            }
        }

        pending.sort(Comparator.comparing(Task::getDueDate));

        if (pending.size() > limit) {
            return new ArrayList<>(pending.subList(0, limit));
        }
        return pending;
    }

    /**
     * Filters and searches tasks by text, status, and priority.
     *
     * @param query          text to match against title or subject (case-insensitive)
     * @param statusFilter   "ALL", "PENDING", or "COMPLETED"
     * @param priorityFilter "ALL", "HIGH", "MEDIUM", or "LOW"
     */
    public List<Task> searchAndFilter(String query, String statusFilter, String priorityFilter) {
        List<Task> all = getAllTasks();
        List<Task> filtered = new ArrayList<>();

        String cleanQuery = (query != null) ? query.trim().toLowerCase() : "";

        for (Task task : all) {
            // 1. Text Search Filter (Title or Subject)
            boolean matchesSearch = true;
            if (!cleanQuery.isEmpty()) {
                String title = task.getTitle() != null ? task.getTitle().toLowerCase() : "";
                String subject = task.getSubject() != null ? task.getSubject().toLowerCase() : "";
                matchesSearch = title.contains(cleanQuery) || subject.contains(cleanQuery);
            }

            // 2. Status Filter
            boolean matchesStatus = true;
            if (statusFilter != null && !statusFilter.equalsIgnoreCase("ALL")) {
                if (task.getStatus() == null || !task.getStatus().name().equalsIgnoreCase(statusFilter)) {
                    matchesStatus = false;
                }
            }

            // 3. Priority Filter
            boolean matchesPriority = true;
            if (priorityFilter != null && !priorityFilter.equalsIgnoreCase("ALL")) {
                if (task.getPriority() == null || !task.getPriority().name().equalsIgnoreCase(priorityFilter)) {
                    matchesPriority = false;
                }
            }

            if (matchesSearch && matchesStatus && matchesPriority) {
                filtered.add(task);
            }
        }

        return filtered;
    }

    /**
     * Resets tasks to default sample tasks and syncs with browser LocalStorage.
     */
    public void resetToSampleData() {
        List<Task> sessionTasks = getSessionTasks();
        if (sessionTasks != null) {
            sessionTasks.clear();
            sessionTasks.addAll(createDefaultSampleTasks());
            taskRepository.deleteAll();
            for (Task t : sessionTasks) {
                try {
                    taskRepository.save(t);
                } catch (Exception ignored) {}
            }
            syncToLocalStorage();
        } else {
            taskRepository.deleteAll();
            initSampleData();
        }
    }
}
