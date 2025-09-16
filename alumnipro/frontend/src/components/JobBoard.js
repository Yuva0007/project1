import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import authService from '../services/authService';
import './JobBoard.css';

const JobBoard = () => {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [jobPostings, setJobPostings] = useState([]);
  const [filteredJobs, setFilteredJobs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showApplicationModal, setShowApplicationModal] = useState(false);
  const [showAdminModal, setShowAdminModal] = useState(false);
  const [selectedJob, setSelectedJob] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [filterType, setFilterType] = useState('ALL');
  const [filterStatus, setFilterStatus] = useState('APPROVED');
  const [applications, setApplications] = useState([]);
  const [myApplications, setMyApplications] = useState([]);
  const [stats, setStats] = useState({});

  // Form states
  const [jobForm, setJobForm] = useState({
    title: '',
    description: '',
    jobType: 'FULL_TIME',
    company: '',
    location: '',
    department: '',
    experienceLevel: 'ENTRY',
    skills: '',
    salaryRange: '',
    requirements: '',
    benefits: '',
    applicationEmail: '',
    applicationUrl: '',
    applicationDeadline: ''
  });

  const [applicationForm, setApplicationForm] = useState({
    coverLetter: '',
    resumeUrl: '',
    portfolioUrl: '',
    additionalNotes: ''
  });

  useEffect(() => {
    loadUser();
  }, []);

  useEffect(() => {
    if (user) {
      loadJobPostings();
      loadMyApplications();
      loadStats();
    }
  }, [user]);

  useEffect(() => {
    filterJobs();
  }, [jobPostings, searchTerm, filterType, filterStatus]);

  const loadUser = () => {
    try {
      if (!authService.isAuthenticated()) {
        navigate('/login');
        return;
      }
      const currentUser = authService.getCurrentUser();
      setUser(currentUser);
    } catch (error) {
      console.error('Error loading user:', error);
      navigate('/login');
    }
  };

  const loadJobPostings = async () => {
    try {
      setLoading(true);
      let response;
      
      if (user.role === 'ADMIN') {
        if (filterStatus === 'ALL') {
          response = await api.get('/job-postings/admin/all');
        } else {
          response = await api.get(`/job-postings/admin/status/${filterStatus}`);
        }
      } else {
        response = await api.get('/job-postings');
      }
      
      setJobPostings(response.data || []);
    } catch (error) {
      console.error('Error loading job postings:', error);
      // Set empty array if API fails (e.g., table doesn't exist yet)
      setJobPostings([]);
    } finally {
      setLoading(false);
    }
  };

  const loadMyApplications = async () => {
    if (user.role === 'STUDENT') {
      try {
        const response = await api.get('/applications/my-applications');
        setMyApplications(response.data || []);
      } catch (error) {
        console.error('Error loading my applications:', error);
        setMyApplications([]);
      }
    }
  };

  const loadStats = async () => {
    try {
      const response = await api.get('/job-postings/stats');
      setStats(response.data || {});
    } catch (error) {
      console.error('Error loading stats:', error);
      setStats({});
    }
  };

  const filterJobs = () => {
    let filtered = jobPostings;

    // Filter by search term
    if (searchTerm) {
      filtered = filtered.filter(job =>
        job.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
        job.company.toLowerCase().includes(searchTerm.toLowerCase()) ||
        job.location.toLowerCase().includes(searchTerm.toLowerCase()) ||
        (job.skills && job.skills.toLowerCase().includes(searchTerm.toLowerCase()))
      );
    }

    // Filter by job type
    if (filterType !== 'ALL') {
      filtered = filtered.filter(job => job.jobType === filterType);
    }

    setFilteredJobs(filtered);
  };

  const handleCreateJob = async (e) => {
    e.preventDefault();
    try {
      await api.post('/job-postings', jobForm);
      setShowCreateModal(false);
      setJobForm({
        title: '',
        description: '',
        jobType: 'FULL_TIME',
        company: '',
        location: '',
        department: '',
        experienceLevel: 'ENTRY',
        skills: '',
        salaryRange: '',
        requirements: '',
        benefits: '',
        applicationEmail: '',
        applicationUrl: '',
        applicationDeadline: ''
      });
      loadJobPostings();
    } catch (error) {
      console.error('Error creating job posting:', error);
      alert('Failed to create job posting. Please try again.');
    }
  };

  const handleApply = async (e) => {
    e.preventDefault();
    try {
      await api.post('/applications', {
        ...applicationForm,
        jobPosting: selectedJob
      });
      setShowApplicationModal(false);
      setApplicationForm({
        coverLetter: '',
        resumeUrl: '',
        portfolioUrl: '',
        additionalNotes: ''
      });
      loadMyApplications();
      alert('Application submitted successfully!');
    } catch (error) {
      console.error('Error submitting application:', error);
      alert('Failed to submit application. Please try again.');
    }
  };

  const handleApproveJob = async (jobId) => {
    try {
      await api.put(`/job-postings/${jobId}/approve`);
      loadJobPostings();
    } catch (error) {
      console.error('Error approving job:', error);
      alert('Failed to approve job posting.');
    }
  };

  const handleRejectJob = async (jobId) => {
    const reason = prompt('Please provide a reason for rejection:');
    if (reason) {
      try {
        await api.put(`/job-postings/${jobId}/reject`, { reason });
        loadJobPostings();
      } catch (error) {
        console.error('Error rejecting job:', error);
        alert('Failed to reject job posting.');
      }
    }
  };

  const handleDeleteJob = async (jobId) => {
    if (window.confirm('Are you sure you want to delete this job posting?')) {
      try {
        await api.delete(`/job-postings/${jobId}`);
        loadJobPostings();
      } catch (error) {
        console.error('Error deleting job:', error);
        alert('Failed to delete job posting.');
      }
    }
  };

  const openApplicationModal = (job) => {
    setSelectedJob(job);
    setShowApplicationModal(true);
  };

  const openAdminModal = (job) => {
    setSelectedJob(job);
    setShowAdminModal(true);
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  };

  const isJobExpired = (deadline) => {
    return new Date(deadline) < new Date();
  };

  const getJobTypeColor = (jobType) => {
    const colors = {
      'FULL_TIME': '#10B981',
      'PART_TIME': '#3B82F6',
      'INTERNSHIP': '#F59E0B',
      'CONTRACT': '#8B5CF6'
    };
    return colors[jobType] || '#6B7280';
  };

  const getStatusColor = (status) => {
    const colors = {
      'APPROVED': '#10B981',
      'PENDING': '#F59E0B',
      'REJECTED': '#EF4444'
    };
    return colors[status] || '#6B7280';
  };

  if (loading) {
    return (
      <div className="page-container">
        <div className="loading">Loading job board...</div>
      </div>
    );
  }

  return (
    <div className="page-container">
      {/* Header */}
      <div className="advanced-header fade-in">
        <div>
          <h1>Job Board</h1>
          <p style={{ color: 'rgba(255,255,255,0.8)', margin: 0 }}>Find your next opportunity, {user?.name}!</p>
        </div>
        <nav className="advanced-nav">
          <button 
            className="nav-button"
            onClick={() => navigate('/dashboard')}
          >
            🏠 Dashboard
          </button>
          <button 
            className="nav-button"
            onClick={() => navigate('/profile')}
          >
            👤 Profile
          </button>
          <button 
            className="nav-button"
            onClick={() => navigate('/messages')}
          >
            💬 Messages
          </button>
          <button 
            className="nav-button"
            onClick={() => navigate('/events')}
          >
            📅 Events
          </button>
          <button 
            className="nav-button active"
            onClick={() => navigate('/jobs')}
          >
            💼 Jobs
          </button>
          {user?.role !== 'ADMIN' && (
            <button 
              className="nav-button"
              onClick={() => navigate('/connections')}
            >
              🤝 Connections
            </button>
          )}
          <button 
            className="nav-button logout-button"
            onClick={() => {
              localStorage.removeItem('token');
              navigate('/login');
            }}
          >
            🚪 Logout
          </button>
        </nav>
      </div>

      {/* Hero Section */}
      <div className="job-board-hero">
        <div className="hero-content">
          <div className="hero-info">
            <div className="hero-icon">💼</div>
            <div className="hero-text">
              <h2>Job & Internship Board</h2>
              <p>Discover amazing opportunities posted by our alumni network</p>
            </div>
          </div>
          <div className="hero-stats">
            <div className="stat-item">
              <div className="stat-number">{stats.total || 0}</div>
              <div className="stat-label">Total Jobs</div>
            </div>
            {user.role === 'STUDENT' && (
              <div className="stat-item">
                <div className="stat-number">{myApplications.length}</div>
                <div className="stat-label">My Applications</div>
              </div>
            )}
            {user.role === 'ALUMNI' && (
              <div className="stat-item">
                <div className="stat-number">{stats.myPostings || 0}</div>
                <div className="stat-label">My Postings</div>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Controls Section */}
      <div className="job-board-controls">
        <div className="controls-left">
          <div className="search-section">
            <div className="search-input-container">
              <input
                type="text"
                placeholder="Search jobs, companies, locations..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="search-input"
              />
              <span className="search-icon">🔍</span>
            </div>
          </div>
        </div>
        <div className="controls-right">
          <select
            value={filterType}
            onChange={(e) => setFilterType(e.target.value)}
            className="filter-select"
          >
            <option value="ALL">All Types</option>
            <option value="FULL_TIME">Full Time</option>
            <option value="PART_TIME">Part Time</option>
            <option value="INTERNSHIP">Internship</option>
            <option value="CONTRACT">Contract</option>
          </select>
          
          {user.role === 'ADMIN' && (
            <select
              value={filterStatus}
              onChange={(e) => setFilterStatus(e.target.value)}
              className="filter-select"
            >
              <option value="ALL">All Status</option>
              <option value="APPROVED">Approved</option>
              <option value="PENDING">Pending</option>
              <option value="REJECTED">Rejected</option>
            </select>
          )}
          
          {user.role === 'ALUMNI' && (
            <button
              className="create-job-btn"
              onClick={() => setShowCreateModal(true)}
            >
              <span className="btn-icon">➕</span>
              <span className="btn-text">Post Job</span>
            </button>
          )}
        </div>
      </div>

      {/* Jobs Grid */}
      <div className="jobs-grid">
        {filteredJobs.length === 0 ? (
          <div className="no-jobs">
            <div className="no-jobs-content">
              <div className="no-jobs-icon">💼</div>
              <h3>No jobs found</h3>
              <p>Try adjusting your search criteria or check back later for new opportunities.</p>
              {user.role === 'ALUMNI' && (
                <button
                  className="create-first-job-btn"
                  onClick={() => setShowCreateModal(true)}
                >
                  <span className="btn-icon">➕</span>
                  <span className="btn-text">Post First Job</span>
                </button>
              )}
            </div>
          </div>
        ) : (
          filteredJobs.map((job) => (
            <div key={job.id} className="job-card">
              <div className="job-card-header">
                <div className="job-type-section">
                  <span 
                    className="job-type-badge"
                    style={{ backgroundColor: getJobTypeColor(job.jobType) }}
                  >
                    {job.jobType.replace('_', ' ')}
                  </span>
                  {user.role === 'ADMIN' && (
                    <span 
                      className="status-badge"
                      style={{ backgroundColor: getStatusColor(job.status) }}
                    >
                      {job.status}
                    </span>
                  )}
                </div>
                <div className="job-date-badge">
                  <div className="date-day">{formatDate(job.applicationDeadline).split(' ')[1]}</div>
                  <div className="date-month">{formatDate(job.applicationDeadline).split(' ')[0]}</div>
                </div>
              </div>
              
              <div className="job-content">
                <h3 className="job-title">{job.title}</h3>
                <p className="job-company">{job.company}</p>
                <p className="job-location">📍 {job.location}</p>
                
                <div className="job-details">
                  {job.salaryRange && (
                    <div className="job-detail">
                      <span className="detail-icon">💰</span>
                      <span className="detail-text">{job.salaryRange}</span>
                    </div>
                  )}
                  {job.experienceLevel && (
                    <div className="job-detail">
                      <span className="detail-icon">🎯</span>
                      <span className="detail-text">{job.experienceLevel}</span>
                    </div>
                  )}
                  {job.department && (
                    <div className="job-detail">
                      <span className="detail-icon">🏢</span>
                      <span className="detail-text">{job.department}</span>
                    </div>
                  )}
                </div>
                
                <p className="job-description">
                  {job.description && job.description.length > 150 
                    ? job.description.substring(0, 150) + '...'
                    : job.description
                  }
                </p>
                
                {job.skills && (
                  <div className="job-skills">
                    {job.skills.split(',').slice(0, 3).map((skill, index) => (
                      <span key={index} className="skill-tag">
                        {skill.trim()}
                      </span>
                    ))}
                    {job.skills.split(',').length > 3 && (
                      <span className="skill-tag more">+{job.skills.split(',').length - 3} more</span>
                    )}
                  </div>
                )}
              </div>
              
              <div className="job-actions">
                {user.role === 'STUDENT' && !isJobExpired(job.applicationDeadline) && (
                  <button
                    className="action-btn apply-btn"
                    onClick={() => openApplicationModal(job)}
                  >
                    <span className="btn-icon">📝</span>
                    <span className="btn-text">Apply</span>
                  </button>
                )}
                
                {user.role === 'ADMIN' && (
                  <>
                    {job.status === 'PENDING' && (
                      <>
                        <button
                          className="action-btn approve-btn"
                          onClick={() => handleApproveJob(job.id)}
                        >
                          <span className="btn-icon">✅</span>
                          <span className="btn-text">Approve</span>
                        </button>
                        <button
                          className="action-btn reject-btn"
                          onClick={() => handleRejectJob(job.id)}
                        >
                          <span className="btn-icon">❌</span>
                          <span className="btn-text">Reject</span>
                        </button>
                      </>
                    )}
                    <button
                      className="action-btn delete-btn"
                      onClick={() => handleDeleteJob(job.id)}
                    >
                      <span className="btn-icon">🗑️</span>
                      <span className="btn-text">Delete</span>
                    </button>
                  </>
                )}
                
                {(user.role === 'ALUMNI' && job.postedBy.id === user.id) && (
                  <button
                    className="action-btn edit-btn"
                    onClick={() => openAdminModal(job)}
                  >
                    <span className="btn-icon">✏️</span>
                    <span className="btn-text">Edit</span>
                  </button>
                )}
              </div>
            </div>
          ))
        )}
      </div>

      {/* Create Job Modal */}
      {showCreateModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="modal-header">
              <h2>Post New Job</h2>
              <button 
                className="close-btn"
                onClick={() => setShowCreateModal(false)}
              >
                ✕
              </button>
            </div>
            <form onSubmit={handleCreateJob} className="job-form">
              <div className="form-row">
                <div className="form-group">
                  <label>Job Title *</label>
                  <input
                    type="text"
                    value={jobForm.title}
                    onChange={(e) => setJobForm({...jobForm, title: e.target.value})}
                    required
                  />
                </div>
                <div className="form-group">
                  <label>Company *</label>
                  <input
                    type="text"
                    value={jobForm.company}
                    onChange={(e) => setJobForm({...jobForm, company: e.target.value})}
                    required
                  />
                </div>
              </div>
              
              <div className="form-group">
                <label>Description *</label>
                <textarea
                  value={jobForm.description}
                  onChange={(e) => setJobForm({...jobForm, description: e.target.value})}
                  rows="4"
                  required
                />
              </div>
              
              <div className="form-row">
                <div className="form-group">
                  <label>Job Type *</label>
                  <select
                    value={jobForm.jobType}
                    onChange={(e) => setJobForm({...jobForm, jobType: e.target.value})}
                    required
                  >
                    <option value="FULL_TIME">Full Time</option>
                    <option value="PART_TIME">Part Time</option>
                    <option value="INTERNSHIP">Internship</option>
                    <option value="CONTRACT">Contract</option>
                  </select>
                </div>
                <div className="form-group">
                  <label>Location</label>
                  <input
                    type="text"
                    value={jobForm.location}
                    onChange={(e) => setJobForm({...jobForm, location: e.target.value})}
                  />
                </div>
              </div>
              
              <div className="form-row">
                <div className="form-group">
                  <label>Department</label>
                  <input
                    type="text"
                    value={jobForm.department}
                    onChange={(e) => setJobForm({...jobForm, department: e.target.value})}
                  />
                </div>
                <div className="form-group">
                  <label>Experience Level</label>
                  <select
                    value={jobForm.experienceLevel}
                    onChange={(e) => setJobForm({...jobForm, experienceLevel: e.target.value})}
                  >
                    <option value="ENTRY">Entry Level</option>
                    <option value="MID">Mid Level</option>
                    <option value="SENIOR">Senior Level</option>
                    <option value="EXECUTIVE">Executive</option>
                  </select>
                </div>
              </div>
              
              <div className="form-group">
                <label>Skills (comma-separated)</label>
                <input
                  type="text"
                  value={jobForm.skills}
                  onChange={(e) => setJobForm({...jobForm, skills: e.target.value})}
                  placeholder="JavaScript, React, Python, SQL"
                />
              </div>
              
              <div className="form-row">
                <div className="form-group">
                  <label>Salary Range</label>
                  <input
                    type="text"
                    value={jobForm.salaryRange}
                    onChange={(e) => setJobForm({...jobForm, salaryRange: e.target.value})}
                    placeholder="$50,000 - $70,000"
                  />
                </div>
                <div className="form-group">
                  <label>Application Deadline *</label>
                  <input
                    type="datetime-local"
                    value={jobForm.applicationDeadline}
                    onChange={(e) => setJobForm({...jobForm, applicationDeadline: e.target.value})}
                    required
                  />
                </div>
              </div>
              
              <div className="form-group">
                <label>Requirements</label>
                <textarea
                  value={jobForm.requirements}
                  onChange={(e) => setJobForm({...jobForm, requirements: e.target.value})}
                  rows="3"
                />
              </div>
              
              <div className="form-group">
                <label>Benefits</label>
                <textarea
                  value={jobForm.benefits}
                  onChange={(e) => setJobForm({...jobForm, benefits: e.target.value})}
                  rows="3"
                />
              </div>
              
              <div className="form-row">
                <div className="form-group">
                  <label>Application Email</label>
                  <input
                    type="email"
                    value={jobForm.applicationEmail}
                    onChange={(e) => setJobForm({...jobForm, applicationEmail: e.target.value})}
                  />
                </div>
                <div className="form-group">
                  <label>Application URL</label>
                  <input
                    type="url"
                    value={jobForm.applicationUrl}
                    onChange={(e) => setJobForm({...jobForm, applicationUrl: e.target.value})}
                  />
                </div>
              </div>
              
              <div className="form-actions">
                <button type="button" onClick={() => setShowCreateModal(false)}>
                  Cancel
                </button>
                <button type="submit" className="submit-btn">
                  Post Job
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Application Modal */}
      {showApplicationModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="modal-header">
              <h2>Apply for {selectedJob?.title}</h2>
              <button 
                className="close-btn"
                onClick={() => setShowApplicationModal(false)}
              >
                ✕
              </button>
            </div>
            <form onSubmit={handleApply} className="application-form">
              <div className="form-group">
                <label>Cover Letter *</label>
                <textarea
                  value={applicationForm.coverLetter}
                  onChange={(e) => setApplicationForm({...applicationForm, coverLetter: e.target.value})}
                  rows="6"
                  required
                  placeholder="Tell us why you're interested in this position and how your skills align with the requirements..."
                />
              </div>
              
              <div className="form-group">
                <label>Resume URL</label>
                <input
                  type="url"
                  value={applicationForm.resumeUrl}
                  onChange={(e) => setApplicationForm({...applicationForm, resumeUrl: e.target.value})}
                  placeholder="https://example.com/resume.pdf"
                />
              </div>
              
              <div className="form-group">
                <label>Portfolio URL</label>
                <input
                  type="url"
                  value={applicationForm.portfolioUrl}
                  onChange={(e) => setApplicationForm({...applicationForm, portfolioUrl: e.target.value})}
                  placeholder="https://example.com/portfolio"
                />
              </div>
              
              <div className="form-group">
                <label>Additional Notes</label>
                <textarea
                  value={applicationForm.additionalNotes}
                  onChange={(e) => setApplicationForm({...applicationForm, additionalNotes: e.target.value})}
                  rows="3"
                  placeholder="Any additional information you'd like to share..."
                />
              </div>
              
              <div className="form-actions">
                <button type="button" onClick={() => setShowApplicationModal(false)}>
                  Cancel
                </button>
                <button type="submit" className="submit-btn">
                  Submit Application
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default JobBoard;
