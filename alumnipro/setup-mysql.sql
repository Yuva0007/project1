-- MySQL Database Setup Script for Alumni Management System

-- Create database
CREATE DATABASE IF NOT EXISTS alumni_db;
USE alumni_db;

-- Create user (optional - adjust as needed)
-- CREATE USER IF NOT EXISTS 'alumni_user'@'localhost' IDENTIFIED BY 'alumni_password';
-- GRANT ALL PRIVILEGES ON alumni_db.* TO 'alumni_user'@'localhost';
-- FLUSH PRIVILEGES;

-- The tables will be created automatically by Spring Boot JPA
-- based on your entity classes when you run the application
