-- Job Board Database Setup Script
-- This script creates the necessary tables for the Job/Internship Board feature

-- Create job_postings table
CREATE TABLE IF NOT EXISTS job_postings (
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
    FOREIGN KEY (posted_by) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_status (status),
    INDEX idx_job_type (job_type),
    INDEX idx_company (company),
    INDEX idx_location (location),
    INDEX idx_posted_by (posted_by),
    INDEX idx_created_at (created_at),
    INDEX idx_application_deadline (application_deadline)
);

-- Create applications table
CREATE TABLE IF NOT EXISTS applications (
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
    UNIQUE KEY unique_application (job_posting_id, applicant_id),
    INDEX idx_job_posting (job_posting_id),
    INDEX idx_applicant (applicant_id),
    INDEX idx_status (status),
    INDEX idx_applied_at (applied_at)
);

-- Insert sample job postings
INSERT INTO job_postings (
    title, description, job_type, company, location, department, 
    experience_level, skills, salary_range, requirements, benefits, 
    application_email, application_deadline, posted_by, status
) VALUES 
(
    'Software Engineer - Full Stack',
    'We are looking for a talented Full Stack Software Engineer to join our dynamic team. You will be responsible for developing and maintaining web applications using modern technologies.',
    'FULL_TIME',
    'TechCorp Solutions',
    'San Francisco, CA',
    'Engineering',
    'MID',
    'JavaScript, React, Node.js, Python, SQL',
    '$80,000 - $120,000',
    'Bachelor degree in Computer Science or related field, 3+ years of experience in full-stack development, Strong problem-solving skills',
    'Health insurance, 401k matching, Flexible work hours, Professional development budget',
    'careers@techcorp.com',
    DATE_ADD(NOW(), INTERVAL 30 DAY),
    (SELECT id FROM users WHERE role = 'ALUMNI' LIMIT 1),
    'APPROVED'
),
(
    'Marketing Intern',
    'Join our marketing team as an intern and gain hands-on experience in digital marketing, social media management, and campaign development.',
    'INTERNSHIP',
    'Digital Marketing Pro',
    'New York, NY',
    'Marketing',
    'ENTRY',
    'Social Media, Content Creation, Analytics, Communication',
    '$15 - $20 per hour',
    'Currently enrolled in Marketing or Business program, Strong communication skills, Basic knowledge of social media platforms',
    'Mentorship program, Networking opportunities, Flexible schedule, Potential full-time offer',
    'internships@digitalmarketingpro.com',
    DATE_ADD(NOW(), INTERVAL 45 DAY),
    (SELECT id FROM users WHERE role = 'ALUMNI' LIMIT 1),
    'APPROVED'
),
(
    'Data Analyst',
    'We are seeking a Data Analyst to help us make data-driven decisions. You will work with large datasets, create reports, and provide insights to stakeholders.',
    'FULL_TIME',
    'Analytics Inc',
    'Austin, TX',
    'Data Science',
    'MID',
    'Python, SQL, Tableau, Statistics, Machine Learning',
    '$70,000 - $95,000',
    'Bachelor degree in Statistics, Mathematics, or related field, 2+ years of experience in data analysis, Proficiency in Python and SQL',
    'Health insurance, Dental coverage, Vision coverage, 401k, Work from home options',
    'jobs@analyticsinc.com',
    DATE_ADD(NOW(), INTERVAL 60 DAY),
    (SELECT id FROM users WHERE role = 'ALUMNI' LIMIT 1),
    'APPROVED'
),
(
    'Product Manager',
    'Lead product development initiatives and work closely with engineering and design teams to deliver exceptional user experiences.',
    'FULL_TIME',
    'InnovateTech',
    'Seattle, WA',
    'Product',
    'SENIOR',
    'Product Management, Agile, User Research, Strategy, Leadership',
    '$120,000 - $160,000',
    'MBA or equivalent experience, 5+ years of product management experience, Strong leadership and communication skills',
    'Stock options, Health insurance, Unlimited PTO, Professional development, Gym membership',
    'careers@innovatetech.com',
    DATE_ADD(NOW(), INTERVAL 20 DAY),
    (SELECT id FROM users WHERE role = 'ALUMNI' LIMIT 1),
    'PENDING'
),
(
    'UX Designer',
    'Create intuitive and engaging user experiences for our mobile and web applications. Work with cross-functional teams to design user-centered solutions.',
    'FULL_TIME',
    'Design Studio',
    'Los Angeles, CA',
    'Design',
    'MID',
    'Figma, Sketch, Adobe Creative Suite, User Research, Prototyping',
    '$75,000 - $105,000',
    'Bachelor degree in Design or related field, 3+ years of UX design experience, Portfolio demonstrating design skills',
    'Health insurance, Flexible schedule, Design tools budget, Creative workspace',
    'design@designstudio.com',
    DATE_ADD(NOW(), INTERVAL 35 DAY),
    (SELECT id FROM users WHERE role = 'ALUMNI' LIMIT 1),
    'APPROVED'
);

