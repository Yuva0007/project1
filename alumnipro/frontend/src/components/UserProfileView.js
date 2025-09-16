import React, { useEffect, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import api from '../services/api';
import authService from '../services/authService';
import './AdvancedStyles.css';
import './UserProfileView.css';

const UserProfileView = () => {
  const { id } = useParams();
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    const load = async () => {
      try {
        const res = await api.get(`/users/${id}`);
        setUser(res.data);
      } catch (e) {
        console.error('Failed to load user', e);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [id]);

  if (loading) return <div className="loading">Loading...</div>;
  if (!user) return (
    <div className="page-container">
      <div className="advanced-header fade-in">
        <div>
          <h1>User Profile</h1>
          <p style={{ color: 'rgba(255,255,255,0.8)', margin: 0 }}>View user information and details</p>
        </div>
        <nav className="advanced-nav">
          <Link to="/connections" className="nav-button">⬅ Back to Connections</Link>
        </nav>
      </div>
      <div className="user-not-found">
        <div className="not-found-content">
          <div className="not-found-icon">👤</div>
          <h3>User Not Found</h3>
          <p>The requested user profile could not be found.</p>
          <Link to="/connections" className="back-btn">Back to Connections</Link>
        </div>
      </div>
    </div>
  );

  const currentUser = authService.getCurrentUser();

  return (
    <div className="page-container upv-container">
      <div className="advanced-header fade-in">
        <div>
          <h1>User Profile</h1>
          <p style={{ color: 'rgba(255,255,255,0.8)', margin: 0 }}>View user information and details</p>
        </div>
        <nav className="advanced-nav">
          <Link to="/dashboard" className="nav-button">🏠 Dashboard</Link>
          <Link to="/profile" className="nav-button">👤 Profile</Link>
          <Link to="/messages" className="nav-button">💬 Messages</Link>
          <Link to="/events" className="nav-button">📅 Events</Link>
          <Link to="/jobs" className="nav-button">💼 Jobs</Link>
          {(currentUser?.role || '').toUpperCase() !== 'ADMIN' && (
            <Link to="/connections" className="nav-button">🤝 Connections</Link>
          )}
          <button onClick={() => {
            authService.logout();
            navigate('/login');
          }} className="nav-button logout-button">🚪 Logout</button>
        </nav>
      </div>

      <div className="profile-hero-section fade-in-up">
        <div className="hero-content">
          <div className="avatar-section">
            <div className="avatar-container">
              <img
                src={`https://ui-avatars.com/api/?name=${encodeURIComponent(user.name)}&background=random&size=200&bold=true&color=fff`}
                alt={user.name}
                className="profile-avatar"
              />
              <div className="avatar-glow"></div>
            </div>
            <div className="user-info">
              <h2 className="user-name">{user.name}</h2>
              <div className="user-email">{user.email}</div>
              <div className={`role-badge ${user.role.toLowerCase()}`}>
                {user.role === 'ALUMNI' ? '🎓 Alumni' : user.role === 'STUDENT' ? '📚 Student' : '👑 Admin'}
              </div>
            </div>
          </div>
          <div className="hero-actions">
            <Link to="/connections" className="action-btn back-btn">
              <span className="btn-icon">⬅</span>
              <span className="btn-text">Back</span>
            </Link>
            <button
              className="action-btn message-btn"
              onClick={() => navigate('/messages', { state: { prefillRecipientEmail: user.email, prefillRecipientRole: user.role } })}
            >
              <span className="btn-icon">💬</span>
              <span className="btn-text">Message</span>
            </button>
          </div>
        </div>
      </div>

      {user.role === 'ALUMNI' && (
        <div className="profile-details-section fade-in-up">
          <div className="section-header">
            <div className="section-icon">🎓</div>
            <h3>Alumni Information</h3>
            <p>Professional background and experience</p>
          </div>
          <div className="details-grid">
            <DetailCard icon="📅" label="Graduation Year" value={user.alumniProfile?.graduationYear} />
            <DetailCard icon="💼" label="Current Position" value={user.alumniProfile?.currentPosition} />
            <DetailCard icon="🏢" label="Company" value={user.alumniProfile?.company} />
            <DetailCard icon="🏭" label="Industry" value={user.alumniProfile?.industry} />
            <DetailCard icon="📍" label="Location" value={user.alumniProfile?.location} />
            <DetailCard icon="🛠️" label="Skills" value={user.alumniProfile?.skills} />
          </div>
          <div className="bio-section">
            <DetailCard icon="📝" label="Bio" value={user.alumniProfile?.bio} full />
          </div>
        </div>
      )}

      {user.role === 'STUDENT' && (
        <div className="profile-details-section fade-in-up">
          <div className="section-header">
            <div className="section-icon">📚</div>
            <h3>Student Information</h3>
            <p>Academic details and current studies</p>
          </div>
          <div className="details-grid">
            <DetailCard icon="🎓" label="Department / Branch" value={user.studentProfile?.department} />
            <DetailCard icon="📖" label="Year of Study" value={user.studentProfile?.yearOfStudy} />
            <DetailCard icon="🆔" label="Registration Number" value={user.studentProfile?.registrationNumber} />
          </div>
        </div>
      )}
    </div>
  );
};

const DetailCard = ({ icon, label, value, full }) => (
  <div className={`detail-card ${full ? 'full-width' : ''}`}>
    <div className="card-header">
      <div className="field-icon">{icon}</div>
      <div className="field-label">{label}</div>
    </div>
    <div className="field-value">{value || '—'}</div>
  </div>
);

export default UserProfileView;


