package com.example.demo.service;

import com.example.demo.model.Application;
import com.example.demo.model.JobPosting;
import com.example.demo.model.User;
import com.example.demo.repository.ApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ApplicationServiceImpl implements ApplicationService {
    
    @Autowired
    private ApplicationRepository applicationRepository;
    
    @Override
    public Application createApplication(Application application, User applicant) {
        // Check if user has already applied to this job posting
        if (hasUserAppliedToJobPosting(application.getJobPosting(), applicant)) {
            throw new RuntimeException("You have already applied to this job posting");
        }
        
        application.setApplicant(applicant);
        application.setStatus(Application.ApplicationStatus.APPLIED);
        application.setAppliedAt(LocalDateTime.now());
        return applicationRepository.save(application);
    }
    
    @Override
    public Application updateApplication(Long id, Application applicationDetails, User currentUser) {
        Application application = applicationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Application not found with id: " + id));
        
        // Check if user can edit this application
        if (!canUserEditApplication(id, currentUser)) {
            throw new RuntimeException("You don't have permission to edit this application");
        }
        
        // Update fields
        if (applicationDetails.getCoverLetter() != null) {
            application.setCoverLetter(applicationDetails.getCoverLetter());
        }
        if (applicationDetails.getResumeUrl() != null) {
            application.setResumeUrl(applicationDetails.getResumeUrl());
        }
        if (applicationDetails.getPortfolioUrl() != null) {
            application.setPortfolioUrl(applicationDetails.getPortfolioUrl());
        }
        if (applicationDetails.getAdditionalNotes() != null) {
            application.setAdditionalNotes(applicationDetails.getAdditionalNotes());
        }
        
        return applicationRepository.save(application);
    }
    
    @Override
    public void deleteApplication(Long id, User currentUser) {
        Application application = applicationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Application not found with id: " + id));
        
        // Check if user can delete this application
        if (!canUserEditApplication(id, currentUser)) {
            throw new RuntimeException("You don't have permission to delete this application");
        }
        
        applicationRepository.delete(application);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Application> getApplicationById(Long id) {
        return applicationRepository.findById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Application> getApplicationsByJobPosting(JobPosting jobPosting) {
        return applicationRepository.findByJobPostingOrderByAppliedAtDesc(jobPosting);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Application> getApplicationsByApplicant(User applicant) {
        return applicationRepository.findByApplicantOrderByAppliedAtDesc(applicant);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Application> getApplicationByJobPostingAndApplicant(JobPosting jobPosting, User applicant) {
        return applicationRepository.findByJobPostingAndApplicant(jobPosting, applicant);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Application> getApplicationsByStatus(Application.ApplicationStatus status) {
        return applicationRepository.findByStatusOrderByAppliedAtDesc(status);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Application> getApplicationsByJobPostingAndStatus(JobPosting jobPosting, Application.ApplicationStatus status) {
        return applicationRepository.findByJobPostingAndStatusOrderByAppliedAtDesc(jobPosting, status);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Application> getApplicationsByApplicantAndStatus(User applicant, Application.ApplicationStatus status) {
        return applicationRepository.findByApplicantAndStatusOrderByAppliedAtDesc(applicant, status);
    }
    
    @Override
    public Application updateApplicationStatus(Long id, Application.ApplicationStatus status, String reviewNotes, User currentUser) {
        Application application = applicationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Application not found with id: " + id));
        
        // Check if user can update application status
        if (!canUserViewApplication(id, currentUser)) {
            throw new RuntimeException("You don't have permission to update this application status");
        }
        
        application.setStatus(status);
        application.setReviewNotes(reviewNotes);
        application.setReviewedAt(LocalDateTime.now());
        return applicationRepository.save(application);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean hasUserAppliedToJobPosting(JobPosting jobPosting, User applicant) {
        return applicationRepository.existsByJobPostingAndApplicant(jobPosting, applicant);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long getApplicationCountByJobPosting(JobPosting jobPosting) {
        return applicationRepository.countByJobPosting(jobPosting);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long getApplicationCountByApplicant(User applicant) {
        return applicationRepository.countByApplicant(applicant);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long getApplicationCountByStatus(Application.ApplicationStatus status) {
        return applicationRepository.countByStatus(status);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Application> getApplicationsWithJobPosting(User applicant) {
        return applicationRepository.findByApplicantWithJobPosting(applicant);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Application> getApplicationsWithApplicant(JobPosting jobPosting) {
        return applicationRepository.findByJobPostingWithApplicant(jobPosting);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Application> getRecentApplications(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return applicationRepository.findRecentApplications(since);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean canUserEditApplication(Long applicationId, User user) {
        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new RuntimeException("Application not found with id: " + applicationId));
        
        // Admin can edit any application
        if (user.getRole().equals("ADMIN")) {
            return true;
        }
        
        // Applicant can edit their own application (only if status is APPLIED)
        if (application.getApplicant().getId().equals(user.getId()) && 
            application.getStatus() == Application.ApplicationStatus.APPLIED) {
            return true;
        }
        
        return false;
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean canUserViewApplication(Long applicationId, User user) {
        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new RuntimeException("Application not found with id: " + applicationId));
        
        // Admin can view any application
        if (user.getRole().equals("ADMIN")) {
            return true;
        }
        
        // Applicant can view their own application
        if (application.getApplicant().getId().equals(user.getId())) {
            return true;
        }
        
        // Job poster can view applications for their job postings
        if (application.getJobPosting().getPostedBy().getId().equals(user.getId())) {
            return true;
        }
        
        return false;
    }
}
