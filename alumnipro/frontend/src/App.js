import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, Link } from 'react-router-dom';
import Login from './components/Login';
import Register from './components/Register';
import Dashboard from './components/Dashboard';
import AdminDashboard from './components/AdminDashboard';
import ManageUsers from './components/ManageUsers';
import UserProfileView from './components/UserProfileView';
import Profile from './components/Profile';
import ProfileEdit from './components/ProfileEdit';
import Connections from './components/Connections';
import Messages from './components/Messages';
import Events from './components/Events';
import JobBoard from './components/JobBoard';
import './App.css';
import authService from './services/authService';

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(authService.isAuthenticated());

  // Update auth state when token changes
  const updateAuthState = () => {
    setIsAuthenticated(authService.isAuthenticated());
  };

  // Check auth state on mount and when localStorage changes
  useEffect(() => {
    updateAuthState();
    
    // Listen for storage changes
    const handleStorageChange = () => {
      updateAuthState();
    };

    window.addEventListener('storage', handleStorageChange);
    
    return () => {
      window.removeEventListener('storage', handleStorageChange);
    };
  }, []);

  const handleLoginSuccess = () => {
    updateAuthState();
  };

  const handleLogout = () => {
    authService.logout();
    updateAuthState();
  };

  return (
    <Router>
      <div className="App">
        <Routes>
          <Route 
            path="/login" 
            element={!isAuthenticated ? <Login onLoginSuccess={handleLoginSuccess} /> : <Navigate to="/dashboard" replace />} 
          />
          <Route 
            path="/register" 
            element={!isAuthenticated ? <Register /> : <Navigate to="/dashboard" replace />} 
          />
          <Route 
            path="/dashboard" 
            element={
              isAuthenticated ? (
                (authService.getCurrentUser()?.role === 'ADMIN') ? (
                  <AdminDashboard onLogout={handleLogout} />
                ) : (
                  <Dashboard onLogout={handleLogout} />
                )
              ) : (
                <Navigate to="/login" replace />
              )
            } 
          />
          <Route 
            path="/profile" 
            element={
              isAuthenticated ? (
                <Profile onLogout={handleLogout} />
              ) : (
                <Navigate to="/login" replace />
              )
            } 
          />
          <Route 
            path="/profile/edit" 
            element={
              isAuthenticated ? (
                <ProfileEdit />
              ) : (
                <Navigate to="/login" replace />
              )
            } 
          />
          <Route 
            path="/connections" 
            element={
              isAuthenticated ? (
                <Connections onLogout={handleLogout} />
              ) : (
                <Navigate to="/login" replace />
              )
            } 
          />
          <Route 
            path="/messages" 
            element={
              isAuthenticated ? (
                <Messages onLogout={handleLogout} />
              ) : (
                <Navigate to="/login" replace />
              )
            } 
          />
          <Route 
            path="/admin/users" 
            element={
              isAuthenticated && authService.getCurrentUser()?.role === 'ADMIN' ? (
                <ManageUsers />
              ) : (
                <Navigate to="/login" replace />
              )
            } 
          />
          <Route 
            path="/users/:id" 
            element={
              isAuthenticated ? (
                <UserProfileView />
              ) : (
                <Navigate to="/login" replace />
              )
            } 
          />
          <Route 
            path="/events" 
            element={
              isAuthenticated ? (
                <Events />
              ) : (
                <Navigate to="/login" replace />
              )
            } 
          />
          <Route 
            path="/jobs" 
            element={
              isAuthenticated ? (
                <JobBoard />
              ) : (
                <Navigate to="/login" replace />
              )
            } 
          />
          <Route 
            path="/" 
            element={<Navigate to={isAuthenticated ? "/dashboard" : "/login"} replace />} 
          />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
