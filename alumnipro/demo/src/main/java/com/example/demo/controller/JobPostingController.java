package com.example.demo.controller;

import com.example.demo.model.JobPosting;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtil;
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
@RequestMapping("/api/job-postings")
@CrossOrigin(origins = "http://localhost:3000")
public class JobPostingController {
    
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
    
    // Create job posting
    @PostMapping
    public ResponseEntity<?> createJobPosting(@RequestBody JobPosting jobPosting, @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            User postedBy = getUserFromToken(token);
            
            // Only alumni can post jobs
            if (!postedBy.getRole().equals("ALUMNI")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only alumni can post job openings"));
            }
            
            JobPosting createdJobPosting = jobPostingService.createJobPosting(jobPosting, postedBy);
            return ResponseEntity.ok(createdJobPosting);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    // Update job posting
    @PutMapping("/{id}")
    public ResponseEntity<?> updateJobPosting(@PathVariable Long id, @RequestBody JobPosting jobPostingDetails, @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            User currentUser = getUserFromToken(token);
            
            JobPosting updatedJobPosting = jobPostingService.updateJobPosting(id, jobPostingDetails, currentUser);
            return ResponseEntity.ok(updatedJobPosting);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    // Delete job posting
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteJobPosting(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            User currentUser = getUserFromToken(token);
            
            jobPostingService.deleteJobPosting(id, currentUser);
            return ResponseEntity.ok(Map.of("message", "Job posting deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    // Get job posting by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getJobPostingById(@PathVariable Long id) {
        try {
            Optional<JobPosting> jobPosting = jobPostingService.getJobPostingById(id);
            if (jobPosting.isPresent()) {
                return ResponseEntity.ok(jobPosting.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    // Get all approved job postings
    @GetMapping
    public ResponseEntity<?> getAllApprovedJobPostings() {
        try {
            List<JobPosting> jobPostings = jobPostingService.getAllApprovedJobPostings();
            return ResponseEntity.ok(jobPostings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    // Get job postings by type
    @GetMapping("/type/{jobType}")
    public ResponseEntity<?> getJobPostingsByType(@PathVariable String jobType) {
        try {
            JobPosting.JobType type = JobPosting.JobType.valueOf(jobType.toUpperCase());
            List<JobPosting> jobPostings = jobPostingService.getJobPostingsByType(type);
            return ResponseEntity.ok(jobPostings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    // Get job postings by company
    @GetMapping("/company/{company}")
    public ResponseEntity<?> getJobPostingsByCompany(@PathVariable String company) {
        try {
            List<JobPosting> jobPostings = jobPostingService.getJobPostingsByCompany(company);
            return ResponseEntity.ok(jobPostings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    // Get job postings by location
    @GetMapping("/location/{location}")
    public ResponseEntity<?> getJobPostingsByLocation(@PathVariable String location) {
        try {
            List<JobPosting> jobPostings = jobPostingService.getJobPostingsByLocation(location);
            return ResponseEntity.ok(jobPostings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    // Get job postings by user
    @GetMapping("/my-postings")
    public ResponseEntity<?> getMyJobPostings(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            User user = getUserFromToken(token);
            
            List<JobPosting> jobPostings = jobPostingService.getJobPostingsByUser(user);
            return ResponseEntity.ok(jobPostings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    // Get all job postings (admin only)
    @GetMapping("/admin/all")
    public ResponseEntity<?> getAllJobPostings(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            User user = getUserFromToken(token);
            
            if (!user.getRole().equals("ADMIN")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Admin access required"));
            }
            
            List<JobPosting> jobPostings = jobPostingService.getAllJobPostings();
            return ResponseEntity.ok(jobPostings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    // Get job postings by status (admin only)
    @GetMapping("/admin/status/{status}")
    public ResponseEntity<?> getJobPostingsByStatus(@PathVariable String status, @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            User user = getUserFromToken(token);
            
            if (!user.getRole().equals("ADMIN")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Admin access required"));
            }
            
            JobPosting.JobStatus jobStatus = JobPosting.JobStatus.valueOf(status.toUpperCase());
            List<JobPosting> jobPostings = jobPostingService.getJobPostingsByStatus(jobStatus);
            return ResponseEntity.ok(jobPostings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    // Search job postings
    @GetMapping("/search")
    public ResponseEntity<?> searchJobPostings(@RequestParam String q) {
        try {
            List<JobPosting> jobPostings = jobPostingService.searchJobPostings(q);
            return ResponseEntity.ok(jobPostings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    // Get job postings by skills
    @GetMapping("/skills/{skill}")
    public ResponseEntity<?> getJobPostingsBySkills(@PathVariable String skill) {
        try {
            List<JobPosting> jobPostings = jobPostingService.getJobPostingsBySkills(skill);
            return ResponseEntity.ok(jobPostings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    // Approve job posting (admin only)
    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveJobPosting(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            User user = getUserFromToken(token);
            
            if (!user.getRole().equals("ADMIN")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Admin access required"));
            }
            
            JobPosting approvedJobPosting = jobPostingService.approveJobPosting(id);
            return ResponseEntity.ok(approvedJobPosting);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    // Reject job posting (admin only)
    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectJobPosting(@PathVariable Long id, @RequestBody Map<String, String> requestBody, @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            User user = getUserFromToken(token);
            
            if (!user.getRole().equals("ADMIN")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Admin access required"));
            }
            
            String reason = requestBody.getOrDefault("reason", "");
            JobPosting rejectedJobPosting = jobPostingService.rejectJobPosting(id, reason);
            return ResponseEntity.ok(rejectedJobPosting);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    // Get job posting statistics
    @GetMapping("/stats")
    public ResponseEntity<?> getJobPostingStats(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            User user = getUserFromToken(token);
            
            Map<String, Object> stats = new HashMap<>();
            
            if (user.getRole().equals("ADMIN")) {
                stats.put("total", jobPostingService.getJobPostingCountByStatus(JobPosting.JobStatus.APPROVED) +
                           jobPostingService.getJobPostingCountByStatus(JobPosting.JobStatus.PENDING) +
                           jobPostingService.getJobPostingCountByStatus(JobPosting.JobStatus.REJECTED));
                stats.put("approved", jobPostingService.getJobPostingCountByStatus(JobPosting.JobStatus.APPROVED));
                stats.put("pending", jobPostingService.getJobPostingCountByStatus(JobPosting.JobStatus.PENDING));
                stats.put("rejected", jobPostingService.getJobPostingCountByStatus(JobPosting.JobStatus.REJECTED));
            } else {
                stats.put("myPostings", jobPostingService.getJobPostingCountByUser(user));
            }
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
}
