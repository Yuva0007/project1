package com.example.demo.controller;

import com.example.demo.model.AlumniProfile;
import com.example.demo.service.AlumniProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alumni-profiles")
@CrossOrigin(origins = "*")
public class AlumniProfileController {

    private final AlumniProfileService alumniProfileService;

    public AlumniProfileController(AlumniProfileService alumniProfileService) {
        this.alumniProfileService = alumniProfileService;
    }

    // Create alumni profile
    @PostMapping
    public ResponseEntity<AlumniProfile> createProfile(@RequestBody AlumniProfile profile) {
        // Ensure bidirectional consistency
        if (profile.getUser() != null) {
            profile.getUser().setAlumniProfile(profile);
        }

        AlumniProfile savedProfile = alumniProfileService.createAlumniProfile(profile);
        return ResponseEntity.ok(savedProfile);
    }

    // Get all profiles
    @GetMapping
    public ResponseEntity<List<AlumniProfile>> getAllProfiles() {
        return ResponseEntity.ok(alumniProfileService.getAllProfiles());
    }

    // Get single profile
    @GetMapping("/{id}")
    public ResponseEntity<AlumniProfile> getProfileById(@PathVariable Long id) {
        return alumniProfileService.getProfileById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Update profile
    @PutMapping("/{id}")
    public ResponseEntity<AlumniProfile> updateProfile(@PathVariable Long id, @RequestBody AlumniProfile profile) {
        return alumniProfileService.updateProfile(id, profile)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Delete profile
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProfile(@PathVariable Long id) {
        return alumniProfileService.deleteProfile(id) ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}
