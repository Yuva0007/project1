package com.example.demo.service;

import com.example.demo.model.Connection;
import com.example.demo.model.User;
import com.example.demo.repository.ConnectionRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Service
public class ConnectionService {

    @Autowired
    private ConnectionRepository connectionRepository;

    @Autowired
    private UserRepository userRepository;

    // ADMIN: View all connections
    public List<Connection> getAllConnections() {
        return connectionRepository.findAll();
    }

    public Optional<Connection> getConnectionById(Long id) {
        return connectionRepository.findById(id);
    }

    // STUDENT & ALUMNI: View their own connections
    public List<Connection> getConnectionsForCurrentUser(Principal principal) {
        User user = getUserByEmail(principal.getName());

        switch (user.getRole()) {
            case STUDENT:
                return connectionRepository.findByStudentUserEmail(user.getEmail());
            case ALUMNI:
                return connectionRepository.findByAlumniUserEmail(user.getEmail());
            default:
                throw new RuntimeException("Only students or alumni can view their connections");
        }
    }

    // STUDENT: Send connection request to alumni
    public Connection sendConnectionRequest(Long alumniId, Principal principal) {
        User student = getUserByEmail(principal.getName());

        if (student.getRole() != User.Role.STUDENT) {
            throw new RuntimeException("Only students can send connection requests");
        }

        User alumni = userRepository.findById(alumniId)
                .orElseThrow(() -> new RuntimeException("Alumni not found"));

        if (alumni.getRole() != User.Role.ALUMNI) {
            throw new RuntimeException("Target user is not an alumni");
        }

        // Optimized duplicate check
        boolean exists = connectionRepository.existsByStudentUserEmailAndAlumniUserEmailAndStatus(
                student.getEmail(), alumni.getEmail(), Connection.Status.PENDING);

        if (exists) {
            throw new RuntimeException("You have already sent a connection request to this alumni");
        }

        Connection connection = new Connection();
        connection.setStudent(student.getStudentProfile());
        connection.setAlumni(alumni.getAlumniProfile());
        connection.setStatus(Connection.Status.PENDING);

        return connectionRepository.save(connection);
    }

    // ALUMNI: Accept or reject connection request
    public Connection respondToConnection(Long connectionId, Connection.Status status, Principal principal) {
        User alumni = getUserByEmail(principal.getName());

        if (alumni.getRole() != User.Role.ALUMNI) {
            throw new RuntimeException("Only alumni can respond to connection requests");
        }

        Connection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new RuntimeException("Connection not found"));

        // Ensure the request is for the logged-in alumni
        if (!connection.getAlumni().getId().equals(alumni.getAlumniProfile().getId())) {
            throw new RuntimeException("You are not authorized to respond to this request");
        }

        connection.setStatus(status);
        return connectionRepository.save(connection);
    }

    // ADMIN: Delete a connection
    public void deleteConnection(Long id) {
        if (!connectionRepository.existsById(id)) {
            throw new RuntimeException("Connection not found");
        }
        connectionRepository.deleteById(id);
    }

    // Helper method to fetch user
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
