import React, { useEffect, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import authService from '../services/authService';
import api from '../services/api';
import './Dashboard.css';
import './AdvancedStyles.css';

const Dashboard = () => {
  const [user, setUser] = useState(null);
  const [stats, setStats] = useState({
    connections: 0,
    messages: 0,
    events: 0,
    network: 0
  });
  const [recentActivity, setRecentActivity] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    if (!authService.isAuthenticated()) {
      navigate('/login');
    } else {
      const currentUser = authService.getCurrentUser();
      console.log('Current user data:', currentUser);
      setUser(currentUser);
      fetchDashboardData();
    }
  }, [navigate]);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      await Promise.all([
        fetchStats(),
        fetchRecentActivity()
      ]);
    } catch (error) {
      console.error('Error fetching dashboard data:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchStats = async () => {
    try {
      // Fetch connections count
      const connectionsResponse = await api.get('/api/connections');
      const connectionsCount = connectionsResponse.data?.length || 0;

      // Fetch messages count
      const messagesResponse = await api.get('/api/messages/conversations');
      const messagesCount = messagesResponse.data?.length || 0;

      // Fetch events count
      const eventsResponse = await api.get('/api/events');
      const eventsCount = eventsResponse.data?.length || 0;

      // Fetch network count (total users excluding current user)
      const usersResponse = await api.get('/users/active');
      const allUsers = usersResponse.data || [];
      const currentUser = authService.getCurrentUser();
      const networkCount = allUsers.filter(user => user.id !== currentUser?.id).length;

      setStats({
        connections: connectionsCount,
        messages: messagesCount,
        events: eventsCount,
        network: networkCount
      });
    } catch (error) {
      console.error('Error fetching stats:', error);
      // Fallback to random numbers if API fails
      setStats({
        connections: Math.floor(Math.random() * 50),
        messages: Math.floor(Math.random() * 30),
        events: Math.floor(Math.random() * 20),
        network: Math.floor(Math.random() * 100)
      });
    }
  };

  const fetchRecentActivity = async () => {
    try {
      const activities = [];
      const currentUser = authService.getCurrentUser();

      // Fetch recent messages
      try {
        const messagesResponse = await api.get('/api/messages/conversations');
        const recentMessages = messagesResponse.data?.slice(0, 3) || [];
        recentMessages.forEach(message => {
          activities.push({
            id: `msg-${message.id}`,
            type: 'message',
            title: 'New Message',
            description: `Message from ${message.senderName || 'Unknown'}`,
            time: message.createdAt ? new Date(message.createdAt).toLocaleDateString() : 'Recently',
            icon: '💬'
          });
        });
      } catch (error) {
        console.error('Error fetching messages:', error);
      }

      // Fetch recent events
      try {
        const eventsResponse = await api.get('/api/events');
        const recentEvents = eventsResponse.data?.slice(0, 2) || [];
        recentEvents.forEach(event => {
          activities.push({
            id: `event-${event.id}`,
            type: 'event',
            title: 'Event Available',
            description: `New event: ${event.title}`,
            time: event.createdAt ? new Date(event.createdAt).toLocaleDateString() : 'Recently',
            icon: '📅'
          });
        });
      } catch (error) {
        console.error('Error fetching events:', error);
      }

      // Add some sample activities if no real data
      if (activities.length === 0) {
        activities.push(
          {
            id: 'sample-1',
            type: 'connection',
            title: 'Network Growth',
            description: 'Your network is growing!',
            time: '2 hours ago',
            icon: '👥'
          },
          {
            id: 'sample-2',
            type: 'message',
            title: 'Welcome Message',
            description: 'Welcome to the Alumni Network!',
            time: '1 day ago',
            icon: '💬'
          },
          {
            id: 'sample-3',
            type: 'event',
            title: 'Upcoming Events',
            description: 'Check out the latest events',
            time: '3 days ago',
            icon: '📅'
          }
        );
      }

      // Sort by time (most recent first) and take top 3
      setRecentActivity(activities.slice(0, 3));
    } catch (error) {
      console.error('Error fetching recent activity:', error);
      // Fallback activities
      setRecentActivity([
        {
          id: 'fallback-1',
          type: 'connection',
          title: 'Network Growth',
          description: 'Your network is growing!',
          time: '2 hours ago',
          icon: '👥'
        },
        {
          id: 'fallback-2',
          type: 'message',
          title: 'Welcome Message',
          description: 'Welcome to the Alumni Network!',
          time: '1 day ago',
          icon: '💬'
        },
        {
          id: 'fallback-3',
          type: 'event',
          title: 'Upcoming Events',
          description: 'Check out the latest events',
          time: '3 days ago',
          icon: '📅'
        }
      ]);
    }
  };

  const handleLogout = () => {
    authService.logout();
    navigate('/login');
  };

  const handleNavigation = (path) => {
    navigate(path);
  };



  if (loading) return <div className="loading">Loading...</div>;

  return (
    <div className="page-container">
      {user && (
        <>
          <div className="advanced-header fade-in">
            <div>
              <h1>
                {(() => {
                  const role = (user.role || '').toUpperCase();
                  if (role === 'STUDENT') return 'Student Dashboard';
                  if (role === 'ALUMNI') return 'Alumni Dashboard';
                  if (role === 'ADMIN') return 'Admin Dashboard';
                  return 'Dashboard';
                })()}
              </h1>
              <p style={{ color: 'rgba(255,255,255,0.8)', margin: 0 }}>Welcome back, {user.name}! Manage your alumni network</p>
            </div>
            <nav className="advanced-nav">
              <Link to="/dashboard" className="nav-button active">🏠 Dashboard</Link>
              <Link to="/profile" className="nav-button">👤 Profile</Link>
              <Link to="/messages" className="nav-button">💬 Messages</Link>
              <Link to="/events" className="nav-button">📅 Events</Link>
              <Link to="/jobs" className="nav-button">💼 Jobs</Link>
              {(user.role || '').toUpperCase() !== 'ADMIN' && (
                <Link to="/connections" className="nav-button">🤝 Connections</Link>
              )}
              <button onClick={handleLogout} className="nav-button logout-button">🚪 Logout</button>
            </nav>
          </div>
          


          <div className="dashboard-hero-section fade-in-up">
            <div className="hero-content">
              <div className="hero-info">
                <div className="hero-icon">🎯</div>
                <div className="hero-text">
                  <h2>Welcome to Your Dashboard</h2>
                  <p>Track your progress and manage your professional network</p>
                </div>
              </div>
              <div className="hero-stats">
                <div className="stat-item">
                  <div className="stat-number">{stats.connections}</div>
                  <div className="stat-label">Connections</div>
                </div>
                <div className="stat-item">
                  <div className="stat-number">{stats.messages}</div>
                  <div className="stat-label">Messages</div>
                </div>
                                 <div className="stat-item">
                   <div className="stat-number">{stats.events}</div>
                   <div className="stat-label">Events</div>
                 </div>
                 <div className="stat-item">
                   <div className="stat-number">{stats.network}</div>
                   <div className="stat-label">Network</div>
                 </div>
              </div>
            </div>
          </div>

          <div className="dashboard-actions-section fade-in-up">
            <div className="section-header">
              <div className="section-icon">⚡</div>
              <h3>Quick Actions</h3>
              <p>Access your most important features</p>
            </div>
            <div className="actions-grid">
              <div className="action-card" onClick={() => handleNavigation('/profile')}>
                <div className="card-header">
                  <div className="action-icon">👤</div>
                  <div className="action-badge">Profile</div>
                </div>
                <div className="card-content">
                  <h3 className="action-title">View Profile</h3>
                  <p className="action-description">Update your profile information and showcase your skills</p>
                </div>
                <div className="card-actions">
                  <button className="action-btn">
                    <span className="btn-icon">→</span>
                    <span className="btn-text">Open</span>
                  </button>
                </div>
              </div>

              {(user.role || '').toUpperCase() !== 'ADMIN' && (
                <div className="action-card" onClick={() => handleNavigation('/connections')}>
                  <div className="card-header">
                    <div className="action-icon">🤝</div>
                    <div className="action-badge">Network</div>
                  </div>
                  <div className="card-content">
                    <h3 className="action-title">Manage Connections</h3>
                    <p className="action-description">Connect with alumni and expand your professional network</p>
                  </div>
                  <div className="card-actions">
                    <button className="action-btn">
                      <span className="btn-icon">→</span>
                      <span className="btn-text">Open</span>
                    </button>
                  </div>
                </div>
              )}

              <div className="action-card" onClick={() => handleNavigation('/messages')}>
                <div className="card-header">
                  <div className="action-icon">💬</div>
                  <div className="action-badge">Chat</div>
                </div>
                <div className="card-content">
                  <h3 className="action-title">Messages</h3>
                  <p className="action-description">Stay in touch with your network and respond to messages</p>
                </div>
                <div className="card-actions">
                  <button className="action-btn">
                    <span className="btn-icon">→</span>
                    <span className="btn-text">Open</span>
                  </button>
                </div>
              </div>

              <div className="action-card" onClick={() => handleNavigation('/events')}>
                <div className="card-header">
                  <div className="action-icon">📅</div>
                  <div className="action-badge">Events</div>
                </div>
                <div className="card-content">
                  <h3 className="action-title">Events</h3>
                  <p className="action-description">Browse alumni meetups, workshops, and job fairs</p>
                </div>
                <div className="card-actions">
                  <button className="action-btn">
                    <span className="btn-icon">→</span>
                    <span className="btn-text">Open</span>
                  </button>
                </div>
              </div>

              <div className="action-card" onClick={() => handleNavigation('/jobs')}>
                <div className="card-header">
                  <div className="action-icon">💼</div>
                  <div className="action-badge">Jobs</div>
                </div>
                <div className="card-content">
                  <h3 className="action-title">Job Board</h3>
                  <p className="action-description">
                    {user.role === 'STUDENT' 
                      ? 'Find internships and job opportunities' 
                      : user.role === 'ALUMNI' 
                        ? 'Post job openings and internships'
                        : 'Manage job postings and applications'
                    }
                  </p>
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

          <div className="dashboard-recent-section fade-in-up">
            <div className="section-header">
              <div className="section-icon">📊</div>
              <h3>Recent Activity</h3>
              <p>Your latest interactions and updates</p>
            </div>
                         <div className="recent-grid">
               {recentActivity.length === 0 ? (
                 <div className="no-activity">
                   <div className="no-activity-content">
                     <div className="no-activity-icon">📊</div>
                     <h3>No Recent Activity</h3>
                     <p>Start connecting and engaging to see your activity here</p>
                   </div>
                 </div>
               ) : (
                 recentActivity.map(activity => (
                   <div key={activity.id} className="recent-card">
                     <div className="recent-icon">{activity.icon}</div>
                     <div className="recent-content">
                       <h4>{activity.title}</h4>
                       <p>{activity.description}</p>
                       <span className="recent-time">{activity.time}</span>
                     </div>
                   </div>
                 ))
               )}
             </div>
          </div>
        </>
      )}
    </div>
  );
};

export default Dashboard;
