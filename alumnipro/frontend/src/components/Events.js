import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import axios from 'axios';
import authService from '../services/authService';
import './Events.css';
import './AdvancedStyles.css';

const Events = () => {
  const [events, setEvents] = useState([]);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [editingEvent, setEditingEvent] = useState(null);
  const [loading, setLoading] = useState(true);
  const [filterType, setFilterType] = useState('ALL');
  const [currentUser, setCurrentUser] = useState(null);
  const [userInterests, setUserInterests] = useState(new Set());

  const navigate = useNavigate();

  // Form state
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    eventType: 'ALUMNI_MEETUP',
    eventDate: '',
    eventTime: '',
    location: '',
    organizer: ''
  });

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token) {
      const payload = JSON.parse(atob(token.split('.')[1]));
      setCurrentUser({
        email: payload.email || payload.username,
        role: payload.role
      });
      fetchEvents();
      fetchUserInterests();
    }
  }, []);

  const fetchEvents = async () => {
    try {
      const response = await axios.get('/events');
      setEvents(response.data);
      setLoading(false);
    } catch (error) {
      console.error('Error fetching events:', error);
      setLoading(false);
    }
  };

  const fetchUserInterests = async () => {
    try {
      const token = localStorage.getItem('token');
      if (token) {
        const interests = new Set();
        for (const event of events) {
          const response = await axios.get(`/events/${event.id}/interest`, {
            headers: { Authorization: `Bearer ${token}` }
          });
          if (response.data) {
            interests.add(event.id);
          }
        }
        setUserInterests(interests);
      }
    } catch (error) {
      console.error('Error fetching user interests:', error);
    }
  };

  const handleCreateEvent = async (e) => {
    e.preventDefault();
    try {
      const token = localStorage.getItem('token');
      const eventDateTime = new Date(`${formData.eventDate}T${formData.eventTime}`);
      
      const eventData = {
        ...formData,
        eventDate: eventDateTime.toISOString()
      };

      await axios.post('/events', eventData, {
        headers: { Authorization: `Bearer ${token}` }
      });

      setShowCreateModal(false);
      setFormData({
        title: '',
        description: '',
        eventType: 'ALUMNI_MEETUP',
        eventDate: '',
        eventTime: '',
        location: '',
        organizer: ''
      });
      fetchEvents();
    } catch (error) {
      console.error('Error creating event:', error);
      alert('Failed to create event. Please try again.');
    }
  };

  const handleEditEvent = async (e) => {
    e.preventDefault();
    try {
      const token = localStorage.getItem('token');
      const eventDateTime = new Date(`${formData.eventDate}T${formData.eventTime}`);
      
      const eventData = {
        ...formData,
        eventDate: eventDateTime.toISOString()
      };

      await axios.put(`/events/${editingEvent.id}`, eventData, {
        headers: { Authorization: `Bearer ${token}` }
      });

      setShowEditModal(false);
      setEditingEvent(null);
      setFormData({
        title: '',
        description: '',
        eventType: 'ALUMNI_MEETUP',
        eventDate: '',
        eventTime: '',
        location: '',
        organizer: ''
      });
      fetchEvents();
    } catch (error) {
      console.error('Error updating event:', error);
      alert('Failed to update event. Please try again.');
    }
  };

  const handleDeleteEvent = async (eventId) => {
    if (window.confirm('Are you sure you want to delete this event?')) {
      try {
        const token = localStorage.getItem('token');
        await axios.delete(`/events/${eventId}`, {
          headers: { Authorization: `Bearer ${token}` }
        });
        fetchEvents();
      } catch (error) {
        console.error('Error deleting event:', error);
        alert('Failed to delete event. Please try again.');
      }
    }
  };

  const handleInterestToggle = async (eventId) => {
    try {
      const token = localStorage.getItem('token');
      const isInterested = userInterests.has(eventId);

      if (isInterested) {
        await axios.delete(`/events/${eventId}/interest`, {
          headers: { Authorization: `Bearer ${token}` }
        });
        setUserInterests(prev => {
          const newSet = new Set(prev);
          newSet.delete(eventId);
          return newSet;
        });
      } else {
        await axios.post(`/events/${eventId}/interest`, {}, {
          headers: { Authorization: `Bearer ${token}` }
        });
        setUserInterests(prev => new Set(prev).add(eventId));
      }
    } catch (error) {
      console.error('Error toggling interest:', error);
      alert('Failed to update interest. Please try again.');
    }
  };

  const openEditModal = (event) => {
    const eventDate = new Date(event.eventDate);
    setFormData({
      title: event.title,
      description: event.description,
      eventType: event.eventType,
      eventDate: eventDate.toISOString().split('T')[0],
      eventTime: eventDate.toTimeString().slice(0, 5),
      location: event.location,
      organizer: event.organizer
    });
    setEditingEvent(event);
    setShowEditModal(true);
  };

  const filteredEvents = events.filter(event => {
    if (filterType === 'ALL') return true;
    return event.eventType === filterType;
  });

  const formatEventDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const getEventTypeColor = (eventType) => {
    switch (eventType) {
      case 'ALUMNI_MEETUP': return '#10B981';
      case 'WORKSHOP': return '#3B82F6';
      case 'JOB_FAIR': return '#F59E0B';
      default: return '#6B7280';
    }
  };

  if (loading) {
    return <div className="events-loading">Loading events...</div>;
  }

  return (
    <div className="page-container">
      <div className="advanced-header fade-in">
        <div>
          <h1>Events</h1>
          <p style={{ color: 'rgba(255,255,255,0.8)', margin: 0 }}>Stay updated with alumni meetups, workshops, and job fairs</p>
        </div>
        <nav className="advanced-nav">
          <Link to="/dashboard" className="nav-button">🏠 Dashboard</Link>
          <Link to="/profile" className="nav-button">👤 Profile</Link>
          <Link to="/messages" className="nav-button">💬 Messages</Link>
          <Link to="/events" className="nav-button active">📅 Events</Link>
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

      <div className="events-controls fade-in-up">
        <div className="events-filters">
          <select 
            value={filterType} 
            onChange={(e) => setFilterType(e.target.value)}
            className="filter-select"
          >
            <option value="ALL">All Events</option>
            <option value="ALUMNI_MEETUP">Alumni Meetups</option>
            <option value="WORKSHOP">Workshops</option>
            <option value="JOB_FAIR">Job Fairs</option>
          </select>
        </div>
        
        {currentUser?.role === 'ADMIN' && (
          <button 
            className="create-event-btn"
            onClick={() => setShowCreateModal(true)}
          >
            <span className="btn-icon">+</span>
            <span className="btn-text">Create New Event</span>
          </button>
        )}
      </div>

      <div className="events-grid fade-in-up">
        {filteredEvents.length === 0 ? (
          <div className="no-events">
            <div className="no-events-content">
              <div className="no-events-icon">📅</div>
              <h3>No Events Found</h3>
              <p>No events found for the selected category.</p>
              {currentUser?.role === 'ADMIN' && (
                <button 
                  className="create-first-event-btn"
                  onClick={() => setShowCreateModal(true)}
                >
                  Create Your First Event
                </button>
              )}
            </div>
          </div>
        ) : (
          filteredEvents.map(event => (
            <div key={event.id} className="event-card">
              <div className="event-card-header">
                <div className="event-type-section">
                  <span 
                    className="event-type-badge"
                    style={{ backgroundColor: getEventTypeColor(event.eventType) }}
                  >
                    {event.eventType.replace('_', ' ')}
                  </span>
                  <div className="event-date-badge">
                    <span className="date-day">{new Date(event.eventDate).getDate()}</span>
                    <span className="date-month">{new Date(event.eventDate).toLocaleDateString('en-US', { month: 'short' })}</span>
                  </div>
                </div>
                {currentUser?.role === 'ADMIN' && (
                  <div className="event-actions">
                    <button 
                      className="action-btn edit-btn"
                      onClick={() => openEditModal(event)}
                      title="Edit Event"
                    >
                      ✏️
                    </button>
                    <button 
                      className="action-btn delete-btn"
                      onClick={() => handleDeleteEvent(event.id)}
                      title="Delete Event"
                    >
                      🗑️
                    </button>
                  </div>
                )}
              </div>

              <div className="event-content">
                <h3 className="event-title">{event.title}</h3>
                <p className="event-description">{event.description}</p>
                
                <div className="event-details">
                  <div className="event-detail">
                    <span className="detail-icon">📅</span>
                    <span className="detail-text">{formatEventDate(event.eventDate)}</span>
                  </div>
                  <div className="event-detail">
                    <span className="detail-icon">📍</span>
                    <span className="detail-text">{event.location}</span>
                  </div>
                  <div className="event-detail">
                    <span className="detail-icon">👤</span>
                    <span className="detail-text">{event.organizer}</span>
                  </div>
                </div>

                {currentUser?.role !== 'ADMIN' && (
                  <button
                    className={`interest-btn ${userInterests.has(event.id) ? 'interested' : ''}`}
                    onClick={() => handleInterestToggle(event.id)}
                  >
                    <span className="btn-icon">
                      {userInterests.has(event.id) ? '✓' : '+'}
                    </span>
                    <span className="btn-text">
                      {userInterests.has(event.id) ? 'Interested' : 'Show Interest'}
                    </span>
                  </button>
                )}
              </div>
            </div>
          ))
        )}
      </div>

      {/* Create Event Modal */}
      {showCreateModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="modal-header">
              <h2>Create New Event</h2>
              <button 
                className="close-btn"
                onClick={() => setShowCreateModal(false)}
              >
                ×
              </button>
            </div>
            <form onSubmit={handleCreateEvent} className="event-form">
              <div className="form-group">
                <label>Event Title *</label>
                <input
                  type="text"
                  value={formData.title}
                  onChange={(e) => setFormData({...formData, title: e.target.value})}
                  required
                />
              </div>
              
              <div className="form-group">
                <label>Description *</label>
                <textarea
                  value={formData.description}
                  onChange={(e) => setFormData({...formData, description: e.target.value})}
                  required
                  rows="3"
                />
              </div>
              
              <div className="form-row">
                <div className="form-group">
                  <label>Event Type *</label>
                  <select
                    value={formData.eventType}
                    onChange={(e) => setFormData({...formData, eventType: e.target.value})}
                    required
                  >
                    <option value="ALUMNI_MEETUP">Alumni Meetup</option>
                    <option value="WORKSHOP">Workshop</option>
                    <option value="JOB_FAIR">Job Fair</option>
                  </select>
                </div>
                
                <div className="form-group">
                  <label>Location *</label>
                  <input
                    type="text"
                    value={formData.location}
                    onChange={(e) => setFormData({...formData, location: e.target.value})}
                    required
                  />
                </div>
              </div>
              
              <div className="form-row">
                <div className="form-group">
                  <label>Date *</label>
                  <input
                    type="date"
                    value={formData.eventDate}
                    onChange={(e) => setFormData({...formData, eventDate: e.target.value})}
                    required
                  />
                </div>
                
                <div className="form-group">
                  <label>Time *</label>
                  <input
                    type="time"
                    value={formData.eventTime}
                    onChange={(e) => setFormData({...formData, eventTime: e.target.value})}
                    required
                  />
                </div>
              </div>
              
              <div className="form-actions">
                <button type="button" onClick={() => setShowCreateModal(false)}>
                  Cancel
                </button>
                <button type="submit" className="submit-btn">
                  Create Event
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Edit Event Modal */}
      {showEditModal && editingEvent && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="modal-header">
              <h2>Edit Event</h2>
              <button 
                className="close-btn"
                onClick={() => setShowEditModal(false)}
              >
                ×
              </button>
            </div>
            <form onSubmit={handleEditEvent} className="event-form">
              <div className="form-group">
                <label>Event Title *</label>
                <input
                  type="text"
                  value={formData.title}
                  onChange={(e) => setFormData({...formData, title: e.target.value})}
                  required
                />
              </div>
              
              <div className="form-group">
                <label>Description *</label>
                <textarea
                  value={formData.description}
                  onChange={(e) => setFormData({...formData, description: e.target.value})}
                  required
                  rows="3"
                />
              </div>
              
              <div className="form-row">
                <div className="form-group">
                  <label>Event Type *</label>
                  <select
                    value={formData.eventType}
                    onChange={(e) => setFormData({...formData, eventType: e.target.value})}
                    required
                  >
                    <option value="ALUMNI_MEETUP">Alumni Meetup</option>
                    <option value="WORKSHOP">Workshop</option>
                    <option value="JOB_FAIR">Job Fair</option>
                  </select>
                </div>
                
                <div className="form-group">
                  <label>Location *</label>
                  <input
                    type="text"
                    value={formData.location}
                    onChange={(e) => setFormData({...formData, location: e.target.value})}
                    required
                  />
                </div>
              </div>
              
              <div className="form-row">
                <div className="form-group">
                  <label>Date *</label>
                  <input
                    type="date"
                    value={formData.eventDate}
                    onChange={(e) => setFormData({...formData, eventDate: e.target.value})}
                    required
                  />
                </div>
                
                <div className="form-group">
                  <label>Time *</label>
                  <input
                    type="time"
                    value={formData.eventTime}
                    onChange={(e) => setFormData({...formData, eventTime: e.target.value})}
                    required
                  />
                </div>
              </div>
              
              <div className="form-actions">
                <button type="button" onClick={() => setShowEditModal(false)}>
                  Cancel
                </button>
                <button type="submit" className="submit-btn">
                  Update Event
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Events;
