package com.example.demo.controller;

import com.example.demo.model.StudentProfile;
import com.example.demo.service.StudentProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    @Autowired
    private StudentProfileService studentProfileService;

    @GetMapping("/dashboard")
    public String studentDashboard() {
        return "Welcome to Student Dashboard!";
    }

    @GetMapping("/profiles")
    public List<StudentProfile> getAllProfiles() {
        return studentProfileService.getAllProfiles();
    }

    @GetMapping("/profiles/{id}")
    public ResponseEntity<StudentProfile> getProfileById(@PathVariable Long id) {
        return studentProfileService.getProfileById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/profiles")
    public StudentProfile createProfile(@RequestBody StudentProfile profile) {
        return studentProfileService.createProfile(profile);
    }

    @PutMapping("/profiles/{id}")
    public ResponseEntity<StudentProfile> updateProfile(@PathVariable Long id, @RequestBody StudentProfile updatedProfile) {
        StudentProfile updated = studentProfileService.updateProfile(id, updatedProfile);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/profiles/{id}")
    public ResponseEntity<Void> deleteProfile(@PathVariable Long id) {
        studentProfileService.deleteProfile(id);
        return ResponseEntity.noContent().build();
    }
}
