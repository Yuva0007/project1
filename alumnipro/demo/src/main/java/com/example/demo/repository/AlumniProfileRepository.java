package com.example.demo.repository;

import com.example.demo.model.AlumniProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AlumniProfileRepository extends JpaRepository<AlumniProfile, Long> {

    Optional<AlumniProfile> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
