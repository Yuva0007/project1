package com.example.demo.repository;

import com.example.demo.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    
    List<Event> findByIsActiveOrderByEventDateAsc(boolean isActive);
    
    List<Event> findByEventTypeAndIsActiveOrderByEventDateAsc(String eventType, boolean isActive);
    
    @Query("SELECT e FROM Event e WHERE e.eventDate >= :now AND e.isActive = true ORDER BY e.eventDate ASC")
    List<Event> findUpcomingEvents(@Param("now") LocalDateTime now);
    
    @Query("SELECT e FROM Event e WHERE e.organizer = :organizer ORDER BY e.createdAt DESC")
    List<Event> findByOrganizer(@Param("organizer") String organizer);
}
