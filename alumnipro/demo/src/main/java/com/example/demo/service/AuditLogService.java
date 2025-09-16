package com.example.demo.service;

import com.example.demo.model.AuditLog;
import com.example.demo.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void logAction(String action, String entityType, Long entityId, String oldValue, String newValue, String performedBy) {
        AuditLog auditLog = new AuditLog(action, entityType, entityId, oldValue, newValue, performedBy);
        auditLogRepository.save(auditLog);
    }

    public List<AuditLog> getAuditLogsByEntity(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId);
    }

    public List<AuditLog> getAuditLogsByUser(String performedBy) {
        return auditLogRepository.findByPerformedBy(performedBy);
    }

    public List<AuditLog> getAuditLogsByDateRange(LocalDateTime start, LocalDateTime end) {
        return auditLogRepository.findByTimestampBetween(start, end);
    }

    public List<AuditLog> getAuditLogsByAction(String action) {
        return auditLogRepository.findByAction(action);
    }

    public List<AuditLog> getAllAuditLogs() {
        return auditLogRepository.findAll();
    }
}
