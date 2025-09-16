package com.example.demo.service;

import com.example.demo.model.JobPosting;
import com.example.demo.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface JobPostingService {
    
    // Create job posting
    JobPosting createJobPosting(JobPosting jobPosting, User postedBy);
    
    // Update job posting
    JobPosting updateJobPosting(Long id, JobPosting jobPostingDetails, User currentUser);
    
    // Delete job posting (soft delete by setting status to rejected)
    void deleteJobPosting(Long id, User currentUser);
    
    // Get job posting by ID
    Optional<JobPosting> getJobPostingById(Long id);
    
    // Get all approved job postings
    List<JobPosting> getAllApprovedJobPostings();
    
    // Get job postings by type
    List<JobPosting> getJobPostingsByType(JobPosting.JobType jobType);
    
    // Get job postings by company
    List<JobPosting> getJobPostingsByCompany(String company);
    
    // Get job postings by location
    List<JobPosting> getJobPostingsByLocation(String location);
    
    // Get job postings by posted by user
    List<JobPosting> getJobPostingsByUser(User user);
    
    // Get all job postings (for admin)
    List<JobPosting> getAllJobPostings();
    
    // Get job postings by status (for admin)
    List<JobPosting> getJobPostingsByStatus(JobPosting.JobStatus status);
    
    // Get upcoming job postings
    List<JobPosting> getUpcomingJobPostings();
    
    // Get expired job postings
    List<JobPosting> getExpiredJobPostings();
    
    // Search job postings
    List<JobPosting> searchJobPostings(String searchTerm);
    
    // Get job postings by skills
    List<JobPosting> getJobPostingsBySkills(String skill);
    
    // Approve job posting (admin only)
    JobPosting approveJobPosting(Long id);
    
    // Reject job posting (admin only)
    JobPosting rejectJobPosting(Long id, String reason);
    
    // Get job posting statistics
    long getJobPostingCountByStatus(JobPosting.JobStatus status);
    
    long getJobPostingCountByUser(User user);
    
    // Check if user can edit job posting
    boolean canUserEditJobPosting(Long jobPostingId, User user);
    
    // Get job posting with applications
    Optional<JobPosting> getJobPostingWithApplications(Long id);
}
