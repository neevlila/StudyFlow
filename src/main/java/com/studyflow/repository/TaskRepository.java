package com.studyflow.repository;

import com.studyflow.model.Priority;
import com.studyflow.model.Task;
import com.studyflow.model.TaskStatus;
import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * TaskRepository handles all direct SQL database interactions with H2.
 * Uses Spring's JdbcTemplate with parameterized queries to prevent SQL Injection.
 * This is straightforward for beginners to explain during a college viva.
 */
@Repository
public class TaskRepository {

    private final JdbcTemplate jdbcTemplate;

    public TaskRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Automatically ensures the tasks table exists in H2 upon application startup.
     */
    @PostConstruct
    public void initTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS tasks (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                title VARCHAR(200) NOT NULL,
                subject VARCHAR(100) NOT NULL,
                description VARCHAR(1000),
                due_date DATE NOT NULL,
                priority VARCHAR(20) NOT NULL,
                status VARCHAR(20) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
            """;
        jdbcTemplate.execute(sql);
    }

    /**
     * RowMapper to convert each SQL database row into a Java Task object.
     */
    private final RowMapper<Task> taskRowMapper = new RowMapper<Task>() {
        @Override
        public Task mapRow(ResultSet rs, int rowNum) throws SQLException {
            Task task = new Task();
            task.setId(rs.getLong("id"));
            task.setTitle(rs.getString("title"));
            task.setSubject(rs.getString("subject"));
            task.setDescription(rs.getString("description"));

            if (rs.getDate("due_date") != null) {
                task.setDueDate(rs.getDate("due_date").toLocalDate());
            }

            String priorityStr = rs.getString("priority");
            if (priorityStr != null) {
                try {
                    task.setPriority(Priority.valueOf(priorityStr.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    task.setPriority(Priority.MEDIUM);
                }
            }

            String statusStr = rs.getString("status");
            if (statusStr != null) {
                try {
                    task.setStatus(TaskStatus.valueOf(statusStr.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    task.setStatus(TaskStatus.PENDING);
                }
            }

            Timestamp ts = rs.getTimestamp("created_at");
            if (ts != null) {
                task.setCreatedAt(ts.toLocalDateTime());
            }

            return task;
        }
    };

    /**
     * CREATE or UPDATE a task.
     * If the task ID is null, perform an SQL INSERT using standard KeyHolder.
     * If the task ID is present, perform an SQL UPDATE.
     */
    public Task save(Task task) {
        if (task.getId() == null) {
            String insertSql = """
                INSERT INTO tasks (title, subject, description, due_date, priority, status, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(insertSql, new String[]{"ID"});
                ps.setString(1, task.getTitle());
                ps.setString(2, task.getSubject());
                ps.setString(3, task.getDescription());
                ps.setDate(4, task.getDueDate() != null ? Date.valueOf(task.getDueDate()) : Date.valueOf(java.time.LocalDate.now()));
                ps.setString(5, task.getPriority() != null ? task.getPriority().name() : Priority.MEDIUM.name());
                ps.setString(6, task.getStatus() != null ? task.getStatus().name() : TaskStatus.PENDING.name());
                ps.setTimestamp(7, task.getCreatedAt() != null ? Timestamp.valueOf(task.getCreatedAt()) : new Timestamp(System.currentTimeMillis()));
                return ps;
            }, keyHolder);

            Map<String, Object> keys = keyHolder.getKeys();
            if (keys != null) {
                // Find ID column case-insensitively
                for (Map.Entry<String, Object> entry : keys.entrySet()) {
                    if ("ID".equalsIgnoreCase(entry.getKey()) && entry.getValue() instanceof Number num) {
                        task.setId(num.longValue());
                        break;
                    }
                }
            } else if (keyHolder.getKey() != null) {
                task.setId(keyHolder.getKey().longValue());
            }
        } else {
            String updateSql = """
                UPDATE tasks
                SET title = ?, subject = ?, description = ?, due_date = ?, priority = ?, status = ?
                WHERE id = ?
                """;
            jdbcTemplate.update(
                updateSql,
                task.getTitle(),
                task.getSubject(),
                task.getDescription(),
                task.getDueDate() != null ? Date.valueOf(task.getDueDate()) : Date.valueOf(java.time.LocalDate.now()),
                task.getPriority().name(),
                task.getStatus().name(),
                task.getId()
            );
        }
        return task;
    }

    /**
     * READ: Fetch all tasks sorted by due date (earliest deadline first).
     */
    public List<Task> findAll() {
        String sql = "SELECT * FROM tasks ORDER BY due_date ASC, id DESC";
        return jdbcTemplate.query(sql, taskRowMapper);
    }

    /**
     * READ: Fetch a single task by its unique ID.
     */
    public Optional<Task> findById(Long id) {
        String sql = "SELECT * FROM tasks WHERE id = ?";
        List<Task> results = jdbcTemplate.query(sql, taskRowMapper, id);
        if (results.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(results.get(0));
    }

    /**
     * DELETE: Remove a task from the database by ID.
     */
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM tasks WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        return rowsAffected > 0;
    }

    /**
     * Total number of tasks in the database.
     */
    public int count() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tasks", Integer.class);
        return count != null ? count : 0;
    }

    /**
     * Wipe all tasks (useful for testing or resetting sample data).
     */
    public void deleteAll() {
        jdbcTemplate.update("DELETE FROM tasks");
    }
}
