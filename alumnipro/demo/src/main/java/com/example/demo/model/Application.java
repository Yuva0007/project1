package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "applications")
public class Application {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_posting_id", nullable = false)
    private JobPosting jobPosting;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false)
    private User applicant;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status = ApplicationStatus.APPLIED; // APPLIED, REVIEWED, ACCEPTED, REJECTED
    
    @Column(columnDefinition = "TEXT")
    private String coverLetter;
    
    @Column(length = 200)
    private String resumeUrl;
    
    @Column(length = 200)
    private String portfolioUrl;
    
    @Column(length = 500)
    private String additionalNotes;
    
    @Column(nullable = false)
    private LocalDateTime appliedAt;
    
    @Column
    private LocalDateTime reviewedAt;
    
    @Column(columnDefinition = "TEXT")
    private String reviewNotes;
    
    // Constructors
    public Application() {}
    
    public Application(JobPosting jobPosting, User applicant, String coverLetter, 
                      String resumeUrl, String portfolioUrl, String additionalNotes) {
        this.jobPosting = jobPosting;
        this.applicant = applicant;
        this.coverLetter = coverLetter;
        this.resumeUrl = resumeUrl;
        this.portfolioUrl = portfolioUrl;
        this.additionalNotes = additionalNotes;
        this.appliedAt = LocalDateTime.now();
    }
    
    @PrePersist
    protected void onCreate() {
        if (appliedAt == null) {
            appliedAt = LocalDateTime.now();
        }
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public JobPosting getJobPosting() {
        return jobPosting;
    }
    
    public void setJobPosting(JobPosting jobPosting) {
        this.jobPosting = jobPosting;
    }
    
    public User getApplicant() {
        return applicant;
    }
    
    public void setApplicant(User applicant) {
        this.applicant = applicant;
    }
    
    public ApplicationStatus getStatus() {
        return status;
    }
    
    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }
    
    public String getCoverLetter() {
        return coverLetter;
    }
    
    public void setCoverLetter(String coverLetter) {
        this.coverLetter = coverLetter;
    }
    
    public String getResumeUrl() {
        return resumeUrl;
    }
    
    public void setResumeUrl(String resumeUrl) {
        this.resumeUrl = resumeUrl;
    }
    
    public String getPortfolioUrl() {
        return portfolioUrl;
    }
    
    public void setPortfolioUrl(String portfolioUrl) {
        this.portfolioUrl = portfolioUrl;
    }
    
    public String getAdditionalNotes() {
        return additionalNotes;
    }
    
    public void setAdditionalNotes(String additionalNotes) {
        this.additionalNotes = additionalNotes;
    }
    
    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }
    
    public void setAppliedAt(LocalDateTime appliedAt) {
        this.appliedAt = appliedAt;
    }
    
    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }
    
    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }
    
    public String getReviewNotes() {
        return reviewNotes;
    }
    
    public void setReviewNotes(String reviewNotes) {
        this.reviewNotes = reviewNotes;
    }
    
    // Enums
    public enum ApplicationStatus {
        APPLIED("Applied"),
        REVIEWED("Reviewed"),
        ACCEPTED("Accepted"),
        REJECTED("Rejected");
        
        private final String displayName;
        
        ApplicationStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}
