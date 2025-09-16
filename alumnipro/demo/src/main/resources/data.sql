-- Insert test users for development
INSERT INTO users (name, email, password, role, account_status, profile_visibility, created_at) 
VALUES ('Test User', 'test@example.com', '$2a$10$iEgqMsOzFscdbsxY8rD7hOxrPCLqR.2Rts7aWnZtSRF2BjR0CccI6', 'STUDENT', 'ACTIVE', 'PUBLIC', CURRENT_TIMESTAMP);

INSERT INTO users (name, email, password, role, account_status, profile_visibility, created_at) 
VALUES ('Admin User', 'admin@example.com', '$2a$10$68dXpAhzry/Ym72.7iw4xOPN2LgB9eaJnOdu4Co2BJ/HXvXApYCwi', 'ADMIN', 'ACTIVE', 'PUBLIC', CURRENT_TIMESTAMP);
