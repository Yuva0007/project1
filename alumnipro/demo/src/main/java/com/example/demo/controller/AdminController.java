package com.example.demo.controller;

import com.example.demo.model.AuditLog;
import com.example.demo.service.AuditLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final AuditLogService auditLogService;

    public AdminController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLog>> getAllAuditLogs() {
        List<AuditLog> logs = auditLogService.getAllAuditLogs();
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/audit-logs/user/{performedBy}")
    public ResponseEntity<List<AuditLog>> getAuditLogsByUser(@PathVariable String performedBy) {
        List<AuditLog> logs = auditLogService.getAuditLogsByUser(performedBy);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/audit-logs/entity/{entityType}/{entityId}")
    public ResponseEntity<List<AuditLog>> getAuditLogsByEntity(
            @PathVariable String entityType,
            @PathVariable Long entityId) {
        List<AuditLog> logs = auditLogService.getAuditLogsByEntity(entityType, entityId);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/audit-logs/date-range")
    public ResponseEntity<List<AuditLog>> getAuditLogsByDateRange(
            @RequestParam String start,
            @RequestParam String end) {
        List<AuditLog> logs = auditLogService.getAuditLogsByDateRange(
                java.time.LocalDateTime.parse(start),
                java.time.LocalDateTime.parse(end));
        return ResponseEntity.ok(logs);
    }
}
