# Database Setup Guide - Alumni Management System

## Issue Fixed: MySQL Connection Error
**Error**: `Public Key Retrieval is not allowed`

## Root Cause
MySQL 8.0+ uses `caching_sha2_password` as the default authentication plugin, which requires `allowPublicKeyRetrieval=true` parameter in the JDBC URL for password authentication.

## Steps to Fix

### 1. MySQL Server Setup
```bash
# Start MySQL server (if not running)
sudo systemctl start mysql
# or
net start mysql
```

### 2. Run Database Setup Script
```bash
# Connect to MySQL as root
mysql -u root -p

# Run the setup script
source setup-mysql-fixed.sql
```

### 3. Verify Database and User
```sql
-- Check if database exists
SHOW DATABASES LIKE 'alumni_db';

-- Check if user exists
SELECT User, Host FROM mysql.user WHERE User = 'alumni_user';

-- Test connection
mysql -u alumni_user -p alumni_db
```

### 4. Application Configuration
The `application.properties` file has been updated with:
- ✅ Added `allowPublicKeyRetrieval=true` parameter
- ✅ Updated deprecated `MySQL8Dialect` to `MySQLDialect`
- ✅ Added connection pooling optimizations
- ✅ Added debug logging for SQL queries

### 5. Restart Application
```bash
# From the demo directory
cd demo
./mvnw spring-boot:run
```

## Alternative Solutions

### If still having issues:

1. **Use MySQL Native Password Authentication**:
   ```sql
   ALTER USER 'alumni_user'@'localhost' IDENTIFIED WITH mysql_native_password BY 'alumni123';
   ```

2. **Check MySQL Version**:
   ```bash
   mysql --version
   ```

3. **Verify MySQL is listening on port 3306**:
   ```bash
   netstat -an | grep 3306
   ```

4. **Check firewall settings** (Windows):
   ```bash
   netsh advfirewall firewall add rule name="MySQL" dir=in action=allow protocol=TCP localport=3306
   ```

## Testing the Connection

### Test from Command Line:
```bash
mysql -u alumni_user -p alumni_db
```

### Test from Spring Boot:
The application should now start successfully with logs showing:
- `HikariPool-1 - Starting...`
- `HikariPool-1 - Start completed.`
- Database connection established
- JPA repositories initialized
- Tomcat started on port 8080

## Common Issues and Solutions

1. **Access denied for user**:
   - Ensure user has proper privileges: `GRANT ALL PRIVILEGES ON alumni_db.* TO 'alumni_user'@'localhost';`

2. **Unknown database 'alumni_db'**:
   - Create database manually: `CREATE DATABASE alumni_db;`

3. **Connection refused**:
   - Ensure MySQL is running: `sudo systemctl status mysql`
   - Check if MySQL is listening on port 3306

4. **Timezone issues**:
   - The JDBC URL already includes `serverTimezone=UTC`
