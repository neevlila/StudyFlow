package com.studyflow.service;

import com.studyflow.model.Priority;
import com.studyflow.model.Task;
import com.studyflow.model.TaskStatus;
import com.studyflow.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TaskServiceTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
    }

    @Test
    @DisplayName("Test Task Creation and Retrieval")
    void testCreateAndRetrieveTask() {
        Task task = new Task("Test DAA Assignment", "DAA", "Binary Search implementation",
                LocalDate.now().plusDays(2), Priority.HIGH);

        Task saved = taskService.saveTask(task);
        assertNotNull(saved.getId(), "Generated ID should not be null");

        Optional<Task> retrieved = taskService.getTaskById(saved.getId());
        assertTrue(retrieved.isPresent(), "Task should be found by ID");
        assertEquals("Test DAA Assignment", retrieved.get().getTitle());
        assertEquals("DAA", retrieved.get().getSubject());
        assertEquals(Priority.HIGH, retrieved.get().getPriority());
        assertEquals(TaskStatus.PENDING, retrieved.get().getStatus());
    }

    @Test
    @DisplayName("Test Task Validation - Empty title should throw exception")
    void testTaskValidationEmptyTitle() {
        Task task = new Task("", "Java", "Notes", LocalDate.now(), Priority.MEDIUM);
        assertThrows(IllegalArgumentException.class, () -> taskService.saveTask(task));
    }

    @Test
    @DisplayName("Test Task Validation - Empty subject should throw exception")
    void testTaskValidationEmptySubject() {
        Task task = new Task("Valid Title", "   ", "Notes", LocalDate.now(), Priority.MEDIUM);
        assertThrows(IllegalArgumentException.class, () -> taskService.saveTask(task));
    }

    @Test
    @DisplayName("Test Task Update")
    void testUpdateTask() {
        Task task = new Task("Original Title", "DBMS", "Desc", LocalDate.now(), Priority.LOW);
        Task saved = taskService.saveTask(task);

        saved.setTitle("Updated Title");
        saved.setPriority(Priority.HIGH);
        taskService.saveTask(saved);

        Optional<Task> updated = taskService.getTaskById(saved.getId());
        assertTrue(updated.isPresent());
        assertEquals("Updated Title", updated.get().getTitle());
        assertEquals(Priority.HIGH, updated.get().getPriority());
    }

    @Test
    @DisplayName("Test Task Deletion")
    void testDeleteTask() {
        Task task = new Task("To Delete", "OS", "Desc", LocalDate.now(), Priority.LOW);
        Task saved = taskService.saveTask(task);

        boolean deleted = taskService.deleteTask(saved.getId());
        assertTrue(deleted, "Delete should return true");

        Optional<Task> retrieved = taskService.getTaskById(saved.getId());
        assertFalse(retrieved.isPresent(), "Deleted task should no longer exist");
    }

    @Test
    @DisplayName("Test Status Toggle between PENDING and COMPLETED")
    void testToggleStatus() {
        Task task = new Task("Toggle Task", "Networks", "Desc", LocalDate.now(), Priority.MEDIUM);
        Task saved = taskService.saveTask(task);
        assertEquals(TaskStatus.PENDING, saved.getStatus());

        Task toggled1 = taskService.toggleStatus(saved.getId());
        assertEquals(TaskStatus.COMPLETED, toggled1.getStatus());

        Task toggled2 = taskService.toggleStatus(saved.getId());
        assertEquals(TaskStatus.PENDING, toggled2.getStatus());
    }

    @Test
    @DisplayName("Test Statistics Calculations & Division-by-Zero safety")
    void testStatisticsCalculations() {
        // Zero tasks: percentage should safely be 0
        assertEquals(0, taskService.getCompletionPercentage());
        assertEquals(0, taskService.getTotalCount());

        LocalDate today = LocalDate.now();

        // 1 pending future task
        taskService.saveTask(new Task("Future 1", "Java", "", today.plusDays(3), Priority.MEDIUM));
        // 1 pending overdue task
        taskService.saveTask(new Task("Overdue 1", "Java", "", today.minusDays(2), Priority.HIGH));
        // 2 completed tasks
        Task c1 = new Task("Done 1", "Java", "", today.minusDays(1), Priority.LOW);
        c1.setStatus(TaskStatus.COMPLETED);
        taskService.saveTask(c1);

        Task c2 = new Task("Done 2", "DAA", "", today.minusDays(1), Priority.LOW);
        c2.setStatus(TaskStatus.COMPLETED);
        taskService.saveTask(c2);

        // Total = 4, Completed = 2, Pending = 2, Overdue = 1
        assertEquals(4, taskService.getTotalCount());
        assertEquals(2, taskService.getPendingCount());
        assertEquals(2, taskService.getCompletedCount());
        assertEquals(1, taskService.getOverdueCount());
        // Percentage: (2 / 4) * 100 = 50%
        assertEquals(50, taskService.getCompletionPercentage());
    }

    @Test
    @DisplayName("Test Search and Filter Logic")
    void testSearchAndFilter() {
        taskService.saveTask(new Task("Dijkstra Algorithm", "DAA", "Graph search", LocalDate.now(), Priority.HIGH));
        taskService.saveTask(new Task("Polymorphism Lab", "Java", "OOP principles", LocalDate.now(), Priority.MEDIUM));

        Task completedTask = new Task("SQL Joins Practice", "DBMS", "Relational queries", LocalDate.now(), Priority.LOW);
        completedTask.setStatus(TaskStatus.COMPLETED);
        taskService.saveTask(completedTask);

        // Search by title keyword (case-insensitive)
        List<Task> searchResult = taskService.searchAndFilter("dijkstra", "ALL", "ALL");
        assertEquals(1, searchResult.size());
        assertEquals("Dijkstra Algorithm", searchResult.get(0).getTitle());

        // Search by subject keyword
        List<Task> subjectResult = taskService.searchAndFilter("java", "ALL", "ALL");
        assertEquals(1, subjectResult.size());

        // Filter by Status: Completed
        List<Task> completedFilter = taskService.searchAndFilter("", "COMPLETED", "ALL");
        assertEquals(1, completedFilter.size());
        assertEquals("SQL Joins Practice", completedFilter.get(0).getTitle());

        // Filter by Priority: High
        List<Task> highFilter = taskService.searchAndFilter("", "ALL", "HIGH");
        assertEquals(1, highFilter.size());
        assertEquals("Dijkstra Algorithm", highFilter.get(0).getTitle());
    }
}
