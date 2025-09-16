package com.example.demo.controller;

import com.example.demo.model.Grievance;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.services.GrievanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/grievances")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class GrievanceController {
    
    private final GrievanceService grievanceService;
    private final UserRepository userRepository;
    private final com.example.demo.services.SpeechToTextService speechToTextService;
    
    @PostMapping
    public ResponseEntity<Grievance> submitGrievance(@Valid @RequestBody Grievance grievance, 
                                                   @RequestParam(required = false) Long userId,
                                                   Authentication authentication) {
        try {
            // Set the user from authentication
            if (authentication != null && authentication.getPrincipal() instanceof User) {
                User user = (User) authentication.getPrincipal();
                grievance.setUser(user);
            } else if (userId != null) {
                userRepository.findById(userId).ifPresent(grievance::setUser);
            }
            
            Grievance submittedGrievance = grievanceService.submitGrievance(grievance);
            return ResponseEntity.status(HttpStatus.CREATED).body(submittedGrievance);
            
        } catch (Exception e) {
            log.error("Error submitting grievance: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Support multipart submissions from the frontend (files are currently ignored)
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Grievance> submitGrievanceMultipart(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String inputType,
            @RequestParam(required = false, defaultValue = "en") String language,
            @RequestParam(required = false) Long userId,
            @RequestPart(required = false) MultipartFile audioFile,
            @RequestPart(required = false) MultipartFile[] attachments,
            Authentication authentication
    ) {
        try {
            Grievance grievance = new Grievance();
            grievance.setTitle(title);
            String finalDescription = description;
            if (audioFile != null && !audioFile.isEmpty()) {
                try {
                    String transcript = speechToTextService.transcribe(audioFile.getBytes(), audioFile.getContentType(), language);
                    if (transcript != null && !transcript.isBlank()) {
                        finalDescription = (finalDescription == null || finalDescription.isBlank()) ? transcript : (finalDescription + "\n" + transcript);
                    }
                } catch (Exception e) {
                    log.warn("STT transcription failed: {}", e.getMessage());
                }
            }
            grievance.setDescription(finalDescription);
            // Safe enum parsing (ignore empty/invalid values)
            if (type != null && !type.isBlank()) {
                try { grievance.setType(Grievance.GrievanceType.valueOf(type.trim().toUpperCase())); } catch (Exception ignored) {}
            }
            if (priority != null && !priority.isBlank()) {
                try { grievance.setPriority(Grievance.GrievancePriority.valueOf(priority.trim().toUpperCase())); } catch (Exception ignored) {}
            }
            if (inputType != null && !inputType.isBlank()) {
                try { grievance.setInputType(Grievance.InputType.valueOf(inputType.trim().toUpperCase())); } catch (Exception ignored) {}
            }
            grievance.setLanguage(language);

            if (authentication != null && authentication.getPrincipal() instanceof User) {
                grievance.setUser((User) authentication.getPrincipal());
            } else if (userId != null) {
                userRepository.findById(userId).ifPresent(grievance::setUser);
            }

            // Note: file persistence omitted for brevity; can be added later
            Grievance submitted = grievanceService.submitGrievance(grievance);
            return ResponseEntity.status(HttpStatus.CREATED).body(submitted);

        } catch (Exception e) {
            log.error("Error submitting grievance (multipart): {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/track/{trackingNumber}")
    public ResponseEntity<Grievance> getGrievanceByTrackingNumber(@PathVariable String trackingNumber) {
        Optional<Grievance> grievance = grievanceService.getGrievanceByTrackingNumber(trackingNumber);
        return grievance.map(ResponseEntity::ok)
                       .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<Grievance>> getGrievancesByUser(@PathVariable Long userId, 
                                                             Pageable pageable) {
        Page<Grievance> grievances = grievanceService.getGrievancesByUser(userId, pageable);
        log.info("Fetched grievances for userId={} page={} size={} returned={}",
                userId,
                pageable.getPageNumber(),
                pageable.getPageSize(),
                grievances.getNumberOfElements());
        return ResponseEntity.ok(grievances);
    }
    
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<Page<Grievance>> getGrievancesByDepartment(@PathVariable Long departmentId, 
                                                                   Pageable pageable) {
        Page<Grievance> grievances = grievanceService.getGrievancesByDepartment(departmentId, pageable);
        return ResponseEntity.ok(grievances);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<Grievance>> getGrievancesByStatus(@PathVariable Grievance.GrievanceStatus status, 
                                                               Pageable pageable) {
        Page<Grievance> grievances = grievanceService.getGrievancesByStatus(status, pageable);
        return ResponseEntity.ok(grievances);
    }
    
    @GetMapping("/overdue")
    public ResponseEntity<List<Grievance>> getOverdueGrievances() {
        List<Grievance> overdueGrievances = grievanceService.getOverdueGrievances();
        return ResponseEntity.ok(overdueGrievances);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Grievance>> searchGrievances(@RequestParam String keyword) {
        List<Grievance> grievances = grievanceService.searchGrievances(keyword);
        return ResponseEntity.ok(grievances);
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<Grievance>> filterGrievances(
            @RequestParam(required = false) Grievance.GrievanceType type,
            @RequestParam(required = false) Grievance.GrievanceStatus status,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Double minConfidence,
            @RequestParam(required = false) Double maxConfidence,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            Pageable pageable) {
        LocalDateTime fromDate = null;
        LocalDateTime toDate = null;
        DateTimeFormatter iso = DateTimeFormatter.ISO_DATE_TIME;
        try {
            if (from != null && !from.isBlank()) fromDate = LocalDateTime.parse(from, iso);
            if (to != null && !to.isBlank()) toDate = LocalDateTime.parse(to, iso);
        } catch (Exception ignored) {}

        Page<Grievance> page = grievanceService.filterGrievances(type, status, departmentId, minConfidence, maxConfidence, fromDate, toDate, pageable);
        return ResponseEntity.ok(page);
    }
    
    @PutMapping("/{grievanceId}/status")
    public ResponseEntity<Grievance> updateGrievanceStatus(@PathVariable Long grievanceId,
                                                         @RequestParam Grievance.GrievanceStatus status,
                                                         @RequestParam(required = false) String reason,
                                                         Authentication authentication) {
        try {
            User changedBy = null;
            if (authentication != null && authentication.getPrincipal() instanceof User) {
                changedBy = (User) authentication.getPrincipal();
            }
            
            Grievance updatedGrievance = grievanceService.updateGrievanceStatus(grievanceId, status, reason, changedBy);
            return ResponseEntity.ok(updatedGrievance);
            
        } catch (RuntimeException e) {
            log.error("Error updating grievance status: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error updating grievance status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PutMapping("/{grievanceId}/assign")
    public ResponseEntity<Grievance> assignGrievance(@PathVariable Long grievanceId,
                                                   @RequestParam(required = false) Long departmentId,
                                                   @RequestParam(required = false) Long officerId) {
        try {
            Grievance assignedGrievance = grievanceService.assignGrievance(grievanceId, departmentId, officerId);
            return ResponseEntity.ok(assignedGrievance);
            
        } catch (RuntimeException e) {
            log.error("Error assigning grievance: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error assigning grievance: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/stats/status/{status}")
    public ResponseEntity<Long> getGrievanceCountByStatus(@PathVariable Grievance.GrievanceStatus status) {
        Long count = grievanceService.getGrievanceCountByStatus(status);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/stats/department/{departmentId}/status/{status}")
    public ResponseEntity<Long> getGrievanceCountByDepartmentAndStatus(@PathVariable Long departmentId,
                                                                     @PathVariable Grievance.GrievanceStatus status) {
        Long count = grievanceService.getGrievanceCountByDepartmentAndStatus(departmentId, status);
        return ResponseEntity.ok(count);
    }

    @DeleteMapping("/{grievanceId}")
    public ResponseEntity<Void> deleteGrievance(@PathVariable Long grievanceId,
                                              @RequestParam(required = false) Long userId,
                                              Authentication authentication) {
        try {
            // Allow owner or admin
            if (authentication != null && authentication.getPrincipal() instanceof User user) {
                if (user.getRole() == User.UserRole.ADMIN) {
                    grievanceService.deleteGrievance(grievanceId);
                } else {
                    grievanceService.deleteGrievanceOwned(grievanceId, user.getId());
                }
            } else if (userId != null) {
                // Fallback: verify by provided userId (when frontend cannot attach auth)
                grievanceService.deleteGrievanceOwned(grievanceId, userId);
            } else {
                // No auth: reject to avoid deleting others' records
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.noContent().build();
        } catch (org.springframework.security.access.AccessDeniedException e) {
            log.warn("Delete denied for grievance {}: {}", grievanceId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            log.error("Error deleting grievance: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error deleting grievance: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/by-tracking/{trackingNumber}")
    public ResponseEntity<Void> deleteGrievanceByTracking(@PathVariable String trackingNumber,
                                                        @RequestParam(required = false) Long userId,
                                                        Authentication authentication) {
        try {
            if (authentication != null && authentication.getPrincipal() instanceof User user) {
                if (user.getRole() == User.UserRole.ADMIN) {
                    var opt = grievanceService.getGrievanceByTrackingNumber(trackingNumber);
                    if (opt.isEmpty()) return ResponseEntity.notFound().build();
                    grievanceService.deleteGrievance(opt.get().getId());
                } else {
                    grievanceService.deleteGrievanceOwnedByTracking(trackingNumber, user.getId());
                }
                return ResponseEntity.noContent().build();
            } else if (userId != null) {
                grievanceService.deleteGrievanceOwnedByTracking(trackingNumber, userId);
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            log.warn("Delete by tracking failed: tracking={}, userId={}", trackingNumber, userId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{grievanceId}/history")
    public ResponseEntity<List<com.example.demo.model.GrievanceStatusHistory>> getGrievanceHistory(@PathVariable Long grievanceId) {
        try {
            List<com.example.demo.model.GrievanceStatusHistory> history = grievanceService.getHistory(grievanceId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("Error fetching grievance history: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
