@echo off
setlocal enabledelayedexpansion

echo ==========================================
echo   Unified Citizen Grievance Portal Setup
echo ==========================================
echo.

REM Check if Java is installed
echo [INFO] Checking system requirements...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Java is not installed. Please install Java 21 or higher.
    pause
    exit /b 1
)
echo [SUCCESS] Java is installed

REM Check if Node.js is installed
node --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Node.js is not installed. Please install Node.js 18 or higher.
    pause
    exit /b 1
)
echo [SUCCESS] Node.js is installed

REM Check if Maven is installed
mvn --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Maven is not installed. Please install Maven 3.6 or higher.
    pause
    exit /b 1
)
echo [SUCCESS] Maven is installed

echo.
echo [INFO] Setting up backend...
cd demo
call mvn clean install -DskipTests
if %errorlevel% neq 0 (
    echo [ERROR] Backend build failed
    pause
    exit /b 1
)
echo [SUCCESS] Backend build completed
cd ..

echo.
echo [INFO] Setting up frontend...
cd frontend
call npm install
if %errorlevel% neq 0 (
    echo [ERROR] Frontend dependencies installation failed
    pause
    exit /b 1
)
echo [SUCCESS] Frontend dependencies installed
cd ..

echo.
echo [INFO] Creating startup scripts...

REM Create backend startup script
echo @echo off > start-backend.bat
echo echo Starting Citizen Grievance Portal Backend... >> start-backend.bat
echo cd demo >> start-backend.bat
echo call mvn spring-boot:run >> start-backend.bat
echo pause >> start-backend.bat

REM Create frontend startup script
echo @echo off > start-frontend.bat
echo echo Starting Citizen Grievance Portal Frontend... >> start-frontend.bat
echo cd frontend >> start-frontend.bat
echo call npm start >> start-frontend.bat
echo pause >> start-frontend.bat

echo [SUCCESS] Setup completed successfully!
echo.
echo Next steps:
echo 1. Configure your database settings in demo/src/main/resources/application.properties
echo 2. Set your OpenAI API key in the same file
echo 3. Start the backend: start-backend.bat
echo 4. Start the frontend: start-frontend.bat
echo.
echo Access the application at:
echo - Frontend: http://localhost:3000
echo - Backend API: http://localhost:8080
echo.
echo Default admin credentials:
echo - Username: admin
echo - Password: admin123
echo.
echo [WARNING] Remember to change default credentials in production!
echo.
pause
