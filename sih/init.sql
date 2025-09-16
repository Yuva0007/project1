-- Initialize the grievance portal database
USE grievance_portal;

-- Create initial departments
INSERT INTO departments (name, code, description, address, phone_number, email, status, created_at, updated_at) VALUES
('Public Works Department', 'PWD', 'Handles infrastructure and public works', '123 Main Street, City', '555-0101', 'pwd@city.gov', 'ACTIVE', NOW(), NOW()),
('Health Department', 'HD', 'Manages healthcare and public health', '456 Health Ave, City', '555-0102', 'health@city.gov', 'ACTIVE', NOW(), NOW()),
('Education Department', 'ED', 'Oversees schools and education', '789 School St, City', '555-0103', 'education@city.gov', 'ACTIVE', NOW(), NOW()),
('Transportation Department', 'TD', 'Manages roads and public transport', '321 Transit Blvd, City', '555-0104', 'transport@city.gov', 'ACTIVE', NOW(), NOW()),
('Utilities Department', 'UD', 'Handles water, electricity, and gas', '654 Utility Way, City', '555-0105', 'utilities@city.gov', 'ACTIVE', NOW(), NOW()),
('Environment Department', 'ENV', 'Environmental protection and waste management', '987 Green St, City', '555-0106', 'environment@city.gov', 'ACTIVE', NOW(), NOW()),
('Safety & Security Department', 'SSD', 'Police and emergency services', '147 Safety Ave, City', '555-0107', 'safety@city.gov', 'ACTIVE', NOW(), NOW()),
('Housing Department', 'HD', 'Housing and urban development', '258 Housing Rd, City', '555-0108', 'housing@city.gov', 'ACTIVE', NOW(), NOW());

-- Create initial admin user
INSERT INTO users (name, email, password, phone_number, role, status, created_at, updated_at) VALUES
('System Administrator', 'admin@grievanceportal.gov.in', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '555-0000', 'SUPER_ADMIN', 'ACTIVE', NOW(), NOW()),
('Department Manager', 'manager@grievanceportal.gov.in', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '555-0001', 'ADMIN', 'ACTIVE', NOW(), NOW());

-- Create sample department officers
INSERT INTO users (name, email, password, phone_number, role, status, department_id, created_at, updated_at) VALUES
('John Smith', 'john.smith@pwd.gov', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '555-1001', 'DEPARTMENT_OFFICER', 'ACTIVE', 1, NOW(), NOW()),
('Sarah Johnson', 'sarah.johnson@health.gov', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '555-1002', 'DEPARTMENT_OFFICER', 'ACTIVE', 2, NOW(), NOW()),
('Mike Davis', 'mike.davis@education.gov', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '555-1003', 'DEPARTMENT_OFFICER', 'ACTIVE', 3, NOW(), NOW()),
('Lisa Wilson', 'lisa.wilson@transport.gov', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '555-1004', 'DEPARTMENT_OFFICER', 'ACTIVE', 4, NOW(), NOW());

