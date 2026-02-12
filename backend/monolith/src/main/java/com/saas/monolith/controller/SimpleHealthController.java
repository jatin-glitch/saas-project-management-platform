package com.saas.monolith.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SimpleHealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "saas-monolith");
        status.put("timestamp", String.valueOf(System.currentTimeMillis()));
        
        return ResponseEntity.ok(status);
    }

    @GetMapping("/auth/health")
    public ResponseEntity<Map<String, String>> authHealth() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "authentication-service");
        status.put("timestamp", String.valueOf(System.currentTimeMillis()));
        
        return ResponseEntity.ok(status);
    }

    @GetMapping("/notifications/health")
    public ResponseEntity<Map<String, String>> notificationHealth() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "notification-service");
        status.put("timestamp", String.valueOf(System.currentTimeMillis()));
        
        return ResponseEntity.ok(status);
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("application", "SaaS Platform Monolith");
        info.put("version", "1.0.0");
        info.put("description", "Simplified monolith version without Kafka/messaging");
        info.put("endpoints", Map.of(
            "health", "/api/health",
            "auth", "/api/auth",
            "projects", "/api/projects",
            "tasks", "/api/tasks",
            "notifications", "/api/notifications"
        ));
        
        return ResponseEntity.ok(info);
    }
}
