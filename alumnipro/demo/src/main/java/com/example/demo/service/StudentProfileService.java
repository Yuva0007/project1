package com.example.demo.service;

import com.example.demo.model.StudentProfile;
import com.example.demo.repository.StudentProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StudentProfileService {

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    public List<StudentProfile> getAllProfiles() {
        return studentProfileRepository.findAll();
    }

    public Optional<StudentProfile> getProfileById(Long id) {
        return studentProfileRepository.findById(id);
    }

    public StudentProfile createProfile(StudentProfile profile) {
        return studentProfileRepository.save(profile);
    }

    public StudentProfile updateProfile(Long id, StudentProfile updatedProfile) {
        return studentProfileRepository.findById(id).map(existing -> {
            existing.setDepartment(updatedProfile.getDepartment());
            existing.setYearOfStudy(updatedProfile.getYearOfStudy());
            existing.setRegistrationNumber(updatedProfile.getRegistrationNumber());
            return studentProfileRepository.save(existing);
        }).orElse(null);
    }

    public void deleteProfile(Long id) {
        studentProfileRepository.deleteById(id);
    }
}
