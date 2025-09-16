# Unified Citizen Grievance Portal with AI Chatbot

A comprehensive multilingual platform that accepts citizen complaints through voice/text input, auto-categorizes them using AI, assigns to relevant departments, and tracks progress transparently.

## 🚀 Features

### Core Functionality
- **Multilingual Support**: Submit grievances in multiple Indian languages
- **Voice & Text Input**: Record voice messages or type complaints
- **AI-Powered Categorization**: Automatic categorization and department assignment
- **Real-time Tracking**: Track grievance progress with live updates
- **File Attachments**: Upload images, videos, and documents
- **Email Notifications**: Automated status updates and notifications

### User Roles
- **Citizens**: Submit and track grievances
- **Department Officers**: Manage assigned grievances
- **Administrators**: Oversee the entire system
- **Super Admins**: Full system control

### AI Features
- **Smart Categorization**: Uses OpenAI GPT models for intelligent categorization
- **Confidence Scoring**: AI provides confidence levels for categorization
- **Auto-assignment**: High-confidence grievances are automatically assigned
- **Multilingual Processing**: AI understands and processes multiple languages

## 🏗️ Architecture

### Backend (Spring Boot)
- **Framework**: Spring Boot 3.5.5 with Java 21
- **Database**: MySQL with JPA/Hibernate
- **Security**: Spring Security with JWT
- **AI Integration**: Spring AI with OpenAI
- **File Storage**: Local file system with Apache Tika
- **Email**: Spring Mail with SMTP
- **WebSocket**: Real-time updates

### Frontend (React)
- **Framework**: React 19 with modern hooks
- **Styling**: Tailwind CSS with custom design system
- **State Management**: React Query for server state
- **Routing**: React Router v6
- **Forms**: React Hook Form with validation
- **Notifications**: React Hot Toast
- **Charts**: Chart.js with React Chart.js 2
- **Icons**: Lucide React

## 📋 Prerequisites

- **Java 21** or higher
- **Node.js 18** or higher
- **MySQL 8.0** or higher
- **Maven 3.6** or higher
- **OpenAI API Key** (for AI features)

## 🛠️ Installation & Setup

### 1. Clone the Repository
```bash
git clone <repository-url>
cd sih
```

### 2. Database Setup
```sql
-- Create database
CREATE DATABASE grievance_portal;
CREATE USER 'grievance_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON grievance_portal.* TO 'grievance_user'@'localhost';
FLUSH PRIVILEGES;
```

### 3. Backend Setup
```bash
cd demo

# Update application.properties with your database credentials
# Set your OpenAI API key
export OPENAI_API_KEY=your_openai_api_key_here

# Build and run
mvn clean install
mvn spring-boot:run
```

### 4. Frontend Setup
```bash
cd frontend

# Install dependencies
npm install

# Start development server
npm start
```

### 5. Environment Configuration

#### Backend Configuration (`demo/src/main/resources/application.properties`)
```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/grievance_portal
spring.datasource.username=grievance_user
spring.datasource.password=your_password

# AI Configuration
spring.ai.openai.api-key=${OPENAI_API_KEY}

# Email Configuration
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
```

#### Frontend Configuration
The frontend is configured to connect to `http://localhost:8080` by default.

## 🎯 Usage

### For Citizens

1. **Submit Grievance**:
   - Visit the portal homepage
   - Click "Submit Grievance"
   - Choose text or voice input
   - Fill in details and attach files
   - Submit and receive tracking number

2. **Track Grievance**:
   - Use the tracking number to check status
   - View real-time updates and comments
   - Receive email notifications

### For Department Officers

1. **Access Dashboard**:
   - Login with officer credentials
   - View assigned grievances
   - Update status and add comments

2. **Manage Grievances**:
   - Review grievance details
   - Update progress status
   - Communicate with citizens

### For Administrators

1. **Admin Dashboard**:
   - View all grievances and statistics
   - Manage departments and users
   - Assign grievances to officers
   - Monitor system performance

## 🔧 API Endpoints

### Grievance Management
- `POST /api/grievances` - Submit new grievance
- `GET /api/grievances/track/{trackingNumber}` - Track grievance
- `PUT /api/grievances/{id}/status` - Update status
- `PUT /api/grievances/{id}/assign` - Assign grievance

### Department Management
- `GET /api/departments` - List all departments
- `POST /api/departments` - Create department
- `PUT /api/departments/{id}` - Update department

