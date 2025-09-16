-- Events table setup
CREATE TABLE IF NOT EXISTS events (
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

-- Event interests junction table
CREATE TABLE IF NOT EXISTS event_interests (
    event_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (event_id, user_id),
    FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Insert sample events
INSERT INTO events (title, description, event_type, event_date, location, organizer, created_at, updated_at, is_active) VALUES
('Alumni Networking Mixer', 'Join fellow alumni for an evening of networking, refreshments, and professional connections. Great opportunity to expand your network and share experiences.', 'ALUMNI_MEETUP', '2024-03-15 18:00:00', 'Grand Hotel Conference Center', 'Alumni Association', NOW(), NOW(), TRUE),
('Tech Skills Workshop: AI & Machine Learning', 'Hands-on workshop covering the latest developments in AI and machine learning. Perfect for both beginners and experienced professionals looking to upskill.', 'WORKSHOP', '2024-03-20 14:00:00', 'University Innovation Lab', 'Tech Alumni Chapter', NOW(), NOW(), TRUE),
('Spring Job Fair 2024', 'Connect with top employers from various industries. Bring your resume and be ready for on-the-spot interviews. Open to all alumni and current students.', 'JOB_FAIR', '2024-04-10 10:00:00', 'University Sports Complex', 'Career Services Office', NOW(), NOW(), TRUE),
('Alumni Homecoming Celebration', 'Annual homecoming event featuring keynote speakers, panel discussions, and networking opportunities. Celebrate our shared journey and achievements.', 'ALUMNI_MEETUP', '2024-04-25 16:00:00', 'University Main Campus', 'Alumni Relations Office', NOW(), NOW(), TRUE),
('Leadership Development Seminar', 'Interactive seminar focused on developing leadership skills for the modern workplace. Learn from successful alumni leaders.', 'WORKSHOP', '2024-05-05 13:00:00', 'Business School Auditorium', 'Leadership Institute', NOW(), NOW(), TRUE);

-- Create indexes for better performance
CREATE INDEX idx_events_event_type ON events(event_type);
CREATE INDEX idx_events_event_date ON events(event_date);
CREATE INDEX idx_events_is_active ON events(is_active);
CREATE INDEX idx_events_organizer ON events(organizer);
