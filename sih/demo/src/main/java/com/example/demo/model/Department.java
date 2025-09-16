package com.example.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "departments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Department {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Department name is required")
    @Size(max = 100)
    @Column(unique = true)
    private String name;
    
    @Size(max = 500)
    private String description;
    
    @NotBlank(message = "Department code is required")
    @Size(max = 10)
    @Column(unique = true)
    private String code;
    
    @Size(max = 200)
    private String address;
    
    @Size(max = 15)
    private String phoneNumber;
    
    @Size(max = 100)
    private String email;
    
    @Enumerated(EnumType.STRING)
    private DepartmentStatus status = DepartmentStatus.ACTIVE;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Removed bidirectional relationships to avoid circular dependency
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum DepartmentStatus {
        ACTIVE, INACTIVE, UNDER_MAINTENANCE
    }
}
