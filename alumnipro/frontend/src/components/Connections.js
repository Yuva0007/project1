import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../services/api';
import authService from '../services/authService';
import './Connections.css';
import './AdvancedStyles.css';

const Connections = ({ onLogout }) => {
  const [connections, setConnections] = useState([]);
  const [pendingRequests, setPendingRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [visibleUsers, setVisibleUsers] = useState([]);
  const [currentUserRole, setCurrentUserRole] = useState('');
  const [showAlumniModal, setShowAlumniModal] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    const user = authService.getCurrentUser();
    const role = (user?.role || '').toUpperCase();
    setCurrentUserRole(role);
    fetchConnections();
    fetchPendingRequests();
    fetchRegisteredByTargetRole(role);
  }, []);

  const fetchConnections = async () => {
    try {
      const response = await api.get('/api/connections');
      setConnections(response.data);
    } catch (error) {
      console.error('Error fetching connections:', error);
    }
  };

  const fetchPendingRequests = async () => {
    try {
      const response = await api.get('/api/connections/pending');
      setPendingRequests(response.data);
    } catch (error) {
      console.error('Error fetching pending requests:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchRegisteredByTargetRole = async (role) => {
    try {
      const targetRole = role === 'ALUMNI' ? 'STUDENT' : role === 'STUDENT' ? 'ALUMNI' : 'ALUMNI';
      let response = await api.get('/users/active');
      let users = response.data || [];
      if (!Array.isArray(users) || users.length === 0) {
        // Fallback to all users if active returns nothing
        response = await api.get('/users');
        users = response.data || [];
      }
      const filtered = users.filter(u => {
        const userRole = (u.role || '').toUpperCase();
        if (targetRole === 'ALUMNI') {
          return userRole === 'ALUMNI' || !!u.alumniProfile;
        }
        if (targetRole === 'STUDENT') {
          return userRole === 'STUDENT' || !!u.studentProfile;
        }
        return false;
      });
      setVisibleUsers(filtered);
    } catch (error) {
      console.error('Error fetching registered users by role:', error);
    }
  };

  const handleAcceptRequest = async (requestId) => {
    try {
      await api.put(`/api/connections/${requestId}/accept`);
      fetchPendingRequests();
      fetchConnections();
    } catch (error) {
      console.error('Error accepting request:', error);
    }
  };

  const handleRejectRequest = async (requestId) => {
    try {
      await api.put(`/api/connections/${requestId}/reject`);
      fetchPendingRequests();
    } catch (error) {
      console.error('Error rejecting request:', error);
    }
  };

  const filteredVisible = visibleUsers.filter(user =>
    user.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    user.email.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const handleMessageClick = (user) => {
    const targetRole = currentUserRole === 'ALUMNI' ? 'STUDENT' : currentUserRole === 'STUDENT' ? 'ALUMNI' : 'ALUMNI';
    navigate('/messages', { state: { prefillRecipientEmail: user.email, prefillRecipientRole: targetRole } });
  };

  const handleLogout = () => {
    authService.logout();
    if (onLogout) {
      onLogout();
    }
    navigate('/login', { replace: true });
  };

  if (loading) return <div className="loading">Loading...</div>;

  const currentUser = authService.getCurrentUser();

  return (
    <div className="page-container">
      <div className="advanced-header fade-in">
        <div>
          <h1>Connections</h1>
          <p style={{ color: 'rgba(255,255,255,0.8)', margin: 0 }}>Manage your professional network</p>
        </div>
        <nav className="advanced-nav">
          <Link to="/dashboard" className="nav-button">🏠 Dashboard</Link>
          <Link to="/profile" className="nav-button">👤 Profile</Link>
          <Link to="/messages" className="nav-button">💬 Messages</Link>
          <Link to="/events" className="nav-button">📅 Events</Link>
          <Link to="/jobs" className="nav-button">💼 Jobs</Link>
          {(currentUser?.role || '').toUpperCase() !== 'ADMIN' && (
            <Link to="/connections" className="nav-button active">🤝 Connections</Link>
          )}
          <button onClick={handleLogout} className="nav-button logout-button">🚪 Logout</button>
        </nav>
      </div>
      
            <div className="connections-hero-section fade-in-up">
        <div className="hero-content">
          <div className="hero-info">
            <div className="hero-icon">🤝</div>
            <div className="hero-text">
              <h2>Professional Network</h2>
              <p>Connect with {currentUserRole === 'ALUMNI' ? 'students' : 'alumni'} and expand your network</p>
            </div>
          </div>
          <div className="hero-stats">
            <div className="stat-item">
              <div className="stat-number">{filteredVisible.length}</div>
              <div className="stat-label">Available</div>
            </div>
            <div className="stat-item">
              <div className="stat-number">{connections.length}</div>
              <div className="stat-label">Connected</div>
            </div>
            <div className="stat-item">
              <div className="stat-number">{pendingRequests.length}</div>
              <div className="stat-label">Pending</div>
            </div>
          </div>
        </div>
      </div>

      <div className="connections-controls-section fade-in-up">
        <div className="search-section">
          <div className="search-input-container">
            <input
              type="text"
              placeholder={`Search registered ${currentUserRole === 'ALUMNI' ? 'students' : 'alumni'}...`}
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="search-input"
            />
            <div className="search-icon">🔍</div>
          </div>
          <button
            className="browse-btn"
            onClick={() => setShowAlumniModal(true)}
          >
            <span className="btn-icon">👥</span>
            <span className="btn-text">{`Browse ${currentUserRole === 'ALUMNI' ? 'Students' : 'Alumni'}`}</span>
          </button>
        </div>
      </div>

      {pendingRequests.length > 0 && (
        <div className="pending-requests-section fade-in-up">
          <div className="section-header">
            <div className="section-icon">⏳</div>
            <h3>Pending Connection Requests</h3>
            <p>Review and respond to connection requests</p>
          </div>
          <div className="requests-grid">
            {pendingRequests.map(request => (
              <div key={request.id} className="request-card">
                <div className="request-header">
                  <div className="request-avatar">
                    <img
                      src={`https://ui-avatars.com/api/?name=${request.senderName}&background=random&size=100&bold=true&color=fff`}
                      alt={request.senderName}
                    />
                  </div>
                  <div className="request-info">
                    <h4 className="request-name">{request.senderName}</h4>
                    <p className="request-email">{request.senderEmail}</p>
                  </div>
                </div>
                <div className="request-message">
                  <p>{request.message}</p>
                </div>
                <div className="request-actions">
                  <button 
                    className="action-btn accept-btn"
                    onClick={() => handleAcceptRequest(request.id)}
                  >
                    <span className="btn-icon">✓</span>
                    <span className="btn-text">Accept</span>
                  </button>
                  <button 
                    className="action-btn reject-btn"
                    onClick={() => handleRejectRequest(request.id)}
                  >
                    <span className="btn-icon">✕</span>
                    <span className="btn-text">Reject</span>
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      <div className="connections-list-section fade-in-up">
        <div className="section-header">
          <div className="section-icon">👥</div>
          <h3>{`Registered ${currentUserRole === 'ALUMNI' ? 'Students' : 'Alumni'}`}</h3>
          <p>{`${filteredVisible.length} ${currentUserRole === 'ALUMNI' ? 'students' : 'alumni'} available to connect with`}</p>
        </div>
        {filteredVisible.length === 0 ? (
          <div className="no-connections">
            <div className="no-connections-content">
              <div className="no-connections-icon">👥</div>
              <h3>No {currentUserRole === 'ALUMNI' ? 'Students' : 'Alumni'} Found</h3>
              <p>{`No ${currentUserRole === 'ALUMNI' ? 'students' : 'alumni'} are currently registered.`}</p>
            </div>
          </div>
        ) : (
          <div className="connections-grid">
            {filteredVisible.map(user => (
              <div key={user.id} className="connection-card" onClick={() => navigate(`/users/${user.id}`)}>
                <div className="card-header">
                  <div className="user-avatar">
                    <img
                      src={`https://ui-avatars.com/api/?name=${user.name}&background=random&size=120&bold=true&color=fff`}
                      alt={user.name}
                    />
                    <div className="avatar-glow"></div>
                  </div>
                  <div className="user-info">
                    <h4 className="user-name">{user.name}</h4>
                    <p className="user-email">{user.email}</p>
                    <div className={`role-badge ${user.role.toLowerCase()}`}>
                      {user.role === 'ALUMNI' ? '🎓 Alumni' : user.role === 'STUDENT' ? '📚 Student' : '👑 Admin'}
                    </div>
                  </div>
                </div>
                <div className="card-actions">
                  <button 
                    className="action-btn message-btn"
                    onClick={(e) => { e.stopPropagation(); handleMessageClick(user); }}
                  >
                    <span className="btn-icon">💬</span>
                    <span className="btn-text">Message</span>
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {showAlumniModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="modal-header">
              <h2>{`Registered ${currentUserRole === 'ALUMNI' ? 'Students' : 'Alumni'}`}</h2>
              <button className="close-btn" onClick={() => setShowAlumniModal(false)}>×</button>
            </div>
            <div className="modal-search">
              <div className="search-input-container">
                <input
                  type="text"
                  placeholder={`Search ${currentUserRole === 'ALUMNI' ? 'students' : 'alumni'} by name or email...`}
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="search-input"
                />
                <div className="search-icon">🔍</div>
              </div>
            </div>
            <div className="modal-users-list">
              {filteredVisible.length === 0 ? (
                <div className="no-results">
                  <div className="no-results-icon">👥</div>
                  <h3>No {currentUserRole === 'ALUMNI' ? 'Students' : 'Alumni'} Found</h3>
                  <p>{`No ${currentUserRole === 'ALUMNI' ? 'students' : 'alumni'} match your search criteria.`}</p>
                </div>
              ) : (
                filteredVisible.map(user => (
                  <div key={user.id} className="modal-user-item">
                    <div className="user-avatar">
                      <img 
                        src={`https://ui-avatars.com/api/?name=${user.name}&background=random&size=80&bold=true&color=fff`} 
                        alt={user.name} 
                      />
                    </div>
                    <div className="user-details">
                      <h4 className="user-name">{user.name}</h4>
                      <p className="user-email">{user.email}</p>
                      <div className={`role-badge ${user.role.toLowerCase()}`}>
                        {user.role === 'ALUMNI' ? '🎓 Alumni' : user.role === 'STUDENT' ? '📚 Student' : '👑 Admin'}
                      </div>
                    </div>
                    <div className="user-actions">
                      <button 
                        className="action-btn message-btn"
                        onClick={() => handleMessageClick(user)}
                      >
                        <span className="btn-icon">💬</span>
                        <span className="btn-text">Message</span>
                      </button>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
             )}
     </div>
   );
};

export default Connections;
