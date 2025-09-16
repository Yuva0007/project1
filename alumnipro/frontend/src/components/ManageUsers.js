import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../services/api';
import authService from '../services/authService';
import './AdvancedStyles.css';
import './Connections.css';

const ManageUsers = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [roleFilter, setRoleFilter] = useState('ALL');
  const navigate = useNavigate();

  useEffect(() => {
    if (!authService.isAuthenticated() || authService.getCurrentUser()?.role !== 'ADMIN') {
      navigate('/login');
      return;
    }
    const loadUsers = async () => {
      try {
        const res = await api.get('/users/active');
        let list = res.data || [];
        if (!Array.isArray(list) || list.length === 0) {
          const resAll = await api.get('/users');
          list = resAll.data || [];
        }
        // Exclude admins from management list
        setUsers(list.filter(u => (u.role || '').toUpperCase() !== 'ADMIN'));
      } catch (e) {
        console.error('Failed to load users', e);
      } finally {
        setLoading(false);
      }
    };
    loadUsers();
  }, [navigate]);

  const handleDelete = async (userId, userName) => {
    const ok = window.confirm(`Remove user ${userName || ''}? This will deactivate their account.`);
    if (!ok) return;
    try {
      await api.delete(`/users/${userId}`);
      // Optimistically remove from UI
      setUsers(prev => prev.filter(u => u.id !== userId));
    } catch (e) {
      console.error('Failed to delete user', e);
      alert('Failed to remove user. Please try again.');
    }
  };

  const filtered = users
    .filter(u => (roleFilter === 'ALL') || ((u.role || '').toUpperCase() === roleFilter))
    .filter(u => {
      const q = searchTerm.toLowerCase();
      return (u.name || '').toLowerCase().includes(q) || (u.email || '').toLowerCase().includes(q);
    });

  if (loading) return <div className="loading">Loading...</div>;

  return (
    <div className="page-container">
      <div className="advanced-header fade-in">
        <div>
          <h1>Manage Users</h1>
          <p style={{ color: 'rgba(255,255,255,0.8)', margin: 0 }}>View all registered alumni and students</p>
        </div>
        <nav className="advanced-nav">
          <Link to="/dashboard" className="nav-button">🏠 Dashboard</Link>
          <Link to="/profile" className="nav-button">👤 Profile</Link>
          <Link to="/messages" className="nav-button">💬 Messages</Link>
          <Link to="/events" className="nav-button">📅 Events</Link>
          <Link to="/jobs" className="nav-button">💼 Jobs</Link>
        </nav>
      </div>

      <div className="glass-card fade-in">
        <div className="search-section" style={{ gap: '10px' }}>
          <input
            type="text"
            placeholder="Search users by name or email..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="advanced-input"
          />
          <select
            className="advanced-input"
            style={{ maxWidth: 200 }}
            value={roleFilter}
            onChange={(e) => setRoleFilter(e.target.value)}
          >
            <option value="ALL">All Roles</option>
            <option value="ALUMNI">Alumni</option>
            <option value="STUDENT">Student</option>
          </select>
        </div>

        <div className="connections-list">
          <h3>All Users ({filtered.length})</h3>
          {filtered.length === 0 ? (
            <p>No users found.</p>
          ) : (
            <div className="connections-grid">
              {filtered.map(u => (
                <div key={u.id} className="connection-card" style={{ position: 'relative' }}>
                  <button
                    className="user-delete-btn"
                    title="Remove user"
                    onClick={() => handleDelete(u.id, u.name)}
                  >
                    🗑️
                  </button>
                  <div className="connection-avatar">
                    <img src={`https://ui-avatars.com/api/?name=${u.name}&background=random`} alt={u.name} />
                  </div>
                  <div className="connection-info">
                    <h4>{u.name}</h4>
                    <p>{u.email}</p>
                    <p className="connection-role">{u.role}</p>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default ManageUsers;


