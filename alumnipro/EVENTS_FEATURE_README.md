# Events Feature - Alumni Network Application

## Overview
The Events feature allows administrators to create and manage various types of events (Alumni Meetups, Workshops, Job Fairs) while enabling students and alumni to browse events and show interest in attending.

## Features

### For Administrators
- **Create Events**: Add new events with title, description, type, date/time, location, and organizer
- **Edit Events**: Modify existing event details
- **Delete Events**: Soft delete events (mark as inactive)
- **Event Management**: Full CRUD operations on events

### For All Users (Students & Alumni)
- **Browse Events**: View all active events with filtering by type
- **Show Interest**: Click "Show Interest" button to indicate attendance
- **Event Details**: View comprehensive event information including date, location, and organizer

## Event Types
1. **ALUMNI_MEETUP** - Networking events, reunions, social gatherings
2. **WORKSHOP** - Skill development, training sessions, seminars
3. **JOB_FAIR** - Career opportunities, recruitment events

## Technical Implementation

### Backend Components

#### 1. Event Model (`Event.java`)
- Core entity with fields: id, title, description, eventType, eventDate, location, organizer, timestamps, isActive
- Many-to-many relationship with User entity for tracking interests
- JPA annotations for database mapping

#### 2. Event Repository (`EventRepository.java`)
- Extends JpaRepository for basic CRUD operations
- Custom queries for filtering by type, upcoming events, and organizer

#### 3. Event Service (`EventService.java` & `EventServiceImpl.java`)
- Business logic for event operations
- Interest management (add/remove user interest)
- Event validation and data processing

#### 4. Event Controller (`EventController.java`)
- REST endpoints for event management
- JWT authentication for user identification
- API endpoints:
  - `POST /api/events` - Create new event
  - `GET /api/events` - Get all active events
  - `GET /api/events/{id}` - Get specific event
  - `PUT /api/events/{id}` - Update event
  - `DELETE /api/events/{id}` - Soft delete event
  - `POST /api/events/{id}/interest` - Show interest
  - `DELETE /api/events/{id}/interest` - Remove interest
  - `GET /api/events/{id}/interest` - Check if user is interested

### Frontend Components

#### 1. Events Component (`Events.js`)
- Main events page with responsive grid layout
- Role-based access control (admin vs. regular users)
- Event filtering by type
- Interest management for non-admin users

#### 2. Events Styling (`Events.css`)
- Modern, glassmorphism design
- Responsive grid layout
- Hover effects and animations
- Modal forms for create/edit operations

### Database Schema

#### Events Table
```sql
CREATE TABLE events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    event_type VARCHAR(50) NOT NULL,
    event_date DATETIME NOT NULL,
    location VARCHAR(255) NOT NULL,
    organizer VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    is_active BOOLEAN DEFAULT TRUE
);
```

#### Event Interests Junction Table
```sql
CREATE TABLE event_interests (
    event_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (event_id, user_id),
    FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

## Setup Instructions

### 1. Database Setup
Run the SQL script `setup-events.sql` to create the necessary tables and sample data.

### 2. Backend
- Ensure all Java files are in the correct package structure
- The application will automatically create the Event entity tables on startup

### 3. Frontend
- Add the Events component to your routing
- Access via `/events` route
- Admin users can access from AdminDashboard
- Regular users can access from Dashboard

## Usage Examples

### Creating an Event (Admin)
1. Navigate to Events page
2. Click "Create New Event" button
3. Fill in event details (title, description, type, date, time, location)
4. Click "Create Event"

### Showing Interest (Students/Alumni)
1. Browse events on the Events page
2. Click "Show Interest" button on any event
3. Button changes to "✓ Interested" to confirm
4. Click again to remove interest

### Filtering Events
- Use the dropdown filter to view events by type
- Options: All Events, Alumni Meetups, Workshops, Job Fairs

## Security Features
- JWT authentication required for all operations
- Role-based access control (admin vs. regular users)
- Users can only modify their own interests
- Soft delete for events (data preservation)

## Future Enhancements
- Event registration with capacity limits
- Email notifications for event updates
- Calendar integration
- Event reminders
- Photo galleries for past events
- Event feedback and ratings

## API Documentation

### Authentication
All endpoints require a valid JWT token in the Authorization header:
```
Authorization: Bearer <jwt_token>
```

### Response Formats
- Success responses include the requested data
- Error responses include appropriate HTTP status codes and error messages
- All timestamps are in ISO 8601 format

## Troubleshooting

### Common Issues
1. **Events not loading**: Check database connection and table existence
2. **Interest not saving**: Verify JWT token validity and user authentication
3. **Admin features not visible**: Ensure user role is set to 'ADMIN'

### Debug Mode
Enable debug logging in the backend to troubleshoot issues with event operations.

## Contributing
When adding new features to the Events system:
1. Update the Event model if new fields are needed
2. Add corresponding repository methods
3. Implement service layer logic
4. Create appropriate controller endpoints
5. Update frontend components and styling
6. Add comprehensive tests
7. Update this documentation
