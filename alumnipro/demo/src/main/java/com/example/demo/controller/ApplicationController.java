package com.example.demo.controller;

import com.example.demo.model.Application;
import com.example.demo.model.JobPosting;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.ApplicationService;
import com.example.demo.service.JobPostingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin(origins = "http://localhost:3000")
public class ApplicationController {
    
    @Autowired
    private ApplicationService applicationService;
    
    @Autowired
    private JobPostingService jobPostingService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    // Helper method to extract user from token
    private User getUserFromToken(String token) {
        String email = null;
        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7);
            email = jwtUtil.extractEmail(jwt);
            if (email == null || email.isBlank()) {
                email = jwtUtil.extractUsername(jwt);
            }
        }
        if (email == null) {
            throw new RuntimeException("Authentication required");
        }
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    // Create application
    @PostMapping
    public ResponseEntity<?> createApplication(@RequestBody Application application, @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            User applicant = getUserFromToken(token);
            
            // Only students can apply for jobs
            if (!applicant.getRole().equals("STUDENT")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only students can apply for jobs"));
            }
            
            Application createdApplication = applicationService.createApplication(application, applicant);
            return ResponseEntity.ok(createdApplication);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    // Update application
    @PutMapping("/{id}")
    public ResponseEntity<?> updateApplication(@PathVariable Long id, @RequestBody Application applicationDetails, @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            User currentUser = getUserFromToken(token);
            
            Application updatedApplication = applicationService.updateApplication(id, applicationDetails, currentUser);
            return ResponseEntity.ok(updatedApplication);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    // Delete application
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteApplication(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            User currentUser = getUserFromToken(token);
            
            applicationService.deleteApplication(id, currentUser);
            return ResponseEntity.ok(Map.of("message", "Application deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    // Get application by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getApplicationById(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            User currentUser = getUserFromToken(token);
            
            Optional<Application> application = applicationService.getApplicationById(id);
            if (application.isPresent()) {
                // Check if user can view this application
                if (applicationService.canUserViewApplication(id, currentUser)) {
                    return ResponseEntity.ok(application.get());
                } else {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "You don't have permission to view this application"));
                }
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    // Get applications by job posting
    @GetMapping("/job-posting/{jobPostingId}")
    public ResponseEntity<?> getApplicationsByJobPosting(@PathVariable Long jobPostingId, @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            User currentUser = getUserFromToken(token);
            
            Optional<JobPosting> jobPosting = jobPostingService.getJobPostingById(jobPostingId);
            if (jobPosting.isPresent()) {
                // Check if user can view applications for this job posting
                if (currentUser.getRole().equals("ADMIN") || 
                    jobPosting.get().getPostedBy().getId().equals(currentUser.getId())) {
                    List<Application> applications = applicationService.getApplicationsWithApplicant(jobPosting.get());
                    return ResponseEntity.ok(applications);
                } else {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "You don't have permission to view applications for this job posting"));
                }
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    // Get my applications
    @GetMapping("/my-applications")
    public ResponseEntity<?> getMyApplications(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            User applicant = getUserFromToken(token);
            
            List<Application> applications = applicationService.getApplicationsWithJobPosting(applicant);
            return ResponseEntity.ok(applications);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    // Get applications by status
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getApplicationsByStatus(@PathVariable String status, @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            User currentUser = getUserFromToken(token);
            
            // Only admin can view applications by status
            if (!currentUser.getRole().equals("ADMIN")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Admin access required"));
            }
            
            Application.ApplicationStatus applicationStatus = Application.ApplicationStatus.valueOf(status.toUpperCase());
            List<Application> applications = applicationService.getApplicationsByStatus(applicationStatus);
            return ResponseEntity.ok(applications);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    // Update application status
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateApplicationStatus(@PathVariable Long id, @RequestBody Map<String, String> requestBody, @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            User currentUser = getUserFromToken(token);
            
            String statusStr = requestBody.get("status");
            String reviewNotes = requestBody.getOrDefault("reviewNotes", "");
            
            Application.ApplicationStatus status = Application.ApplicationStatus.valueOf(statusStr.toUpperCase());
            Application updatedApplication = applicationService.updateApplicationStatus(id, status, reviewNotes, currentUser);
            return ResponseEntity.ok(updatedApplication);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    // Check if user has applied to job posting
    @GetMapping("/check/{jobPostingId}")
    public ResponseEntity<?> checkApplication(@PathVariable Long jobPostingId, @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            User applicant = getUserFromToken(token);
            
            Optional<JobPosting> jobPosting = jobPostingService.getJobPostingById(jobPostingId);
            if (jobPosting.isPresent()) {
                boolean hasApplied = applicationService.hasUserAppliedToJobPosting(jobPosting.get(), applicant);
                return ResponseEntity.ok(Map.of("hasApplied", hasApplied));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    // Get application statistics
    @GetMapping("/stats")
    public ResponseEntity<?> getApplicationStats(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            User user = getUserFromToken(token);
            
            Map<String, Object> stats = new HashMap<>();
            
            if (user.getRole().equals("ADMIN")) {
                stats.put("total", applicationService.getApplicationCountByStatus(Application.ApplicationStatus.APPLIED) +
                           applicationService.getApplicationCountByStatus(Application.ApplicationStatus.REVIEWED) +
                           applicationService.getApplicationCountByStatus(Application.ApplicationStatus.ACCEPTED) +
                           applicationService.getApplicationCountByStatus(Application.ApplicationStatus.REJECTED));
                stats.put("applied", applicationService.getApplicationCountByStatus(Application.ApplicationStatus.APPLIED));
                stats.put("reviewed", applicationService.getApplicationCountByStatus(Application.ApplicationStatus.REVIEWED));
                stats.put("accepted", applicationService.getApplicationCountByStatus(Application.ApplicationStatus.ACCEPTED));
                stats.put("rejected", applicationService.getApplicationCountByStatus(Application.ApplicationStatus.REJECTED));
            } else if (user.getRole().equals("STUDENT")) {
                stats.put("myApplications", applicationService.getApplicationCountByApplicant(user));
            } else if (user.getRole().equals("ALUMNI")) {
                // Get applications for alumni's job postings
                List<JobPosting> myJobPostings = jobPostingService.getJobPostingsByUser(user);
                long totalApplications = 0;
                for (JobPosting jobPosting : myJobPostings) {
                    totalApplications += applicationService.getApplicationCountByJobPosting(jobPosting);
                }
                stats.put("applicationsToMyJobs", totalApplications);
            }
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    // Get recent applications (admin only)
    @GetMapping("/recent")
    public ResponseEntity<?> getRecentApplications(@RequestParam(defaultValue = "30") int days, @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            User user = getUserFromToken(token);
            
            if (!user.getRole().equals("ADMIN")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Admin access required"));
            }
            
            List<Application> applications = applicationService.getRecentApplications(days);
            return ResponseEntity.ok(applications);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
}
