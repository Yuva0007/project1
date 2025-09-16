-- MySQL Database Setup Script for Alumni Management System
-- Run this script to fix database connection issues

-- Create database
CREATE DATABASE IF NOT EXISTS alumni_db;
USE alumni_db;

-- Create user with proper authentication plugin for MySQL 8.0+
CREATE USER IF NOT EXISTS 'alumni_user'@'localhost' IDENTIFIED WITH mysql_native_password BY 'alumni123';
GRANT ALL PRIVILEGES ON alumni_db.* TO 'alumni_user'@'localhost';
FLUSH PRIVILEGES;

-- Alternative: If you prefer to use caching_sha2_password (MySQL 8.0+ default)
-- CREATE USER IF NOT EXISTS 'alumni_user'@'localhost' IDENTIFIED WITH caching_sha2_password BY 'alumni123';

-- Verify user creation
SELECT User, Host, plugin FROM mysql.user WHERE User = 'alumni_user';

-- Show grants for the user
SHOW GRANTS FOR 'alumni_user'@'localhost';

-- The tables will be created automatically by Spring Boot JPA
-- based on your entity classes when you run the application
