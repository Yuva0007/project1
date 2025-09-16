package com.example.demo.service;

import com.example.demo.model.Event;
import java.util.List;

public interface EventService {
    
    Event createEvent(Event event);
    
    Event updateEvent(Long id, Event event);
    
    void deleteEvent(Long id);
    
    Event getEventById(Long id);
    
    List<Event> getAllActiveEvents();
    
    List<Event> getEventsByType(String eventType);
    
    List<Event> getUpcomingEvents();
    
    void addUserInterest(Long eventId, Long userId);
    
    void removeUserInterest(Long eventId, Long userId);
    
    boolean isUserInterested(Long eventId, Long userId);
}
