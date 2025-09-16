package com.example.demo.repository;

import com.example.demo.model.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {
    
    // Find approved job postings
    List<JobPosting> findByStatusOrderByCreatedAtDesc(JobPosting.JobStatus status);
    
    // Find job postings by type and status
    List<JobPosting> findByJobTypeAndStatusOrderByCreatedAtDesc(
        JobPosting.JobType jobType, JobPosting.JobStatus status);
    
    // Find job postings by company
    List<JobPosting> findByCompanyContainingIgnoreCaseAndStatusOrderByCreatedAtDesc(
        String company, JobPosting.JobStatus status);
    
    // Find job postings by location
    List<JobPosting> findByLocationContainingIgnoreCaseAndStatusOrderByCreatedAtDesc(
        String location, JobPosting.JobStatus status);
    
    // Find job postings by posted by user
    List<JobPosting> findByPostedByOrderByCreatedAtDesc(com.example.demo.model.User postedBy);
    
    // Find all job postings (for admin)
    List<JobPosting> findAllByOrderByCreatedAtDesc();
    
    // Find upcoming job postings (not expired)
    @Query("SELECT j FROM JobPosting j WHERE j.status = :status AND j.applicationDeadline > :now ORDER BY j.applicationDeadline ASC")
    List<JobPosting> findUpcomingJobPostings(@Param("status") JobPosting.JobStatus status, @Param("now") LocalDateTime now);
    
    // Find expired job postings
    @Query("SELECT j FROM JobPosting j WHERE j.status = :status AND j.applicationDeadline <= :now ORDER BY j.applicationDeadline DESC")
    List<JobPosting> findExpiredJobPostings(@Param("status") JobPosting.JobStatus status, @Param("now") LocalDateTime now);
    
    // Search job postings by title, company, or location
    @Query("SELECT j FROM JobPosting j WHERE j.status = :status AND " +
           "(LOWER(j.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(j.company) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(j.location) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY j.createdAt DESC")
    List<JobPosting> searchJobPostings(@Param("status") JobPosting.JobStatus status, @Param("searchTerm") String searchTerm);
    
    // Find job postings by skills
    @Query("SELECT j FROM JobPosting j WHERE j.status = :status AND " +
           "LOWER(j.skills) LIKE LOWER(CONCAT('%', :skill, '%')) " +
           "ORDER BY j.createdAt DESC")
    List<JobPosting> findBySkillsContaining(@Param("status") JobPosting.JobStatus status, @Param("skill") String skill);
    
    // Count job postings by status
    long countByStatus(JobPosting.JobStatus status);
    
    // Count job postings by posted by user
    long countByPostedBy(com.example.demo.model.User postedBy);
    
    // Find job posting with applications count
    @Query("SELECT j FROM JobPosting j LEFT JOIN FETCH j.applications WHERE j.id = :id")
    Optional<JobPosting> findByIdWithApplications(@Param("id") Long id);
}
