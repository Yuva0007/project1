package com.example.demo.repository;

import com.example.demo.model.Application;
import com.example.demo.model.JobPosting;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    
    // Find applications by job posting
    List<Application> findByJobPostingOrderByAppliedAtDesc(JobPosting jobPosting);
    
    // Find applications by applicant
    List<Application> findByApplicantOrderByAppliedAtDesc(User applicant);
    
    // Find application by job posting and applicant (to check if already applied)
    Optional<Application> findByJobPostingAndApplicant(JobPosting jobPosting, User applicant);
    
    // Find applications by status
    List<Application> findByStatusOrderByAppliedAtDesc(Application.ApplicationStatus status);
    
    // Find applications by job posting and status
    List<Application> findByJobPostingAndStatusOrderByAppliedAtDesc(
        JobPosting jobPosting, Application.ApplicationStatus status);
    
    // Find applications by applicant and status
    List<Application> findByApplicantAndStatusOrderByAppliedAtDesc(
        User applicant, Application.ApplicationStatus status);
    
    // Count applications by job posting
    long countByJobPosting(JobPosting jobPosting);
    
    // Count applications by applicant
    long countByApplicant(User applicant);
    
    // Count applications by status
    long countByStatus(Application.ApplicationStatus status);
    
    // Find applications with job posting details
    @Query("SELECT a FROM Application a JOIN FETCH a.jobPosting WHERE a.applicant = :applicant ORDER BY a.appliedAt DESC")
    List<Application> findByApplicantWithJobPosting(@Param("applicant") User applicant);
    
    // Find applications with applicant details
    @Query("SELECT a FROM Application a JOIN FETCH a.applicant WHERE a.jobPosting = :jobPosting ORDER BY a.appliedAt DESC")
    List<Application> findByJobPostingWithApplicant(@Param("jobPosting") JobPosting jobPosting);
    
    // Find recent applications (last 30 days)
    @Query("SELECT a FROM Application a WHERE a.appliedAt >= :since ORDER BY a.appliedAt DESC")
    List<Application> findRecentApplications(@Param("since") java.time.LocalDateTime since);
    
    // Check if user has applied to job posting
    boolean existsByJobPostingAndApplicant(JobPosting jobPosting, User applicant);
    
    // Find applications by job posting ID
    @Query("SELECT a FROM Application a JOIN FETCH a.applicant WHERE a.jobPosting.id = :jobPostingId ORDER BY a.appliedAt DESC")
    List<Application> findByJobPostingIdWithApplicant(@Param("jobPostingId") Long jobPostingId);
    
    // Find applications by applicant ID
    @Query("SELECT a FROM Application a JOIN FETCH a.jobPosting WHERE a.applicant.id = :applicantId ORDER BY a.appliedAt DESC")
    List<Application> findByApplicantIdWithJobPosting(@Param("applicantId") Long applicantId);
}
