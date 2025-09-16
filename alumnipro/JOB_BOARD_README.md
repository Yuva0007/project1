# Job/Internship Board Feature 🏢

## Overview
The Job/Internship Board is a comprehensive feature that allows alumni to post job openings and internships, students to apply for positions, and administrators to manage and approve job postings. This feature includes application tracking, status management, and a modern, responsive UI.

## Features

### For Alumni 👨‍💼
- **Post Job Openings**: Create detailed job postings with company information, requirements, and application details
- **Manage Postings**: Edit and delete their own job postings
- **View Applications**: See who has applied to their job postings
- **Track Statistics**: Monitor application counts for their postings

### For Students 🎓
- **Browse Jobs**: Search and filter through available job opportunities
- **Apply for Positions**: Submit applications with cover letters, resume URLs, and portfolio links
- **Track Applications**: View the status of their submitted applications
- **Bookmark Jobs**: Save interesting positions for later

### For Administrators 👨‍💻
- **Approve/Reject Jobs**: Review and approve or reject job postings from alumni
- **Manage Applications**: View all applications across the platform
- **Statistics Dashboard**: Monitor job posting and application metrics
- **Content Moderation**: Ensure quality and appropriateness of job postings

## Technical Implementation

### Backend Components

#### Models
- **JobPosting**: Main entity for job postings with fields for title, description, company, location, requirements, etc.
- **Application**: Entity for tracking student applications to job postings

#### Key Features
- **Role-based Access Control**: Different permissions for students, alumni, and admins
- **Status Management**: Job postings can be PENDING, APPROVED, or REJECTED
- **Application Tracking**: Applications can be APPLIED, REVIEWED, ACCEPTED, or REJECTED
- **Search and Filtering**: Advanced search capabilities by company, location, skills, etc.
- **Soft Delete**: Job postings are soft-deleted (status changed to REJECTED)

#### API Endpoints

**Job Postings:**
- `POST /api/job-postings` - Create new job posting (Alumni only)
- `GET /api/job-postings` - Get all approved job postings
- `GET /api/job-postings/{id}` - Get specific job posting
- `PUT /api/job-postings/{id}` - Update job posting (Owner/Admin only)
- `DELETE /api/job-postings/{id}` - Delete job posting (Owner/Admin only)
- `GET /api/job-postings/search?q={term}` - Search job postings
- `GET /api/job-postings/type/{type}` - Filter by job type
- `GET /api/job-postings/company/{company}` - Filter by company
- `GET /api/job-postings/location/{location}` - Filter by location
- `PUT /api/job-postings/{id}/approve` - Approve job posting (Admin only)
- `PUT /api/job-postings/{id}/reject` - Reject job posting (Admin only)
- `GET /api/job-postings/my-postings` - Get user's job postings
- `GET /api/job-postings/admin/all` - Get all job postings (Admin only)
- `GET /api/job-postings/admin/status/{status}` - Get job postings by status (Admin only)

**Applications:**
- `POST /api/applications` - Submit application (Students only)
- `GET /api/applications/{id}` - Get specific application
- `PUT /api/applications/{id}` - Update application (Owner only)
- `DELETE /api/applications/{id}` - Delete application (Owner only)
- `GET /api/applications/my-applications` - Get user's applications
- `GET /api/applications/job-posting/{jobPostingId}` - Get applications for job posting
- `PUT /api/applications/{id}/status` - Update application status
- `GET /api/applications/check/{jobPostingId}` - Check if user has applied
- `GET /api/applications/stats` - Get application statistics
- `GET /api/applications/recent` - Get recent applications (Admin only)

### Frontend Components

#### JobBoard Component
- **Modern UI**: Glassmorphism design with dark theme
- **Responsive Layout**: Works on desktop, tablet, and mobile
- **Advanced Search**: Real-time search with multiple filters
- **Role-based Interface**: Different views for students, alumni, and admins
- **Interactive Modals**: Create job postings and submit applications
- **Status Management**: Visual indicators for job and application statuses

#### Key Features
- **Job Cards**: Attractive cards displaying job information with action buttons
- **Filter System**: Filter by job type, company, location, and status
- **Application Forms**: Comprehensive forms for job applications
- **Statistics Display**: Real-time stats for different user roles
- **Navigation Integration**: Seamlessly integrated into main navigation

## Database Schema

