package com.example.demo.service;

import com.example.demo.model.AlumniProfile;
import com.example.demo.repository.AlumniProfileRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AlumniProfileService {

    private final AlumniProfileRepository alumniProfileRepository;

    public AlumniProfileService(AlumniProfileRepository alumniProfileRepository) {
        this.alumniProfileRepository = alumniProfileRepository;
    }

    public AlumniProfile createAlumniProfile(AlumniProfile profile) {
        if (profile.getUser() != null) {
            profile.getUser().setAlumniProfile(profile);
        }

        return alumniProfileRepository.save(profile);
    }

    public List<AlumniProfile> getAllProfiles() {
        return alumniProfileRepository.findAll();
    }

    public Optional<AlumniProfile> getProfileById(Long id) {
        return alumniProfileRepository.findById(id);
    }

    public Optional<AlumniProfile> updateProfile(Long id, AlumniProfile updatedProfile) {
        return alumniProfileRepository.findById(id).map(profile -> {
            profile.setGraduationYear(updatedProfile.getGraduationYear());
            profile.setCurrentPosition(updatedProfile.getCurrentPosition());
            profile.setCompany(updatedProfile.getCompany());
            profile.setLocation(updatedProfile.getLocation());

            if (updatedProfile.getUser() != null) {
                updatedProfile.getUser().setAlumniProfile(profile);
                profile.setUser(updatedProfile.getUser());
            }

            return alumniProfileRepository.save(profile);
        });
    }

    public boolean deleteProfile(Long id) {
        if (alumniProfileRepository.existsById(id)) {
            alumniProfileRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
