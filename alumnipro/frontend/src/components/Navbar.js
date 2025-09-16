import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import authService from '../services/authService';
import './Navbar.css';

const Navbar = ({ onLogout }) => {
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const [isScrolled, setIsScrolled] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    const handleScroll = () => {
      setIsScrolled(window.scrollY > 20);
    };
    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  const handleLogout = () => {
    authService.logout();
    if (onLogout) {
      onLogout();
    }
    navigate('/login', { replace: true });
    setIsMenuOpen(false);
  };

  const toggleMenu = () => {
    setIsMenuOpen(!isMenuOpen);
  };

  const closeMenu = () => {
    setIsMenuOpen(false);
  };

  const isActive = (path) => {
    return location.pathname === path ? 'active' : '';
  };

  return (
    <nav className={`navbar ${isScrolled ? 'scrolled' : ''}`}>
      <div className="nav-container">
        <Link to="/dashboard" className="nav-logo" onClick={closeMenu}>
          <span className="logo-text">Alumni</span>
          <span className="logo-accent">Network</span>
        </Link>

        <div className={`nav-menu ${isMenuOpen ? 'active' : ''}`}>
          <Link 
            to="/dashboard" 
            className={`nav-link ${isActive('/dashboard')}`}
            onClick={closeMenu}
          >
            <i className="nav-icon">📊</i>
            Dashboard
          </Link>
          
          <Link 
            to="/profile" 
            className={`nav-link ${isActive('/profile')}`}
            onClick={closeMenu}
          >
            <i className="nav-icon">👤</i>
            Profile
          </Link>
          
          <Link 
            to="/connections" 
            className={`nav-link ${isActive('/connections')}`}
            onClick={closeMenu}
          >
            <i className="nav-icon">🤝</i>
            Connections
          </Link>
          
          <Link 
            to="/messages" 
            className={`nav-link ${isActive('/messages')}`}
            onClick={closeMenu}
          >
            <i className="nav-icon">💬</i>
            Messages
          </Link>
          
          <button onClick={handleLogout} className="nav-link logout-btn">
            <i className="nav-icon">🚪</i>
            Logout
          </button>
        </div>

        <div 
          className={`nav-toggle ${isMenuOpen ? 'active' : ''}`}
          onClick={toggleMenu}
        >
          <span></span>
          <span></span>
          <span></span>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
