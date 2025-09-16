#!/bin/bash

# Unified Citizen Grievance Portal Setup Script
# This script helps set up the development environment

echo "🚀 Setting up Unified Citizen Grievance Portal..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if required tools are installed
check_requirements() {
    print_status "Checking system requirements..."
    
    # Check Java
    if command -v java &> /dev/null; then
        JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
        if [ "$JAVA_VERSION" -ge 21 ]; then
            print_success "Java $JAVA_VERSION is installed"
        else
            print_error "Java 21 or higher is required. Current version: $JAVA_VERSION"
            exit 1
        fi
    else
        print_error "Java is not installed. Please install Java 21 or higher."
        exit 1
    fi
    
    # Check Node.js
    if command -v node &> /dev/null; then
        NODE_VERSION=$(node -v | cut -d'v' -f2 | cut -d'.' -f1)
        if [ "$NODE_VERSION" -ge 18 ]; then
            print_success "Node.js $NODE_VERSION is installed"
        else
            print_error "Node.js 18 or higher is required. Current version: $NODE_VERSION"
            exit 1
        fi
    else
        print_error "Node.js is not installed. Please install Node.js 18 or higher."
        exit 1
    fi
    
    # Check MySQL
    if command -v mysql &> /dev/null; then
        print_success "MySQL is installed"
    else
        print_warning "MySQL is not installed. Please install MySQL 8.0 or higher."
    fi
    
    # Check Maven
    if command -v mvn &> /dev/null; then
        print_success "Maven is installed"
    else
        print_error "Maven is not installed. Please install Maven 3.6 or higher."
        exit 1
    fi
}

# Setup database
setup_database() {
    print_status "Setting up database..."
    
    read -p "Enter MySQL root password: " -s MYSQL_ROOT_PASSWORD
    echo
    
    read -p "Enter database name (default: grievance_portal): " DB_NAME
    DB_NAME=${DB_NAME:-grievance_portal}
    
    read -p "Enter database username (default: grievance_user): " DB_USER
    DB_USER=${DB_USER:-grievance_user}
    
    read -p "Enter database password: " -s DB_PASSWORD
    echo
    
    # Create database and user
    mysql -u root -p$MYSQL_ROOT_PASSWORD << EOF
CREATE DATABASE IF NOT EXISTS $DB_NAME;
CREATE USER IF NOT EXISTS '$DB_USER'@'localhost' IDENTIFIED BY '$DB_PASSWORD';
GRANT ALL PRIVILEGES ON $DB_NAME.* TO '$DB_USER'@'localhost';
FLUSH PRIVILEGES;
EOF
    
    if [ $? -eq 0 ]; then
        print_success "Database setup completed"
        
        # Update application.properties
        print_status "Updating database configuration..."
        sed -i "s/grievance_portal/$DB_NAME/g" demo/src/main/resources/application.properties
        sed -i "s/root/$DB_USER/g" demo/src/main/resources/application.properties
        sed -i "s/password/$DB_PASSWORD/g" demo/src/main/resources/application.properties
        
        print_success "Database configuration updated"
    else
        print_error "Database setup failed"
        exit 1
    fi
}

# Setup environment variables
setup_environment() {
    print_status "Setting up environment variables..."
    
    read -p "Enter OpenAI API Key: " OPENAI_API_KEY
    read -p "Enter email username (for notifications): " EMAIL_USERNAME
    read -p "Enter email password (app password): " -s EMAIL_PASSWORD
    echo
    
    # Create .env file for backend
    cat > demo/.env << EOF
OPENAI_API_KEY=$OPENAI_API_KEY
EMAIL_USERNAME=$EMAIL_USERNAME
EMAIL_PASSWORD=$EMAIL_PASSWORD
EOF
    
    print_success "Environment variables configured"
}

# Install backend dependencies
setup_backend() {
    print_status "Setting up backend..."
    
    cd demo
    
    # Build the project
    print_status "Building Spring Boot application..."
    mvn clean install -DskipTests
    
    if [ $? -eq 0 ]; then
        print_success "Backend build completed"
    else
        print_error "Backend build failed"
        exit 1
    fi
    
    cd ..
}

# Install frontend dependencies
setup_frontend() {
    print_status "Setting up frontend..."
    
    cd frontend
    
    # Install dependencies
    print_status "Installing Node.js dependencies..."
    npm install
    
    if [ $? -eq 0 ]; then
        print_success "Frontend dependencies installed"
    else
        print_error "Frontend dependencies installation failed"
        exit 1
    fi
    
    cd ..
}

# Create startup scripts
create_startup_scripts() {
    print_status "Creating startup scripts..."
    
    # Backend startup script
    cat > start-backend.sh << 'EOF'
#!/bin/bash
echo "🚀 Starting Citizen Grievance Portal Backend..."
cd demo
mvn spring-boot:run
EOF
    
    # Frontend startup script
    cat > start-frontend.sh << 'EOF'
#!/bin/bash
echo "🚀 Starting Citizen Grievance Portal Frontend..."
cd frontend
npm start
EOF
    
    # Combined startup script
    cat > start-all.sh << 'EOF'
#!/bin/bash
echo "🚀 Starting Citizen Grievance Portal..."

# Start backend in background
echo "Starting backend..."
cd demo
mvn spring-boot:run &
BACKEND_PID=$!

# Wait for backend to start
sleep 30

# Start frontend
echo "Starting frontend..."
cd ../frontend
npm start &
FRONTEND_PID=$!

# Wait for user to stop
echo "Press Ctrl+C to stop all services"
wait $BACKEND_PID $FRONTEND_PID
EOF
    
    # Make scripts executable
    chmod +x start-backend.sh start-frontend.sh start-all.sh
    
    print_success "Startup scripts created"
}

# Main setup function
main() {
    echo "=========================================="
    echo "  Unified Citizen Grievance Portal Setup"
    echo "=========================================="
    echo
    
    check_requirements
    echo
    
    read -p "Do you want to set up the database? (y/n): " SETUP_DB
    if [[ $SETUP_DB =~ ^[Yy]$ ]]; then
        setup_database
        echo
    fi
    
    read -p "Do you want to configure environment variables? (y/n): " SETUP_ENV
    if [[ $SETUP_ENV =~ ^[Yy]$ ]]; then
        setup_environment
        echo
    fi
    
    setup_backend
    echo
    
    setup_frontend
    echo
    
    create_startup_scripts
    echo
    
    print_success "Setup completed successfully!"
    echo
    echo "Next steps:"
    echo "1. Start the backend: ./start-backend.sh"
    echo "2. Start the frontend: ./start-frontend.sh"
    echo "3. Or start both: ./start-all.sh"
    echo
    echo "Access the application at:"
    echo "- Frontend: http://localhost:3000"
    echo "- Backend API: http://localhost:8080"
    echo
    echo "Default admin credentials:"
    echo "- Username: admin"
    echo "- Password: admin123"
    echo
    print_warning "Remember to change default credentials in production!"
}

# Run main function
main "$@"
