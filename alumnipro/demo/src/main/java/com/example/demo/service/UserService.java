package com.example.demo.service;

import com.example.demo.model.AlumniProfile;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("User with this email already exists.");
        }

        if (user.getAlumniProfile() != null) {
            user.getAlumniProfile().setUser(user);
        }

        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> updateUser(Long id, User updatedUser) {
        return userRepository.findById(id).map(existingUser -> {
            // Basic fields
            if (updatedUser.getName() != null) existingUser.setName(updatedUser.getName());
            if (updatedUser.getEmail() != null) existingUser.setEmail(updatedUser.getEmail());
            if (updatedUser.getPassword() != null) existingUser.setPassword(updatedUser.getPassword());
            if (updatedUser.getPhone() != null) existingUser.setPhone(updatedUser.getPhone());
            if (updatedUser.getRole() != null) existingUser.setRole(updatedUser.getRole());
            if (updatedUser.getAccountStatus() != null) existingUser.setAccountStatus(updatedUser.getAccountStatus());
            if (updatedUser.getProfileVisibility() != null) existingUser.setProfileVisibility(updatedUser.getProfileVisibility());

            // AlumniProfile update logic
            AlumniProfile existingProfile = existingUser.getAlumniProfile();
            AlumniProfile newProfile = updatedUser.getAlumniProfile();

            if (existingProfile != null && newProfile != null) {
                // Merge fields
                existingProfile.setGraduationYear(newProfile.getGraduationYear());
                existingProfile.setCurrentPosition(newProfile.getCurrentPosition());
                existingProfile.setCompany(newProfile.getCompany());
                existingProfile.setLocation(newProfile.getLocation());
                existingProfile.setIndustry(newProfile.getIndustry());
                existingProfile.setSkills(newProfile.getSkills());
                existingProfile.setBio(newProfile.getBio());
            } else if (existingProfile == null && newProfile != null) {
                newProfile.setUser(existingUser);
                existingUser.setAlumniProfile(newProfile);
            }

            return userRepository.save(existingUser);
        });
    }

    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<User> getActiveUsers() {
        return userRepository.findAll().stream()
                .filter(user -> user.getAccountStatus() != User.AccountStatus.DELETED)
                .toList();
    }

    public List<User> getUsersByRole(User.Role role) {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() == role && user.getAccountStatus() != User.AccountStatus.DELETED)
                .toList();
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> searchUsers(String query) {
        return userRepository.findAll().stream()
                .filter(user -> user.getAccountStatus() == User.AccountStatus.ACTIVE)
                .filter(user -> 
                    user.getName().toLowerCase().contains(query.toLowerCase()) ||
                    user.getEmail().toLowerCase().contains(query.toLowerCase())
                )
                .toList();
    }

    public List<User> getAllActiveUsers() {
        return userRepository.findAll().stream()
                .filter(user -> user.getAccountStatus() == User.AccountStatus.ACTIVE)
                .toList();
    }
}
