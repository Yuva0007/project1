package com.example.demo.services;

import com.example.demo.model.*;
import com.example.demo.repository.GrievanceRepository;
import com.example.demo.repository.DepartmentRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.GrievanceStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class GrievanceService {
    
    private final GrievanceRepository grievanceRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final AICategorizationService aiCategorizationService;
    private final NotificationService notificationService;
    private final GrievanceStatusHistoryRepository historyRepository;
    
    public Grievance submitGrievance(Grievance grievance) {
        log.info("Submitting new grievance: {}", grievance.getTitle());
        
        // AI Categorization
        AICategorizationService.GrievanceCategorizationResult categorization = 
            aiCategorizationService.categorizeGrievance(grievance.getTitle(), grievance.getDescription());
        
        grievance.setAiCategory(categorization.category());
        grievance.setAiConfidence(categorization.confidence());
        
        // Auto-assign if confidence is high enough
        if (aiCategorizationService.shouldAutoAssign(categorization)) {
            autoAssignGrievance(grievance, categorization.category());
        }
        
        Grievance savedGrievance = grievanceRepository.save(grievance);
        
        // Create initial status history
        createStatusHistory(savedGrievance, null, Grievance.GrievanceStatus.SUBMITTED, 
                           "Grievance submitted", savedGrievance.getUser());
        
        // Send notifications
        notificationService.sendGrievanceSubmittedNotification(savedGrievance);
        
        log.info("Grievance submitted successfully with tracking number: {}", savedGrievance.getTrackingNumber());
        return savedGrievance;
    }
    
    public Optional<Grievance> getGrievanceByTrackingNumber(String trackingNumber) {
        return grievanceRepository.findByTrackingNumber(trackingNumber);
    }
    
    public Page<Grievance> getGrievancesByUser(Long userId, Pageable pageable) {
        return grievanceRepository.findByUserIdOrderBySubmittedAtDesc(userId, pageable);
    }
    
    public Page<Grievance> getGrievancesByDepartment(Long departmentId, Pageable pageable) {
        return grievanceRepository.findByDepartmentId(departmentId, pageable);
    }
    
    public Page<Grievance> getGrievancesByStatus(Grievance.GrievanceStatus status, Pageable pageable) {
        return grievanceRepository.findByStatus(status, pageable);
    }
    
    public List<Grievance> getOverdueGrievances() {
        return grievanceRepository.findOverdueGrievances(LocalDateTime.now());
    }
    
    public Grievance updateGrievanceStatus(Long grievanceId, Grievance.GrievanceStatus newStatus, 
                                         String reason, User changedBy) {
        Optional<Grievance> optionalGrievance = grievanceRepository.findById(grievanceId);
        if (optionalGrievance.isEmpty()) {
            throw new RuntimeException("Grievance not found with id: " + grievanceId);
        }
        
        Grievance grievance = optionalGrievance.get();
        Grievance.GrievanceStatus oldStatus = grievance.getStatus();
        
        grievance.setStatus(newStatus);
        
        if (newStatus == Grievance.GrievanceStatus.RESOLVED) {
            grievance.setResolvedAt(LocalDateTime.now());
        }
        
        Grievance savedGrievance = grievanceRepository.save(grievance);
        
        // Create status history
        createStatusHistory(savedGrievance, oldStatus, newStatus, reason, changedBy);
        
        // Send notifications
        notificationService.sendStatusUpdateNotification(savedGrievance, oldStatus, newStatus);
        
        log.info("Grievance status updated from {} to {} for tracking number: {}", 
                oldStatus, newStatus, savedGrievance.getTrackingNumber());
        
        return savedGrievance;
    }
    
    public Grievance assignGrievance(Long grievanceId, Long departmentId, Long officerId) {
        Optional<Grievance> optionalGrievance = grievanceRepository.findById(grievanceId);
        if (optionalGrievance.isEmpty()) {
            throw new RuntimeException("Grievance not found with id: " + grievanceId);
        }
        
        Grievance grievance = optionalGrievance.get();
        
        if (departmentId != null) {
            Optional<Department> department = departmentRepository.findById(departmentId);
            department.ifPresent(grievance::setDepartment);
        }
        
        if (officerId != null) {
            Optional<User> officer = userRepository.findById(officerId);
            officer.ifPresent(grievance::setAssignedOfficer);
        }
        
        grievance.setAssignedAt(LocalDateTime.now());
        grievance.setStatus(Grievance.GrievanceStatus.ASSIGNED);
        
        Grievance savedGrievance = grievanceRepository.save(grievance);
        
        // Create status history
        createStatusHistory(savedGrievance, Grievance.GrievanceStatus.SUBMITTED, 
                           Grievance.GrievanceStatus.ASSIGNED, "Grievance assigned", null);
        
        // Send notifications
        notificationService.sendAssignmentNotification(savedGrievance);
        
        return savedGrievance;
    }
    
    public List<Grievance> searchGrievances(String keyword) {
        return grievanceRepository.findByKeyword(keyword);
    }

    public Page<Grievance> filterGrievances(
            Grievance.GrievanceType type,
            Grievance.GrievanceStatus status,
            Long departmentId,
            Double minConfidence,
            Double maxConfidence,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable) {
        Specification<Grievance> spec = Specification.where(null);
        if (type != null) spec = spec.and((root, q, cb) -> cb.equal(root.get("type"), type));
        if (status != null) spec = spec.and((root, q, cb) -> cb.equal(root.get("status"), status));
        if (departmentId != null) spec = spec.and((root, q, cb) -> cb.equal(root.get("department").get("id"), departmentId));
        if (minConfidence != null) spec = spec.and((root, q, cb) -> cb.greaterThanOrEqualTo(root.get("aiConfidence"), minConfidence));
        if (maxConfidence != null) spec = spec.and((root, q, cb) -> cb.lessThanOrEqualTo(root.get("aiConfidence"), maxConfidence));
        if (fromDate != null) spec = spec.and((root, q, cb) -> cb.greaterThanOrEqualTo(root.get("submittedAt"), fromDate));
        if (toDate != null) spec = spec.and((root, q, cb) -> cb.lessThanOrEqualTo(root.get("submittedAt"), toDate));
        return grievanceRepository.findAll(spec, pageable);
    }
    
    public Long getGrievanceCountByStatus(Grievance.GrievanceStatus status) {
        return grievanceRepository.countByStatus(status);
    }
    
    public Long getGrievanceCountByDepartmentAndStatus(Long departmentId, Grievance.GrievanceStatus status) {
        return grievanceRepository.countByDepartmentAndStatus(departmentId, status);
    }

    public void deleteGrievance(Long grievanceId) {
        if (!grievanceRepository.existsById(grievanceId)) {
            throw new RuntimeException("Grievance not found with id: " + grievanceId);
        }
        grievanceRepository.deleteById(grievanceId);
        log.info("Deleted grievance with id {}", grievanceId);
    }

    public void deleteGrievanceOwned(Long grievanceId, Long userId) {
        // Attempt atomic delete; check existence first for clear 404
        if (!grievanceRepository.existsById(grievanceId)) {
            throw new RuntimeException("Grievance not found with id: " + grievanceId);
        }
        var owned = grievanceRepository.findByIdAndUserId(grievanceId, userId);
        if (owned.isPresent()) {
            grievanceRepository.deleteByIdAndUserId(grievanceId, userId);
        } else {
            // Handle legacy/orphan records without user linkage
            var g = grievanceRepository.findById(grievanceId).orElseThrow();
            Long ownerId = g.getUser() != null ? g.getUser().getId() : null;
            if (ownerId == null && userId != null) {
                grievanceRepository.delete(g);
            } else {
                throw new org.springframework.security.access.AccessDeniedException("Not owner");
            }
        }
        log.info("Deleted grievance {} owned by user {}", grievanceId, userId);
    }

    public void deleteGrievanceOwnedByTracking(String trackingNumber, Long userId) {
        var opt = grievanceRepository.findByTrackingNumber(trackingNumber)
                .or(() -> grievanceRepository.findByTrackingNumberTrim(trackingNumber))
                .or(() -> grievanceRepository.findByTrackingNumberFlexible(trackingNumber));
        if (opt.isEmpty()) {
            throw new RuntimeException("Grievance not found with tracking: " + trackingNumber);
        }
        var g = opt.get();
        Long ownerId = g.getUser() != null ? g.getUser().getId() : null;
        if (ownerId == null) {
            // Legacy/orphan: allow user provided to delete
            if (userId != null) {
                grievanceRepository.delete(g);
            } else {
                throw new org.springframework.security.access.AccessDeniedException("Not owner");
            }
        } else if (ownerId.equals(userId)) {
            grievanceRepository.delete(g);
        } else {
            throw new org.springframework.security.access.AccessDeniedException("Not owner");
        }
        log.info("Deleted grievance {} (tracking {}) owned by user {}", g.getId(), trackingNumber, userId);
    }
    
    private void autoAssignGrievance(Grievance grievance, String category) {
        // Simple auto-assignment logic based on category
        // In a real implementation, this would be more sophisticated
        List<Department> departments = departmentRepository.findActiveDepartments();
        
        if (!departments.isEmpty()) {
            // For now, assign to the first active department
            // In production, you'd have more sophisticated mapping logic
            grievance.setDepartment(departments.get(0));
            grievance.setStatus(Grievance.GrievanceStatus.ASSIGNED);
            grievance.setAssignedAt(LocalDateTime.now());
        }
    }
    
    private void createStatusHistory(Grievance grievance, Grievance.GrievanceStatus fromStatus, 
                                   Grievance.GrievanceStatus toStatus, String reason, User changedBy) {
        GrievanceStatusHistory history = new GrievanceStatusHistory();
        history.setGrievance(grievance);
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        history.setReason(reason);
        history.setChangedBy(changedBy);
        // capture assigned department if present
        history.setAssignedDepartment(grievance.getDepartment());
        history.setChangedAt(LocalDateTime.now());
        historyRepository.save(history);
        log.info("Status history created: {} -> {} for grievance {}", fromStatus, toStatus, grievance.getId());
    }

    public List<GrievanceStatusHistory> getHistory(Long grievanceId) {
        return historyRepository.findByGrievanceIdOrderByChangedAt(grievanceId);
    }
}
