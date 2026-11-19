package com.studyflow.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Lightweight Health Monitoring Controller.
 * 
 * Provides ultra-fast, zero-overhead HTTP GET endpoints for:
 * - cron-job.org / UptimeRobot (to keep Render free tier awake 24/7)
 * - Cloud platform container health checks
 * - System status probes
 */
@RestController
public class HealthController {

    private static final LocalDateTime START_TIME = LocalDateTime.now();

    @GetMapping(value = {"/health", "/api/health", "/ping"}, produces = "application/json")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "UP");
        response.put("application", "StudyFlow");
        response.put("environment", "Cloud / Render Ready");
        response.put("storage", "Browser LocalStorage + In-Memory H2");
        response.put("startedAt", START_TIME.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        return ResponseEntity.ok(response);
    }
}
