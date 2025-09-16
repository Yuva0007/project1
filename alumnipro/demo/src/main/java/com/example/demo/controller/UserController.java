package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.service.UserService;
import com.example.demo.service.AuditLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;
    private final AuditLogService auditLogService;

    public UserController(UserService userService, AuditLogService auditLogService) {
        this.userService = userService;
        this.auditLogService = auditLogService;
    }

    // Create user
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        if (user.getAlumniProfile() != null) {
            user.getAlumniProfile().setUser(user);
        }

        User createdUser = userService.createUser(user);
        auditLogService.logAction("CREATE", "USER", createdUser.getId(), null, createdUser.getEmail(), "SYSTEM");
        return ResponseEntity.ok(createdUser);
    }

    // Get all users (excluding deleted ones)
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // Get user by ID
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Update user
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        User oldUser = userService.getUserById(id).orElse(null);
        if (oldUser == null) {
            return ResponseEntity.notFound().build();
        }
        
        User updatedUser = userService.updateUser(id, user).orElse(null);
        if (updatedUser != null) {
            auditLogService.logAction("UPDATE", "USER", id, oldUser.getEmail(), updatedUser.getEmail(), "ADMIN");
        }
        
        return updatedUser != null ? ResponseEntity.ok(updatedUser) : ResponseEntity.notFound().build();
    }

    // Soft delete user
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        User user = userService.getUserById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        user.setAccountStatus(User.AccountStatus.DELETED);
        userService.updateUser(id, user);
        auditLogService.logAction("DELETE", "USER", id, user.getEmail(), null, "ADMIN");
        
        return ResponseEntity.ok().build();
    }

    // Deactivate user account
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<User> deactivateUser(@PathVariable Long id) {
        User user = userService.getUserById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        user.setAccountStatus(User.AccountStatus.INACTIVE);
        User updatedUser = userService.updateUser(id, user).orElse(null);
        if (updatedUser != null) {
            auditLogService.logAction("DEACTIVATE", "USER", id, "ACTIVE", "INACTIVE", "ADMIN");
        }
        
        return updatedUser != null ? ResponseEntity.ok(updatedUser) : ResponseEntity.notFound().build();
    }

    // Activate user account
    @PutMapping("/{id}/activate")
    public ResponseEntity<User> activateUser(@PathVariable Long id) {
        User user = userService.getUserById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        user.setAccountStatus(User.AccountStatus.ACTIVE);
        User updatedUser = userService.updateUser(id, user).orElse(null);
        if (updatedUser != null) {
            auditLogService.logAction("ACTIVATE", "USER", id, "INACTIVE", "ACTIVE", "ADMIN");
        }
        
        return updatedUser != null ? ResponseEntity.ok(updatedUser) : ResponseEntity.notFound().build();
    }

    // Update profile visibility
    @PutMapping("/{id}/visibility")
    public ResponseEntity<User> updateProfileVisibility(@PathVariable Long id, @RequestParam String visibility) {
        User user = userService.getUserById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        User.ProfileVisibility oldVisibility = user.getProfileVisibility();
        user.setProfileVisibility(User.ProfileVisibility.valueOf(visibility.toUpperCase()));
        User updatedUser = userService.updateUser(id, user).orElse(null);
        
        if (updatedUser != null) {
            auditLogService.logAction("UPDATE_VISIBILITY", "USER", id, oldVisibility.toString(), visibility, "ADMIN");
        }
        
        return updatedUser != null ? ResponseEntity.ok(updatedUser) : ResponseEntity.notFound().build();
    }

    // Get user by email
    @GetMapping("/email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Search users by name or email
    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsers(@RequestParam String query) {
        List<User> users = userService.searchUsers(query);
        return ResponseEntity.ok(users);
    }

    // Get all active users (excluding deleted/inactive/suspended)
    @GetMapping("/active")
    public ResponseEntity<List<User>> getAllActiveUsers() {
        return ResponseEntity.ok(userService.getAllActiveUsers());
    }

    // Partial update: contact and alumni fields
    @PutMapping("/{id}/profile")
    public ResponseEntity<User> updateContactAndAlumni(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return userService.getUserById(id).map(existing -> {
            if (body.containsKey("phone")) {
                Object v = body.get("phone");
                existing.setPhone(v != null ? v.toString() : null);
            }
            // Alumni fields
            if ("ALUMNI".equalsIgnoreCase(existing.getRole() != null ? existing.getRole().name() : null)) {
                if (existing.getAlumniProfile() == null) {
                    existing.setAlumniProfile(new com.example.demo.model.AlumniProfile());
                }
                var ap = existing.getAlumniProfile();
                if (body.containsKey("graduationYear")) { Object v = body.get("graduationYear"); ap.setGraduationYear(v != null ? v.toString() : null); }
                if (body.containsKey("currentPosition")) { Object v = body.get("currentPosition"); ap.setCurrentPosition(v != null ? v.toString() : null); }
                if (body.containsKey("company")) { Object v = body.get("company"); ap.setCompany(v != null ? v.toString() : null); }
                if (body.containsKey("industry")) { Object v = body.get("industry"); ap.setIndustry(v != null ? v.toString() : null); }
                if (body.containsKey("location")) { Object v = body.get("location"); ap.setLocation(v != null ? v.toString() : null); }
                if (body.containsKey("skills")) { Object v = body.get("skills"); ap.setSkills(v != null ? v.toString() : null); }
                if (body.containsKey("bio")) { Object v = body.get("bio"); ap.setBio(v != null ? v.toString() : null); }
            }
            // Student fields
            if ("STUDENT".equalsIgnoreCase(existing.getRole() != null ? existing.getRole().name() : null)) {
                if (existing.getStudentProfile() == null) {
                    existing.setStudentProfile(new com.example.demo.model.StudentProfile());
                }
                var sp = existing.getStudentProfile();
                if (body.containsKey("department")) { Object v = body.get("department"); sp.setDepartment(v != null ? v.toString() : null); }
                if (body.containsKey("branch")) { Object v = body.get("branch"); sp.setDepartment(v != null ? v.toString() : null); }
                if (body.containsKey("yearOfStudy")) {
                    Object v = body.get("yearOfStudy");
                    try {
                        int yos = v != null ? Integer.parseInt(v.toString()) : 0;
                        sp.setYearOfStudy(yos);
                    } catch (NumberFormatException ignored) { }
                }
                if (body.containsKey("registrationNumber")) { Object v = body.get("registrationNumber"); sp.setRegistrationNumber(v != null ? v.toString() : null); }
            }
            return userService.updateUser(id, existing)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        }).orElse(ResponseEntity.notFound().build());
    }
}
