import api from './api';

class AuthService {
  async login(credentials) {
    const response = await api.post('/auth/login', credentials);
    if (response.data.token) {
      localStorage.setItem('token', response.data.token);
    }
    return response.data;
  }

  async register(userData) {
    const response = await api.post('/auth/register', userData);
    if (response.data.token) {
      localStorage.setItem('token', response.data.token);
    }
    return response.data;
  }

  logout() {
    localStorage.removeItem('token');
    window.location.href = '/login';
  }

  getCurrentUser() {
    const token = localStorage.getItem('token');
    if (!token) {
      return null;
    }

    try {
      // Split the token into parts
      const parts = token.split('.');
      if (parts.length !== 3) {
        console.warn('Invalid JWT token format');
        this.logout();
        return null;
      }

      // Decode the payload (second part)
      const payload = parts[1];
      
      // Add padding if needed for base64 decoding
      const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
      const padding = '='.repeat((4 - base64.length % 4) % 4);
      const decoded = atob(base64 + padding);
      
      const payloadObj = JSON.parse(decoded);
      
      // Return user object with proper structure for the dashboard
      return {
        username: payloadObj.username || payloadObj.sub,
        email: payloadObj.email || 'No email provided',
        role: payloadObj.role || 'No role assigned',
        sub: payloadObj.sub
      };
    } catch (error) {
      console.error('Error decoding JWT token:', error);
      // Clear invalid token
      this.logout();
      return null;
    }
  }

  isAuthenticated() {
    const token = localStorage.getItem('token');
    if (!token) {
      return false;
    }

    try {
      // Check if token is valid and not expired
      const parts = token.split('.');
      if (parts.length !== 3) {
        return false;
      }

      const payload = parts[1];
      const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
      const padding = '='.repeat((4 - base64.length % 4) % 4);
      const decoded = atob(base64 + padding);
      const payloadObj = JSON.parse(decoded);
      
      // Check if token is expired
      if (payloadObj.exp && payloadObj.exp * 1000 < Date.now()) {
        this.logout();
        return false;
      }
      
      return true;
    } catch (error) {
      console.error('Error validating token:', error);
      return false;
    }
  }

  getToken() {
    return localStorage.getItem('token');
  }

  clearToken() {
    localStorage.removeItem('token');
  }
}

export default new AuthService();
