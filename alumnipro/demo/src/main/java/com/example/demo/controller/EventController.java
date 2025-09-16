package com.example.demo.controller;

import com.example.demo.model.Event;
import com.example.demo.service.EventService;
import com.example.demo.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
public class EventController {

    @Autowired
    private EventService eventService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<Event> createEvent(@RequestBody Event event, HttpServletRequest request) {
        String jwt = extractJwtFromRequest(request);
        String organizer = jwtUtil.extractEmail(jwt);
        if (organizer == null) {
            organizer = jwtUtil.extractUsername(jwt);
        }
        
        event.setOrganizer(organizer);
        Event createdEvent = eventService.createEvent(event);
        return ResponseEntity.ok(createdEvent);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Event> updateEvent(@PathVariable Long id, @RequestBody Event event) {
        Event updatedEvent = eventService.updateEvent(id, event);
        return ResponseEntity.ok(updatedEvent);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        Event event = eventService.getEventById(id);
        return ResponseEntity.ok(event);
    }

    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents() {
        List<Event> events = eventService.getAllActiveEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/type/{eventType}")
    public ResponseEntity<List<Event>> getEventsByType(@PathVariable String eventType) {
        List<Event> events = eventService.getEventsByType(eventType);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<Event>> getUpcomingEvents() {
        List<Event> events = eventService.getUpcomingEvents();
        return ResponseEntity.ok(events);
    }

    @PostMapping("/{eventId}/interest")
    public ResponseEntity<Void> addUserInterest(@PathVariable Long eventId, HttpServletRequest request) {
        String jwt = extractJwtFromRequest(request);
        Long userId = getUserIdFromJwt(jwt);
        
        if (userId != null) {
            eventService.addUserInterest(eventId, userId);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    @DeleteMapping("/{eventId}/interest")
    public ResponseEntity<Void> removeUserInterest(@PathVariable Long eventId, HttpServletRequest request) {
        String jwt = extractJwtFromRequest(request);
        Long userId = getUserIdFromJwt(jwt);
        
        if (userId != null) {
            eventService.removeUserInterest(eventId, userId);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    @GetMapping("/{eventId}/interest")
    public ResponseEntity<Boolean> isUserInterested(@PathVariable Long eventId, HttpServletRequest request) {
        String jwt = extractJwtFromRequest(request);
        Long userId = getUserIdFromJwt(jwt);
        
        if (userId != null) {
            boolean isInterested = eventService.isUserInterested(eventId, userId);
            return ResponseEntity.ok(isInterested);
        }
        return ResponseEntity.badRequest().build();
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private Long getUserIdFromJwt(String jwt) {
        if (jwt != null) {
            try {
                String email = jwtUtil.extractEmail(jwt);
                if (email != null) {
                    // For now, we'll use a simple approach
                    // In a production environment, you'd want to inject UserService and get the actual user ID
                    return 1L; // Placeholder - replace with actual user ID extraction
                }
            } catch (Exception e) {
                // Handle exception
            }
        }
        return null;
    }
}
