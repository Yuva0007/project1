package com.example.demo.service;

import com.example.demo.model.JobPosting;
import com.example.demo.model.User;
import com.example.demo.repository.JobPostingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class JobPostingServiceImpl implements JobPostingService {
    
    @Autowired
    private JobPostingRepository jobPostingRepository;
    
    @Override
    public JobPosting createJobPosting(JobPosting jobPosting, User postedBy) {
        jobPosting.setPostedBy(postedBy);
        jobPosting.setStatus(JobPosting.JobStatus.PENDING);
        jobPosting.setCreatedAt(LocalDateTime.now());
        jobPosting.setUpdatedAt(LocalDateTime.now());
        return jobPostingRepository.save(jobPosting);
    }
    
    @Override
    public JobPosting updateJobPosting(Long id, JobPosting jobPostingDetails, User currentUser) {
        JobPosting jobPosting = jobPostingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Job posting not found with id: " + id));
        
        // Check if user can edit this job posting
        if (!canUserEditJobPosting(id, currentUser)) {
            throw new RuntimeException("You don't have permission to edit this job posting");
        }
        
        // Update fields
        if (jobPostingDetails.getTitle() != null) {
            jobPosting.setTitle(jobPostingDetails.getTitle());
        }
        if (jobPostingDetails.getDescription() != null) {
            jobPosting.setDescription(jobPostingDetails.getDescription());
        }
        if (jobPostingDetails.getJobType() != null) {
            jobPosting.setJobType(jobPostingDetails.getJobType());
        }
        if (jobPostingDetails.getCompany() != null) {
            jobPosting.setCompany(jobPostingDetails.getCompany());
        }
        if (jobPostingDetails.getLocation() != null) {
            jobPosting.setLocation(jobPostingDetails.getLocation());
        }
        if (jobPostingDetails.getDepartment() != null) {
            jobPosting.setDepartment(jobPostingDetails.getDepartment());
        }
        if (jobPostingDetails.getExperienceLevel() != null) {
            jobPosting.setExperienceLevel(jobPostingDetails.getExperienceLevel());
        }
        if (jobPostingDetails.getSkills() != null) {
            jobPosting.setSkills(jobPostingDetails.getSkills());
        }
        if (jobPostingDetails.getSalaryRange() != null) {
            jobPosting.setSalaryRange(jobPostingDetails.getSalaryRange());
        }
        if (jobPostingDetails.getRequirements() != null) {
            jobPosting.setRequirements(jobPostingDetails.getRequirements());
        }
        if (jobPostingDetails.getBenefits() != null) {
            jobPosting.setBenefits(jobPostingDetails.getBenefits());
        }
        if (jobPostingDetails.getApplicationEmail() != null) {
            jobPosting.setApplicationEmail(jobPostingDetails.getApplicationEmail());
        }
        if (jobPostingDetails.getApplicationUrl() != null) {
            jobPosting.setApplicationUrl(jobPostingDetails.getApplicationUrl());
        }
        if (jobPostingDetails.getApplicationDeadline() != null) {
            jobPosting.setApplicationDeadline(jobPostingDetails.getApplicationDeadline());
        }
        
        jobPosting.setUpdatedAt(LocalDateTime.now());
        return jobPostingRepository.save(jobPosting);
    }
    
    @Override
    public void deleteJobPosting(Long id, User currentUser) {
        JobPosting jobPosting = jobPostingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Job posting not found with id: " + id));
        
        // Check if user can delete this job posting
        if (!canUserEditJobPosting(id, currentUser)) {
            throw new RuntimeException("You don't have permission to delete this job posting");
        }
        
        // Soft delete by setting status to rejected
        jobPosting.setStatus(JobPosting.JobStatus.REJECTED);
        jobPosting.setUpdatedAt(LocalDateTime.now());
        jobPostingRepository.save(jobPosting);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<JobPosting> getJobPostingById(Long id) {
        return jobPostingRepository.findById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<JobPosting> getAllApprovedJobPostings() {
        return jobPostingRepository.findByStatusOrderByCreatedAtDesc(JobPosting.JobStatus.APPROVED);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<JobPosting> getJobPostingsByType(JobPosting.JobType jobType) {
        return jobPostingRepository.findByJobTypeAndStatusOrderByCreatedAtDesc(jobType, JobPosting.JobStatus.APPROVED);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<JobPosting> getJobPostingsByCompany(String company) {
        return jobPostingRepository.findByCompanyContainingIgnoreCaseAndStatusOrderByCreatedAtDesc(company, JobPosting.JobStatus.APPROVED);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<JobPosting> getJobPostingsByLocation(String location) {
        return jobPostingRepository.findByLocationContainingIgnoreCaseAndStatusOrderByCreatedAtDesc(location, JobPosting.JobStatus.APPROVED);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<JobPosting> getJobPostingsByUser(User user) {
        return jobPostingRepository.findByPostedByOrderByCreatedAtDesc(user);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<JobPosting> getAllJobPostings() {
        return jobPostingRepository.findAllByOrderByCreatedAtDesc();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<JobPosting> getJobPostingsByStatus(JobPosting.JobStatus status) {
        return jobPostingRepository.findByStatusOrderByCreatedAtDesc(status);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<JobPosting> getUpcomingJobPostings() {
        return jobPostingRepository.findUpcomingJobPostings(JobPosting.JobStatus.APPROVED, LocalDateTime.now());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<JobPosting> getExpiredJobPostings() {
        return jobPostingRepository.findExpiredJobPostings(JobPosting.JobStatus.APPROVED, LocalDateTime.now());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<JobPosting> searchJobPostings(String searchTerm) {
        return jobPostingRepository.searchJobPostings(JobPosting.JobStatus.APPROVED, searchTerm);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<JobPosting> getJobPostingsBySkills(String skill) {
        return jobPostingRepository.findBySkillsContaining(JobPosting.JobStatus.APPROVED, skill);
    }
    
    @Override
    public JobPosting approveJobPosting(Long id) {
        JobPosting jobPosting = jobPostingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Job posting not found with id: " + id));
        
        jobPosting.setStatus(JobPosting.JobStatus.APPROVED);
        jobPosting.setUpdatedAt(LocalDateTime.now());
        return jobPostingRepository.save(jobPosting);
    }
    
    @Override
    public JobPosting rejectJobPosting(Long id, String reason) {
        JobPosting jobPosting = jobPostingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Job posting not found with id: " + id));
        
        jobPosting.setStatus(JobPosting.JobStatus.REJECTED);
        jobPosting.setUpdatedAt(LocalDateTime.now());
        return jobPostingRepository.save(jobPosting);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long getJobPostingCountByStatus(JobPosting.JobStatus status) {
        return jobPostingRepository.countByStatus(status);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long getJobPostingCountByUser(User user) {
        return jobPostingRepository.countByPostedBy(user);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean canUserEditJobPosting(Long jobPostingId, User user) {
        JobPosting jobPosting = jobPostingRepository.findById(jobPostingId)
            .orElseThrow(() -> new RuntimeException("Job posting not found with id: " + jobPostingId));
        
        // Admin can edit any job posting
        if (user.getRole().equals("ADMIN")) {
            return true;
        }
        
        // User can edit their own job postings
        return jobPosting.getPostedBy().getId().equals(user.getId());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<JobPosting> getJobPostingWithApplications(Long id) {
        return jobPostingRepository.findByIdWithApplications(id);
    }
}
