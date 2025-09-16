package com.example.demo.repository;

import com.example.demo.model.Connection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConnectionRepository extends JpaRepository<Connection, Long> {

    List<Connection> findByStudentUserEmail(String email);

    List<Connection> findByAlumniUserEmail(String email);

    boolean existsByStudentUserEmailAndAlumniUserEmailAndStatus(
            String studentEmail,
            String alumniEmail,
            Connection.Status status
    );
}
