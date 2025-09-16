package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "grievance_status_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GrievanceStatusHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    private Grievance.GrievanceStatus fromStatus;
    
    @Enumerated(EnumType.STRING)
    private Grievance.GrievanceStatus toStatus;
    
    @Size(max = 500)
    private String reason;
    
    @Column(name = "changed_at")
    private LocalDateTime changedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grievance_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Grievance grievance;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_id")
    @JsonIgnoreProperties({"password", "authorities", "hibernateLazyInitializer", "handler"})
    private User changedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_department_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Department assignedDepartment;
    
    @PrePersist
    protected void onCreate() {
        changedAt = LocalDateTime.now();
    }
}