-- Insert sample applications (only if there are students in the database)
INSERT INTO applications (
    job_posting_id, applicant_id, cover_letter, resume_url, additional_notes, status
) 
SELECT 
    jp.id,
    u.id,
    'I am very interested in this position and believe my skills align well with your requirements. I have experience with the technologies mentioned and am eager to contribute to your team.',
    'https://example.com/resume.pdf',
    'Available for immediate start. Can provide additional portfolio samples upon request.',
    'APPLIED'
FROM job_postings jp
CROSS JOIN users u
WHERE jp.status = 'APPROVED' 
  AND u.role = 'STUDENT'
  AND jp.id IN (1, 2, 3, 5)  -- Only apply to approved jobs
LIMIT 10;  -- Limit to 10 applications to avoid too many duplicates

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_job_postings_search ON job_postings(title, company, location);
CREATE INDEX IF NOT EXISTS idx_applications_recent ON applications(applied_at DESC);

-- Add some additional sample data for testing
INSERT INTO job_postings (
    title, description, job_type, company, location, department, 
    experience_level, skills, salary_range, requirements, benefits, 
    application_email, application_deadline, posted_by, status
) VALUES 
(
    'DevOps Engineer',
    'Join our infrastructure team and help us scale our cloud-based systems. You will work with AWS, Docker, Kubernetes, and CI/CD pipelines.',
    'FULL_TIME',
    'CloudTech Solutions',
    'Denver, CO',
    'Engineering',
    'SENIOR',
    'AWS, Docker, Kubernetes, Terraform, Jenkins, Python',
    '$100,000 - $140,000',
    'Bachelor degree in Computer Science, 4+ years of DevOps experience, Strong knowledge of cloud platforms',
    'Health insurance, 401k, Stock options, Remote work options, Learning budget',
    'devops@cloudtech.com',
    DATE_ADD(NOW(), INTERVAL 25 DAY),
    (SELECT id FROM users WHERE role = 'ALUMNI' LIMIT 1),
    'APPROVED'
),
(
    'Sales Representative',
    'Drive sales growth by building relationships with new and existing clients. You will be responsible for meeting sales targets and expanding our customer base.',
    'FULL_TIME',
    'SalesForce Pro',
    'Chicago, IL',
    'Sales',
    'ENTRY',
    'Sales, CRM, Communication, Negotiation, Customer Service',
    '$45,000 - $65,000 + Commission',
    'Bachelor degree preferred, Strong communication skills, Sales experience preferred but not required',
    'Commission structure, Health insurance, Sales training, Career advancement opportunities',
    'sales@salesforcepro.com',
    DATE_ADD(NOW(), INTERVAL 40 DAY),
    (SELECT id FROM users WHERE role = 'ALUMNI' LIMIT 1),
    'APPROVED'
);

-- Update the updated_at timestamp for all job postings to current time
UPDATE job_postings SET updated_at = NOW() WHERE updated_at < NOW();

-- Display summary
SELECT 
    'Job Board Setup Complete' as status,
    (SELECT COUNT(*) FROM job_postings) as total_job_postings,
    (SELECT COUNT(*) FROM job_postings WHERE status = 'APPROVED') as approved_job_postings,
    (SELECT COUNT(*) FROM job_postings WHERE status = 'PENDING') as pending_job_postings,
    (SELECT COUNT(*) FROM applications) as total_applications;