-- Create sample citizens
INSERT INTO users (name, email, password, phone_number, role, status, created_at, updated_at) VALUES
('Alice Brown', 'alice.brown@email.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '555-2001', 'CITIZEN', 'ACTIVE', NOW(), NOW()),
('Bob Green', 'bob.green@email.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '555-2002', 'CITIZEN', 'ACTIVE', NOW(), NOW()),
('Carol White', 'carol.white@email.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '555-2003', 'CITIZEN', 'ACTIVE', NOW(), NOW());

-- Create sample grievances
INSERT INTO grievances (title, description, type, priority, status, input_type, language, ai_category, ai_confidence, tracking_number, submitted_at, created_at, updated_at, user_id, department_id, assigned_officer_id) VALUES
('Broken Street Light', 'The street light on Main Street near the park is not working. It has been dark for the past week.', 'INFRASTRUCTURE', 'MEDIUM', 'ASSIGNED', 'TEXT', 'en', 'INFRASTRUCTURE', 0.95, 'GRV1703123456789', NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 1 DAY, 3, 1, 1),
('Pothole on Highway', 'There is a large pothole on Highway 101 near exit 5. It is causing traffic issues and vehicle damage.', 'INFRASTRUCTURE', 'HIGH', 'IN_PROGRESS', 'TEXT', 'en', 'INFRASTRUCTURE', 0.92, 'GRV1703123456790', NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 1 HOUR, 4, 1, 1),
('School Bus Route Issue', 'The school bus route has changed and my child has to walk an extra mile to reach the bus stop.', 'EDUCATION', 'MEDIUM', 'SUBMITTED', 'TEXT', 'en', 'EDUCATION', 0.88, 'GRV1703123456791', NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY, 5, 3, 3),
('Water Supply Problem', 'No water supply in our area for the past 2 days. Please investigate and restore the supply.', 'UTILITIES', 'HIGH', 'ASSIGNED', 'TEXT', 'en', 'UTILITIES', 0.94, 'GRV1703123456792', NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 1 DAY, 6, 5, NULL),
('Garbage Collection Delay', 'Garbage has not been collected in our neighborhood for 3 days. The bins are overflowing.', 'ENVIRONMENT', 'MEDIUM', 'SUBMITTED', 'TEXT', 'en', 'ENVIRONMENT', 0.91, 'GRV1703123456793', NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY, 7, 6, NULL);

-- Create sample status history
INSERT INTO grievance_status_history (grievance_id, from_status, to_status, reason, changed_at, changed_by_id) VALUES
(1, NULL, 'SUBMITTED', 'Grievance submitted by citizen', NOW() - INTERVAL 5 DAY, 3),
(1, 'SUBMITTED', 'ASSIGNED', 'Assigned to Public Works Department', NOW() - INTERVAL 4 DAY, 1),
(2, NULL, 'SUBMITTED', 'Grievance submitted by citizen', NOW() - INTERVAL 3 DAY, 4),
(2, 'SUBMITTED', 'ASSIGNED', 'Assigned to Public Works Department', NOW() - INTERVAL 2 DAY, 1),
(2, 'ASSIGNED', 'IN_PROGRESS', 'Work started on pothole repair', NOW() - INTERVAL 1 HOUR, 1),
(3, NULL, 'SUBMITTED', 'Grievance submitted by citizen', NOW() - INTERVAL 1 DAY, 5),
(4, NULL, 'SUBMITTED', 'Grievance submitted by citizen', NOW() - INTERVAL 2 DAY, 6),
(4, 'SUBMITTED', 'ASSIGNED', 'Assigned to Utilities Department', NOW() - INTERVAL 1 DAY, 1),
(5, NULL, 'SUBMITTED', 'Grievance submitted by citizen', NOW() - INTERVAL 1 DAY, 7);

-- Create sample comments
INSERT INTO grievance_comments (grievance_id, comment, type, created_at, user_id) VALUES
(1, 'Received the complaint. Will inspect the street light within 24 hours.', 'INTERNAL', NOW() - INTERVAL 4 DAY, 1),
(1, 'Inspection completed. Found faulty wiring. Replacement parts ordered.', 'INTERNAL', NOW() - INTERVAL 3 DAY, 1),
(2, 'Pothole repair work has been scheduled for tomorrow morning.', 'PUBLIC', NOW() - INTERVAL 1 HOUR, 1),
(4, 'Water supply issue reported to maintenance team. Investigation in progress.', 'INTERNAL', NOW() - INTERVAL 1 DAY, 1);

-- Create sample attachments (placeholder)
INSERT INTO grievance_attachments (file_name, file_path, file_type, file_size, mime_type, uploaded_at, grievance_id) VALUES
('street_light_photo.jpg', '/uploads/street_light_photo.jpg', 'image/jpeg', 1024000, 'image/jpeg', NOW() - INTERVAL 5 DAY, 1),
('pothole_photo.jpg', '/uploads/pothole_photo.jpg', 'image/jpeg', 2048000, 'image/jpeg', NOW() - INTERVAL 3 DAY, 2);
