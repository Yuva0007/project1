import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../services/api';
import authService from '../services/authService';
import './Profile.css';
import './AdvancedStyles.css';

const Profile = ({ onLogout }) => {
  const [user, setUser] = useState(null);
  const [editMode, setEditMode] = useState(false);
  const [graduationYear, setGraduationYear] = useState('');
  const [currentPosition, setCurrentPosition] = useState('');
  const [company, setCompany] = useState('');
  const [industry, setIndustry] = useState('');
  const [locationField, setLocationField] = useState('');
  const [skills, setSkills] = useState('');
  const [bio, setBio] = useState('');
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    const loadProfile = async () => {
      try {
        const tokenUser = authService.getCurrentUser();
        if (!tokenUser) {
          navigate('/login');
          return;
        }
        const emailOrUsername = tokenUser.email && tokenUser.email !== 'No email provided'
          ? tokenUser.email
          : tokenUser.username;
        if (!emailOrUsername) {
          navigate('/login');
          return;
        }
        const res = await api.get(`/users/email/${encodeURIComponent(emailOrUsername)}`);
        setUser(res.data);
        setGraduationYear(res.data?.alumniProfile?.graduationYear || '');
        setCurrentPosition(res.data?.alumniProfile?.currentPosition || '');
        setCompany(res.data?.alumniProfile?.company || '');
        setIndustry(res.data?.alumniProfile?.industry || '');
        setLocationField(res.data?.alumniProfile?.location || '');
        setSkills(res.data?.alumniProfile?.skills || '');
        setBio(res.data?.alumniProfile?.bio || '');
      } catch (err) {
        console.error('Error fetching profile:', err);
      } finally {
        setLoading(false);
      }
    };
    loadProfile();
  }, [navigate]);

  const handleLogout = () => {
    authService.logout();
    if (onLogout) {
      onLogout();
    }
    navigate('/login', { replace: true });
  };

  if (loading) return <div className="loading">Loading...</div>;

  return (
    <div className="page-container">
      <div className="advanced-header fade-in">
        <div>
          <h1>My Profile</h1>
          <p style={{ color: 'rgba(255,255,255,0.8)', margin: 0 }}>Your personal information and professional details</p>
        </div>
        <nav className="advanced-nav">
          <Link to="/dashboard" className="nav-button">🏠 Dashboard</Link>
          <Link to="/profile" className="nav-button active">👤 Profile</Link>
          <Link to="/messages" className="nav-button">💬 Messages</Link>
          <Link to="/events" className="nav-button">📅 Events</Link>
          <Link to="/jobs" className="nav-button">💼 Jobs</Link>
          {(user?.role || '').toUpperCase() !== 'ADMIN' && (
            <Link to="/connections" className="nav-button">🤝 Connections</Link>
          )}
          <button onClick={handleLogout} className="nav-button logout-button">🚪 Logout</button>
        </nav>
      </div>

      <div className="profile-hero-section fade-in-up">
        <div className="hero-content">
          <div className="profile-avatar-section">
            <div className="avatar-container">
              <img
                src={`https://ui-avatars.com/api/?name=${user?.name || 'User'}&background=random&size=200&bold=true&color=fff`}
                alt="Profile"
                className="profile-avatar"
              />
              <div className="avatar-glow"></div>
            </div>
            <div className="profile-basic-info">
              <h2 className="profile-name">{user?.name}</h2>
              <p className="profile-email">{user?.email}</p>
              <div className={`role-badge ${(user?.role || '').toLowerCase()}`}>
                {user?.role === 'ALUMNI' ? '🎓 Alumni' : user?.role === 'STUDENT' ? '📚 Student' : '👑 Admin'}
              </div>
              {user?.createdAt && (
                <p className="join-date">Member since {new Date(user.createdAt).toLocaleDateString()}</p>
              )}
            </div>
          </div>
          <div className="profile-actions">
            <button className="action-btn edit-btn" onClick={() => navigate('/profile/edit')}>
              <span className="btn-icon">✏️</span>
              <span className="btn-text">Edit Profile</span>
            </button>
          </div>
        </div>
      </div>

      <div className="profile-details-section fade-in-up">
        <div className="section-header">
          <div className="section-icon">📋</div>
          <h3>Profile Details</h3>
          <p>Your complete professional information</p>
        </div>
        
        <div className="details-grid">
          {user?.role === 'ALUMNI' && user?.alumniProfile && (
            <>
              <div className="detail-card">
                <div className="card-header">
                  <div className="field-icon">🎓</div>
                  <div className="field-label">Graduation Year</div>
                </div>
                <div className="field-value">{user.alumniProfile.graduationYear || 'Not specified'}</div>
              </div>
              
              <div className="detail-card">
                <div className="card-header">
                  <div className="field-icon">💼</div>
                  <div className="field-label">Current Position</div>
                </div>
                <div className="field-value">{user.alumniProfile.currentPosition || 'Not specified'}</div>
              </div>
              
              <div className="detail-card">
                <div className="card-header">
                  <div className="field-icon">🏢</div>
                  <div className="field-label">Company</div>
                </div>
                <div className="field-value">{user.alumniProfile.company || 'Not specified'}</div>
              </div>
              
              <div className="detail-card">
                <div className="card-header">
                  <div className="field-icon">🏭</div>
                  <div className="field-label">Industry</div>
                </div>
                <div className="field-value">{user.alumniProfile.industry || 'Not specified'}</div>
              </div>
              
              <div className="detail-card">
                <div className="card-header">
                  <div className="field-icon">📍</div>
                  <div className="field-label">Location</div>
                </div>
                <div className="field-value">{user.alumniProfile.location || 'Not specified'}</div>
              </div>
              
              <div className="detail-card">
                <div className="card-header">
                  <div className="field-icon">🛠️</div>
                  <div className="field-label">Skills</div>
                </div>
                <div className="field-value">{user.alumniProfile.skills || 'Not specified'}</div>
              </div>
            </>
          )}

          {user?.role === 'STUDENT' && user?.studentProfile && (
            <>
              <div className="detail-card">
                <div className="card-header">
                  <div className="field-icon">🏛️</div>
                  <div className="field-label">Department</div>
                </div>
                <div className="field-value">{user.studentProfile.department || 'Not specified'}</div>
              </div>
              
              <div className="detail-card">
                <div className="card-header">
                  <div className="field-icon">📅</div>
                  <div className="field-label">Year of Study</div>
                </div>
                <div className="field-value">{user.studentProfile.yearOfStudy || 'Not specified'}</div>
              </div>
              
              <div className="detail-card">
                <div className="card-header">
                  <div className="field-icon">🆔</div>
                  <div className="field-label">Registration Number</div>
                </div>
                <div className="field-value">{user.studentProfile.registrationNumber || 'Not specified'}</div>
              </div>
            </>
          )}
        </div>

        {(user?.alumniProfile?.bio || user?.studentProfile?.bio) && (
          <div className="bio-section">
            <div className="section-header">
              <div className="section-icon">📝</div>
              <h3>Bio</h3>
              <p>Your personal story and background</p>
            </div>
            <div className="bio-content">
              <p>{user?.alumniProfile?.bio || user?.studentProfile?.bio}</p>
            </div>
          </div>
                 )}
       </div>
     </div>
   );
 };

export default Profile;
