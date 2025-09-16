package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "job_postings")
public class JobPosting {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobType jobType; // FULL_TIME, PART_TIME, INTERNSHIP, CONTRACT
    
    @Column(nullable = false, length = 100)
    private String company;
    
    @Column(length = 100)
    private String location;
    
    @Column(length = 50)
    private String department;
    
    @Column(length = 100)
    private String experienceLevel; // ENTRY, MID, SENIOR, EXECUTIVE
    
    @Column(length = 200)
    private String skills;
    
    @Column(length = 100)
    private String salaryRange;
    
    @Column(length = 500)
    private String requirements;
    
    @Column(length = 500)
    private String benefits;
    
    @Column(length = 200)
    private String applicationEmail;
    
    @Column(length = 200)
    private String applicationUrl;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status = JobStatus.PENDING; // PENDING, APPROVED, REJECTED
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(nullable = false)
    private LocalDateTime applicationDeadline;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "posted_by", nullable = false)
    private User postedBy;
    
    @OneToMany(mappedBy = "jobPosting", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Application> applications = new ArrayList<>();
    
    // Constructors
    public JobPosting() {}
    
    public JobPosting(String title, String description, JobType jobType, String company, 
                     String location, String department, String experienceLevel, 
                     String skills, String salaryRange, String requirements, 
                     String benefits, String applicationEmail, String applicationUrl,
                     LocalDateTime applicationDeadline, User postedBy) {
        this.title = title;
        this.description = description;
        this.jobType = jobType;
        this.company = company;
        this.location = location;
        this.department = department;
        this.experienceLevel = experienceLevel;
        this.skills = skills;
        this.salaryRange = salaryRange;
        this.requirements = requirements;
        this.benefits = benefits;
        this.applicationEmail = applicationEmail;
        this.applicationUrl = applicationUrl;
        this.applicationDeadline = applicationDeadline;
        this.postedBy = postedBy;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public JobType getJobType() {
        return jobType;
    }
    
    public void setJobType(JobType jobType) {
        this.jobType = jobType;
    }
    
    public String getCompany() {
        return company;
    }
    
    public void setCompany(String company) {
        this.company = company;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getDepartment() {
        return department;
    }
    
    public void setDepartment(String department) {
        this.department = department;
    }
    
    public String getExperienceLevel() {
        return experienceLevel;
    }
    
    public void setExperienceLevel(String experienceLevel) {
        this.experienceLevel = experienceLevel;
    }
    
    public String getSkills() {
        return skills;
    }
    
    public void setSkills(String skills) {
        this.skills = skills;
    }
    
    public String getSalaryRange() {
        return salaryRange;
    }
    
    public void setSalaryRange(String salaryRange) {
        this.salaryRange = salaryRange;
    }
    
    public String getRequirements() {
        return requirements;
    }
    
    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }
    
    public String getBenefits() {
        return benefits;
    }
    
    public void setBenefits(String benefits) {
        this.benefits = benefits;
    }
    
    public String getApplicationEmail() {
        return applicationEmail;
    }
    
    public void setApplicationEmail(String applicationEmail) {
        this.applicationEmail = applicationEmail;
    }
    
    public String getApplicationUrl() {
        return applicationUrl;
    }
    
    public void setApplicationUrl(String applicationUrl) {
        this.applicationUrl = applicationUrl;
    }
    
    public JobStatus getStatus() {
        return status;
    }
    
    public void setStatus(JobStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public LocalDateTime getApplicationDeadline() {
        return applicationDeadline;
    }
    
    public void setApplicationDeadline(LocalDateTime applicationDeadline) {
        this.applicationDeadline = applicationDeadline;
    }
    
    public User getPostedBy() {
        return postedBy;
    }
    
    public void setPostedBy(User postedBy) {
        this.postedBy = postedBy;
    }
    
    public List<Application> getApplications() {
        return applications;
    }
    
    public void setApplications(List<Application> applications) {
        this.applications = applications;
    }
    
    // Enums
    public enum JobType {
        FULL_TIME("Full Time"),
        PART_TIME("Part Time"),
        INTERNSHIP("Internship"),
        CONTRACT("Contract");
        
        private final String displayName;
        
        JobType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum JobStatus {
        PENDING("Pending"),
        APPROVED("Approved"),
        REJECTED("Rejected");
        
        private final String displayName;
        
        JobStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}
