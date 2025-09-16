-- Seed one admin user (email: admin@example.com, password: password)
INSERT INTO users (name, email, password, role, status, created_at, updated_at)
VALUES (
  'System Admin',
  'admin@example.com',
  '$2a$10$7EqJtq98hPqEX7fNZaFWoOa5EtRyyuWZ1jCei8aD9K8S9Z/atrK5K',
  'ADMIN',
  'ACTIVE',
  NOW(),
  NOW()
)
ON DUPLICATE KEY UPDATE 
  name = VALUES(name),
  password = VALUES(password),
  role = VALUES(role),
  status = VALUES(status),
  updated_at = NOW();


