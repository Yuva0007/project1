package com.example.demo.service;

import com.example.demo.model.Application;
import com.example.demo.model.JobPosting;
import com.example.demo.model.User;

import java.util.List;
import java.util.Optional;

public interface ApplicationService {
    
    // Create application
    Application createApplication(Application application, User applicant);
    
    // Update application
    Application updateApplication(Long id, Application applicationDetails, User currentUser);
    
    // Delete application
    void deleteApplication(Long id, User currentUser);
    
    // Get application by ID
    Optional<Application> getApplicationById(Long id);
    
    // Get applications by job posting
    List<Application> getApplicationsByJobPosting(JobPosting jobPosting);
    
    // Get applications by applicant
    List<Application> getApplicationsByApplicant(User applicant);
    
    // Get application by job posting and applicant
    Optional<Application> getApplicationByJobPostingAndApplicant(JobPosting jobPosting, User applicant);
    
    // Get applications by status
    List<Application> getApplicationsByStatus(Application.ApplicationStatus status);
    
    // Get applications by job posting and status
    List<Application> getApplicationsByJobPostingAndStatus(JobPosting jobPosting, Application.ApplicationStatus status);
    
    // Get applications by applicant and status
    List<Application> getApplicationsByApplicantAndStatus(User applicant, Application.ApplicationStatus status);
    
    // Update application status (for job poster or admin)
    Application updateApplicationStatus(Long id, Application.ApplicationStatus status, String reviewNotes, User currentUser);
    
    // Check if user has applied to job posting
    boolean hasUserAppliedToJobPosting(JobPosting jobPosting, User applicant);
    
    // Get application statistics
    long getApplicationCountByJobPosting(JobPosting jobPosting);
    
    long getApplicationCountByApplicant(User applicant);
    
    long getApplicationCountByStatus(Application.ApplicationStatus status);
    
    // Get applications with job posting details
    List<Application> getApplicationsWithJobPosting(User applicant);
    
    // Get applications with applicant details
    List<Application> getApplicationsWithApplicant(JobPosting jobPosting);
    
    // Get recent applications
    List<Application> getRecentApplications(int days);
    
    // Check if user can edit application
    boolean canUserEditApplication(Long applicationId, User user);
    
    // Check if user can view application
    boolean canUserViewApplication(Long applicationId, User user);
}
