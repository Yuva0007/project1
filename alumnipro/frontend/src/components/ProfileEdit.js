import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../services/api';
import authService from '../services/authService';
import './AdvancedStyles.css';
import './ProfileEdit.css';

const ProfileEdit = () => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [graduationYear, setGraduationYear] = useState('');
  const [currentPosition, setCurrentPosition] = useState('');
  const [company, setCompany] = useState('');
  const [industry, setIndustry] = useState('');
  const [locationField, setLocationField] = useState('');
  const [skills, setSkills] = useState('');
  const [bio, setBio] = useState('');
  const [department, setDepartment] = useState('');
  const [yearOfStudy, setYearOfStudy] = useState('');
  const [registrationNumber, setRegistrationNumber] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    const loadUser = async () => {
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
        const u = res.data;
        setUser(u);
        setGraduationYear(u?.alumniProfile?.graduationYear || '');
        setCurrentPosition(u?.alumniProfile?.currentPosition || '');
        setCompany(u?.alumniProfile?.company || '');
        setIndustry(u?.alumniProfile?.industry || '');
        setLocationField(u?.alumniProfile?.location || '');
        setSkills(u?.alumniProfile?.skills || '');
        setBio(u?.alumniProfile?.bio || '');
        setDepartment(u?.studentProfile?.department || '');
        setYearOfStudy(u?.studentProfile?.yearOfStudy ?? '');
        setRegistrationNumber(u?.studentProfile?.registrationNumber || '');
      } catch (err) {
        console.error('Failed to load profile:', err);
      } finally {
        setLoading(false);
      }
    };
    loadUser();
  }, [navigate]);

  const handleSave = async (e) => {
    e.preventDefault();
    if (!user) return;
    try {
      setSaving(true);
      const payload = {
        ...(user.role === 'ALUMNI' ? {
          graduationYear,
          currentPosition,
          company,
          industry,
          location: locationField,
          skills,
          bio,
        } : user.role === 'STUDENT' ? {
          department,
          yearOfStudy,
          registrationNumber,
        } : {}),
      };
      try {
        const resp = await api.put(`/users/${user.id}/profile`, payload);
        if (!(resp && (resp.status === 200 || resp.status === 204))) {
          throw new Error('Unexpected response');
        }
        navigate('/profile');
        return;
      } catch (err1) {
        // Fallback: full update via /users/{id}
        const fullPayload = {
          ...user,
          alumniProfile: user.role === 'ALUMNI' ? {
            ...(user.alumniProfile || {}),
            graduationYear,
            currentPosition,
            company,
            industry,
            location: locationField,
            skills,
            bio,
          } : user.alumniProfile,
          studentProfile: user.role === 'STUDENT' ? {
            ...(user.studentProfile || {}),
            department,
            yearOfStudy,
            registrationNumber,
          } : user.studentProfile,
        };
        const resp2 = await api.put(`/users/${user.id}`, fullPayload);
        if (!(resp2 && (resp2.status === 200 || resp2.status === 204))) {
          throw err1;
        }
        navigate('/profile');
        return;
      }
    } catch (err) {
      console.error('Save failed:', err);
      const msg = err?.response?.data?.message || err?.response?.data?.error || err?.message || 'Failed to save. Please try again.';
      alert(msg);
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <div className="loading">Loading...</div>;

  return (
    <div className="page-container">
      <div className="advanced-header fade-in">
        <div>
          <h1>Edit Profile</h1>
          <p style={{ color: 'rgba(255,255,255,0.8)', margin: 0 }}>Update your contact and background details</p>
        </div>
        <nav className="advanced-nav">
          <Link to="/dashboard" className="nav-button">📊 Dashboard</Link>
          <Link to="/profile" className="nav-button">👤 Profile</Link>
          <Link to="/connections" className="nav-button">🤝 Connections</Link>
          <Link to="/messages" className="nav-button">💬 Messages</Link>
          <Link to="/events" className="nav-button">📅 Events</Link>
          <Link to="/jobs" className="nav-button">💼 Jobs</Link>
        </nav>
      </div>

      <div className="glass-card fade-in edit-card">
        <div className="edit-grid">
          <div className="edit-left">
            <div className="edit-avatar">
              <img src={`https://ui-avatars.com/api/?name=${user?.name || 'User'}&background=random`} alt="avatar" />
            </div>
            <h2 className="edit-name">{user?.name}</h2>
            <p className="edit-email">{user?.email}</p>
            <p className="edit-role">{user?.role}</p>
          </div>
          <form className="edit-form" onSubmit={handleSave}>
            <div className="form-row">
              {user?.role === 'ALUMNI' && (
                <div className="form-group">
                  <label>Graduation Year (Batch)</label>
                  <input type="text" value={graduationYear} onChange={(e) => setGraduationYear(e.target.value)} placeholder="e.g. 2022" />
                </div>
              )}
            </div>

            {user?.role === 'ALUMNI' && (
              <>
                <div className="form-row">
                  <div className="form-group">
                    <label>Current Position</label>
                    <input type="text" value={currentPosition} onChange={(e) => setCurrentPosition(e.target.value)} placeholder="e.g. Software Engineer" />
                  </div>
                  <div className="form-group">
                    <label>Company</label>
                    <input type="text" value={company} onChange={(e) => setCompany(e.target.value)} placeholder="e.g. ACME Corp" />
                  </div>
                </div>
                <div className="form-row">
                  <div className="form-group">
                    <label>Industry</label>
                    <input type="text" value={industry} onChange={(e) => setIndustry(e.target.value)} placeholder="e.g. FinTech" />
                  </div>
                  <div className="form-group">
                    <label>Location</label>
                    <input type="text" value={locationField} onChange={(e) => setLocationField(e.target.value)} placeholder="City, Country" />
                  </div>
                </div>
                <div className="form-row">
                  <div className="form-group">
                    <label>Skills</label>
                    <input type="text" value={skills} onChange={(e) => setSkills(e.target.value)} placeholder="e.g. Java, React, SQL" />
                  </div>
                </div>
                <div className="form-row">
                  <div className="form-group full-width">
                    <label>Bio</label>
                    <textarea rows="4" value={bio} onChange={(e) => setBio(e.target.value)} placeholder="Tell us about yourself" />
                  </div>
                </div>
              </>
            )}

            {user?.role === 'STUDENT' && (
              <>
                <div className="form-row">
                  <div className="form-group">
                    <label>Department / Branch</label>
                    <input type="text" value={department} onChange={(e) => setDepartment(e.target.value)} placeholder="e.g. CSE" />
                  </div>
                  <div className="form-group">
                    <label>Year of Study</label>
                    <input type="number" value={yearOfStudy} onChange={(e) => setYearOfStudy(e.target.value)} placeholder="e.g. 3" />
                  </div>
                </div>
                <div className="form-row">
                  <div className="form-group">
                    <label>Registration Number</label>
                    <input type="text" value={registrationNumber} onChange={(e) => setRegistrationNumber(e.target.value)} placeholder="e.g. 21CS001" />
                  </div>
                </div>
              </>
            )}

            <div className="edit-actions">
              <button type="button" className="btn btn-secondary" onClick={() => navigate('/profile')}>Cancel</button>
              <button type="submit" className="btn btn-primary" disabled={saving}>{saving ? 'Saving...' : 'Save Changes'}</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default ProfileEdit;