### Statistics
- `GET /api/grievances/stats/status/{status}` - Get count by status
- `GET /api/grievances/overdue` - Get overdue grievances

## 🤖 AI Integration

### Categorization Process
1. **Input Processing**: Text/voice input is processed
2. **AI Analysis**: OpenAI GPT analyzes content
3. **Category Assignment**: AI assigns category with confidence score
4. **Auto-assignment**: High-confidence grievances auto-assigned to departments

### Supported Categories
- Infrastructure
- Healthcare
- Education
- Transportation
- Utilities
- Environment
- Safety & Security
- Corruption
- Civil Rights
- Housing
- Employment
- Other

## 🌐 Multilingual Support

### Supported Languages
- English (en)
- Hindi (hi)
- Bengali (bn)
- Telugu (te)
- Marathi (mr)
- Tamil (ta)
- Gujarati (gu)
- Kannada (kn)
- Malayalam (ml)
- Punjabi (pa)

### Voice Input
- Browser-based speech recognition
- WebM audio format support
- Real-time recording with visual feedback
- Audio playback and re-recording options

## 📊 Dashboard Features

### Citizen Dashboard
- Grievance submission form
- Tracking interface
- Status timeline
- File upload support

### Admin Dashboard
- Real-time statistics
- Grievance management
- Department administration
- User management
- Performance analytics

### Analytics
- Status distribution charts
- Monthly trends
- Department performance
- Overdue grievances tracking

## 🔒 Security Features

- **Authentication**: JWT-based authentication
- **Authorization**: Role-based access control
- **CORS**: Configured for cross-origin requests
- **File Validation**: File type and size validation
- **Input Sanitization**: XSS protection
- **SQL Injection**: JPA parameterized queries

## 📱 Responsive Design

- **Mobile-first**: Optimized for mobile devices
- **Tablet Support**: Responsive design for tablets
- **Desktop**: Full-featured desktop experience
- **Touch-friendly**: Optimized for touch interactions

## 🚀 Deployment

### Backend Deployment
```bash
# Build JAR file
mvn clean package

# Run with production profile
java -jar target/citizen-grievance-portal-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### Frontend Deployment
```bash
# Build for production
npm run build

# Serve static files
# Deploy the 'build' folder to your web server
```

### Docker Deployment
```dockerfile
# Backend Dockerfile
FROM openjdk:21-jdk-slim
COPY target/citizen-grievance-portal-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]

# Frontend Dockerfile
FROM node:18-alpine
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build
FROM nginx:alpine
COPY --from=0 /app/build /usr/share/nginx/html
```

## 🧪 Testing

### Backend Testing
```bash
# Run unit tests
mvn test

# Run integration tests
mvn verify
```

### Frontend Testing
```bash
# Run tests
npm test

# Run with coverage
npm run test:coverage
```

## 📈 Performance Optimization

- **Database Indexing**: Optimized queries with proper indexes
- **Caching**: Redis caching for frequently accessed data
- **CDN**: Static asset delivery through CDN
- **Lazy Loading**: Component-based lazy loading
- **Image Optimization**: Compressed images and lazy loading

## 🔧 Configuration

### AI Configuration
```properties
# AI Settings
ai.categorization.enabled=true
ai.categorization.confidence-threshold=0.7
spring.ai.openai.chat.options.temperature=0.3
```

### File Upload Configuration
```properties
# File Settings
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
file.upload-dir=./uploads/
```

## 🐛 Troubleshooting

### Common Issues

1. **Database Connection Error**:
   - Check MySQL service is running
   - Verify database credentials
   - Ensure database exists

2. **AI Categorization Not Working**:
   - Verify OpenAI API key is set
   - Check API quota and billing
   - Review network connectivity

3. **File Upload Issues**:
   - Check file size limits
   - Verify upload directory permissions
   - Review file type restrictions

4. **Email Notifications Not Sending**:
   - Verify SMTP credentials
   - Check email service provider settings
   - Review firewall settings

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new features
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- React team for the powerful frontend library
- OpenAI for AI capabilities
- Tailwind CSS for the utility-first CSS framework
- All contributors and testers

## 📞 Support

For support and questions:
- Create an issue in the repository
- Contact the development team
- Check the documentation wiki

---

**Built with ❤️ for better citizen-government interaction**