### job_postings Table
```sql
CREATE TABLE job_postings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    job_type ENUM('FULL_TIME', 'PART_TIME', 'INTERNSHIP', 'CONTRACT') NOT NULL,
    company VARCHAR(100) NOT NULL,
    location VARCHAR(100),
    department VARCHAR(50),
    experience_level VARCHAR(100),
    skills VARCHAR(200),
    salary_range VARCHAR(100),
    requirements VARCHAR(500),
    benefits VARCHAR(500),
    application_email VARCHAR(200),
    application_url VARCHAR(200),
    status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    application_deadline TIMESTAMP NOT NULL,
    posted_by BIGINT NOT NULL,
    FOREIGN KEY (posted_by) REFERENCES users(id) ON DELETE CASCADE
);
```

### applications Table
```sql
CREATE TABLE applications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_posting_id BIGINT NOT NULL,
    applicant_id BIGINT NOT NULL,
    status ENUM('APPLIED', 'REVIEWED', 'ACCEPTED', 'REJECTED') NOT NULL DEFAULT 'APPLIED',
    cover_letter TEXT,
    resume_url VARCHAR(200),
    portfolio_url VARCHAR(200),
    additional_notes VARCHAR(500),
    applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP NULL,
    review_notes TEXT,
    FOREIGN KEY (job_posting_id) REFERENCES job_postings(id) ON DELETE CASCADE,
    FOREIGN KEY (applicant_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_application (job_posting_id, applicant_id)
);
```

## Setup Instructions

### 1. Database Setup
Run the SQL script to create the necessary tables and sample data:
```bash
mysql -u your_username -p your_database < setup-job-board.sql
```

### 2. Backend Setup
The backend components are already integrated into the Spring Boot application:
- Models are in `demo/src/main/java/com/example/demo/model/`
- Repositories are in `demo/src/main/java/com/example/demo/repository/`
- Services are in `demo/src/main/java/com/example/demo/service/`
- Controllers are in `demo/src/main/java/com/example/demo/controller/`

### 3. Frontend Setup
The frontend components are integrated into the React application:
- Component: `frontend/src/components/JobBoard.js`
- Styles: `frontend/src/components/JobBoard.css`
- Routing: Added to `frontend/src/App.js`
- Navigation: Updated in Dashboard and AdminDashboard components

## Usage Examples

### For Alumni - Posting a Job
1. Navigate to the Job Board from the dashboard
2. Click "Post Job" button
3. Fill in job details (title, company, description, requirements, etc.)
4. Set application deadline
5. Submit for admin approval

### For Students - Applying for Jobs
1. Browse available job postings
2. Use search and filters to find relevant positions
3. Click "Apply" on desired job
4. Fill in application form with cover letter and resume URL
5. Submit application

### For Administrators - Managing Jobs
1. View all job postings in admin mode
2. Review pending job postings
3. Approve or reject with reasons
4. Monitor application statistics
5. Manage job posting quality

## Security Features

- **Role-based Access**: Different permissions for different user types
- **JWT Authentication**: Secure API access with token validation
- **Input Validation**: Server-side validation for all inputs
- **SQL Injection Protection**: Using JPA/Hibernate for safe database queries
- **XSS Protection**: Proper input sanitization and output encoding

## Performance Optimizations

- **Database Indexing**: Optimized indexes for search and filtering
- **Lazy Loading**: Efficient loading of related entities
- **Pagination**: Ready for pagination implementation
- **Caching**: Can be extended with Redis caching
- **Search Optimization**: Full-text search capabilities

## Future Enhancements

- **Email Notifications**: Notify users of application status changes
- **Resume Upload**: Direct file upload instead of URL
- **Advanced Analytics**: Detailed reporting and analytics
- **Recommendation Engine**: Suggest relevant jobs to students
- **Interview Scheduling**: Built-in interview scheduling system
- **Company Profiles**: Detailed company information pages
- **Salary Insights**: Market salary data and insights
- **Application Tracking**: Detailed application status tracking

## Troubleshooting

### Common Issues

1. **Job posting not appearing**: Check if it's approved by admin
2. **Cannot apply to job**: Verify user role is STUDENT
3. **Permission denied**: Check user authentication and role
4. **Search not working**: Verify search term and filters

### Debug Steps

1. Check browser console for JavaScript errors
2. Verify API endpoints are accessible
3. Check database connection and table structure
4. Validate JWT token and user permissions

## Contributing

When contributing to the Job Board feature:

1. Follow the existing code style and patterns
2. Add proper error handling and validation
3. Include unit tests for new functionality
4. Update documentation for any API changes
5. Test with different user roles and scenarios

## Support

For issues or questions regarding the Job Board feature:
1. Check the troubleshooting section
2. Review the API documentation
3. Test with sample data
4. Contact the development team

---

**Note**: This feature is fully integrated with the existing authentication system and follows the same design patterns as other features in the application.
