package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "grievances")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Grievance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Title is required")
    @Size(max = 200)
    private String title;
    
    @NotBlank(message = "Description is required")
    @Size(max = 2000)
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    private GrievanceType type;
    
    @Enumerated(EnumType.STRING)
    private GrievancePriority priority = GrievancePriority.MEDIUM;
    
    @Enumerated(EnumType.STRING)
    private GrievanceStatus status = GrievanceStatus.SUBMITTED;
    
    @Enumerated(EnumType.STRING)
    private InputType inputType = InputType.TEXT;
    
    @Size(max = 50)
    private String language = "en";
    
    @Column(name = "ai_category")
    private String aiCategory;
    
    @Column(name = "ai_confidence")
    private Double aiConfidence;
    
    @Column(name = "tracking_number", unique = true)
    private String trackingNumber;
    
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;
    
    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;
    
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
    
    @Column(name = "expected_resolution_date")
    private LocalDateTime expectedResolutionDate;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"authorities", "hibernateLazyInitializer", "handler", "password"})
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Department department;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_officer_id")
    @JsonIgnoreProperties({"authorities", "hibernateLazyInitializer", "handler", "password"})
    private User assignedOfficer;
    
    // Removed bidirectional relationships to avoid circular dependency
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        submittedAt = LocalDateTime.now();
        generateTrackingNumber();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    private void generateTrackingNumber() {
        if (trackingNumber == null) {
            trackingNumber = "GRV" + System.currentTimeMillis();
        }
    }
    
    public enum GrievanceType {
        INFRASTRUCTURE, HEALTHCARE, EDUCATION, TRANSPORTATION, 
        UTILITIES, ENVIRONMENT, SAFETY_SECURITY, CORRUPTION,
        CIVIL_RIGHTS, HOUSING, EMPLOYMENT, OTHER
    }
    
    public enum GrievancePriority {
        LOW, MEDIUM, HIGH, URGENT
    }
    
    public enum GrievanceStatus {
        SUBMITTED, UNDER_REVIEW, ASSIGNED, IN_PROGRESS, 
        PENDING_INFORMATION, RESOLVED, CLOSED, REJECTED
    }
    
    public enum InputType {
        TEXT, VOICE, IMAGE, VIDEO
    }
}
