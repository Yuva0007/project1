package com.example.demo.controller;

import com.example.demo.model.Connection;
import com.example.demo.service.ConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/connections")
public class ConnectionController {

    @Autowired
    private ConnectionService connectionService;

    // ADMIN: View all connections
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Connection> getAllConnections() {
        return connectionService.getAllConnections();
    }

    // STUDENT & ALUMNI: View their own connections
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('STUDENT','ALUMNI')")
    public List<Connection> getMyConnections(Principal principal) {
        return connectionService.getConnectionsForCurrentUser(principal);
    }

    // STUDENT: Send request to alumni
    @PostMapping("/request/{alumniId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Connection> sendConnectionRequest(
            @PathVariable Long alumniId,
            Principal principal) {
        return ResponseEntity.ok(connectionService.sendConnectionRequest(alumniId, principal));
    }

    // ALUMNI: Accept or reject request
    @PutMapping("/{connectionId}/respond")
    @PreAuthorize("hasRole('ALUMNI')")
    public ResponseEntity<Connection> respondToConnection(
            @PathVariable Long connectionId,
            @RequestParam Connection.Status status, // Use Enum directly
            Principal principal) {
        return ResponseEntity.ok(connectionService.respondToConnection(connectionId, status, principal));
    }

    // ADMIN: Delete a connection
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteConnection(@PathVariable Long id) {
        connectionService.deleteConnection(id);
        return ResponseEntity.noContent().build();
    }
}
