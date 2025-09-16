import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../services/api';
import authService from '../services/authService';
import './AdvancedStyles.css';
import './Dashboard.css';

const AdminDashboard = ({ onLogout }) => {
  const [user, setUser] = useState(null);
  const [totalUsers, setTotalUsers] = useState('—');
  const [alumniCount, setAlumniCount] = useState('—');
  const [studentCount, setStudentCount] = useState('—');
  const navigate = useNavigate();

  useEffect(() => {
    if (!authService.isAuthenticated()) {
      navigate('/login');
      return;
    }
    const currentUser = authService.getCurrentUser();
    setUser(currentUser);
    // Load dynamic stats
    const loadStats = async () => {
      try {
        const res = await api.get('/users/active');
        const users = res.data || [];
        setTotalUsers(users.length);
        setAlumniCount(users.filter(u => (u.role || '').toUpperCase() === 'ALUMNI').length);
        setStudentCount(users.filter(u => (u.role || '').toUpperCase() === 'STUDENT').length);
      } catch (e) {
        console.error('Failed to load user stats', e);
      }
    };
    loadStats();
  }, [navigate]);

  const handleLogout = () => {
    authService.logout();
    if (onLogout) onLogout();
    navigate('/login');
  };

  return (
    <div className="page-container">
      <div className="advanced-header fade-in">
        <div>
          <h1>Admin Dashboard</h1>
          <p style={{ color: 'rgba(255,255,255,0.8)', margin: 0 }}>Monitor and manage the platform</p>
        </div>
        <nav className="advanced-nav">
          <Link to="/dashboard" className="nav-button active">🏠 Dashboard</Link>
          <Link to="/profile" className="nav-button">👤 Profile</Link>
          <Link to="/messages" className="nav-button">💬 Messages</Link>
          <Link to="/events" className="nav-button">📅 Events</Link>
          <Link to="/jobs" className="nav-button">💼 Jobs</Link>
          <button onClick={handleLogout} className="nav-button logout-button">🚪 Logout</button>
        </nav>
      </div>

      <div className="dashboard-hero-section fade-in">
        <div className="hero-content">
          <div className="hero-info">
            <div className="hero-icon">👑</div>
            <div className="hero-text">
              <h2>Platform Overview</h2>
              <p>Monitor and manage the platform</p>
            </div>
          </div>
          <div className="hero-stats">
            <div className="stat-item">
              <div className="stat-number">{totalUsers}</div>
              <div className="stat-label">Total Users</div>
            </div>
            <div className="stat-item">
              <div className="stat-number">{alumniCount}</div>
              <div className="stat-label">Alumni</div>
            </div>
            <div className="stat-item">
              <div className="stat-number">{studentCount}</div>
              <div className="stat-label">Students</div>
            </div>
          </div>
        </div>
      </div>

      <div className="dashboard-actions-section fade-in-up">
        <div className="section-header">
          <div className="section-icon">⚡</div>
          <div>
            <h3>Quick Actions</h3>
            <p>Manage your platform efficiently</p>
          </div>
        </div>
        <div className="actions-grid">
          <div className="action-card" onClick={() => navigate('/admin/users')}>
            <div className="card-header">
              <div className="action-icon">🧑‍💼</div>
              <div className="action-badge">Admin</div>
            </div>
            <div className="card-content">
              <h3 className="action-title">Manage Users</h3>
              <p className="action-description">View and manage registered users</p>
            </div>
            <div className="card-actions">
              <button className="action-btn">
                <span className="btn-icon">→</span>
                <span className="btn-text">Open</span>
              </button>
            </div>
          </div>

          <div className="action-card" onClick={() => navigate('/messages')}>
            <div className="card-header">
              <div className="action-icon">💬</div>
              <div className="action-badge">Monitor</div>
            </div>
            <div className="card-content">
              <h3 className="action-title">Messages</h3>
              <p className="action-description">Review recent conversations</p>
            </div>
            <div className="card-actions">
              <button className="action-btn">
                <span className="btn-icon">→</span>
                <span className="btn-text">Open</span>
              </button>
            </div>
          </div>

          <div className="action-card" onClick={() => navigate('/profile')}>
            <div className="card-header">
              <div className="action-icon">⚙️</div>
              <div className="action-badge">Settings</div>
            </div>
            <div className="card-content">
              <h3 className="action-title">Admin Settings</h3>
              <p className="action-description">Update your admin profile and preferences</p>
            </div>
            <div className="card-actions">
              <button className="action-btn">
                <span className="btn-icon">→</span>
                <span className="btn-text">Open</span>
              </button>
            </div>
          </div>

          <div className="action-card" onClick={() => navigate('/events')}>
            <div className="card-header">
              <div className="action-icon">📅</div>
              <div className="action-badge">Events</div>
            </div>
            <div className="card-content">
              <h3 className="action-title">Manage Events</h3>
              <p className="action-description">Create and manage alumni meetups, workshops, and job fairs</p>
            </div>
            <div className="card-actions">
              <button className="action-btn">
                <span className="btn-icon">→</span>
                <span className="btn-text">Open</span>
              </button>
            </div>
          </div>

          <div className="action-card" onClick={() => navigate('/jobs')}>
            <div className="card-header">
              <div className="action-icon">💼</div>
              <div className="action-badge">Jobs</div>
            </div>
            <div className="card-content">
              <h3 className="action-title">Job Board</h3>
              <p className="action-description">Approve/reject job postings and manage applications</p>
            </div>
            <div className="card-actions">
              <button className="action-btn">
                <span className="btn-icon">→</span>
                <span className="btn-text">Open</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminDashboard;


