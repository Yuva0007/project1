package com.example.demo.service;

import com.example.demo.model.Event;
import com.example.demo.model.User;
import com.example.demo.repository.EventRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EventServiceImpl implements EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Event createEvent(Event event) {
        event.setCreatedAt(LocalDateTime.now());
        event.setUpdatedAt(LocalDateTime.now());
        event.setActive(true);
        return eventRepository.save(event);
    }

    @Override
    public Event updateEvent(Long id, Event event) {
        Optional<Event> existingEvent = eventRepository.findById(id);
        if (existingEvent.isPresent()) {
            Event updatedEvent = existingEvent.get();
            updatedEvent.setTitle(event.getTitle());
            updatedEvent.setDescription(event.getDescription());
            updatedEvent.setEventType(event.getEventType());
            updatedEvent.setEventDate(event.getEventDate());
            updatedEvent.setLocation(event.getLocation());
            updatedEvent.setOrganizer(event.getOrganizer());
            updatedEvent.setUpdatedAt(LocalDateTime.now());
            return eventRepository.save(updatedEvent);
        }
        throw new RuntimeException("Event not found with id: " + id);
    }

    @Override
    public void deleteEvent(Long id) {
        Optional<Event> event = eventRepository.findById(id);
        if (event.isPresent()) {
            Event existingEvent = event.get();
            existingEvent.setActive(false);
            existingEvent.setUpdatedAt(LocalDateTime.now());
            eventRepository.save(existingEvent);
        } else {
            throw new RuntimeException("Event not found with id: " + id);
        }
    }

    @Override
    public Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));
    }

    @Override
    public List<Event> getAllActiveEvents() {
        return eventRepository.findByIsActiveOrderByEventDateAsc(true);
    }

    @Override
    public List<Event> getEventsByType(String eventType) {
        return eventRepository.findByEventTypeAndIsActiveOrderByEventDateAsc(eventType, true);
    }

    @Override
    public List<Event> getUpcomingEvents() {
        return eventRepository.findUpcomingEvents(LocalDateTime.now());
    }

    @Override
    public void addUserInterest(Long eventId, Long userId) {
        Event event = getEventById(eventId);
        Optional<User> user = userRepository.findById(userId);
        
        if (user.isPresent()) {
            event.getInterestedUsers().add(user.get());
            eventRepository.save(event);
        } else {
            throw new RuntimeException("User not found with id: " + userId);
        }
    }

    @Override
    public void removeUserInterest(Long eventId, Long userId) {
        Event event = getEventById(eventId);
        Optional<User> user = userRepository.findById(userId);
        
        if (user.isPresent()) {
            event.getInterestedUsers().remove(user.get());
            eventRepository.save(event);
        } else {
            throw new RuntimeException("User not found with id: " + userId);
        }
    }

    @Override
    public boolean isUserInterested(Long eventId, Long userId) {
        Event event = getEventById(eventId);
        Optional<User> user = userRepository.findById(userId);
        
        if (user.isPresent()) {
            return event.getInterestedUsers().contains(user.get());
        }
        return false;
    }
}
