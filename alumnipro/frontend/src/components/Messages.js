import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import api from '../services/api';
import authService from '../services/authService';
import './Messages.css';
import './AdvancedStyles.css';

const Messages = ({ onLogout }) => {
  const [conversations, setConversations] = useState([]);
  const [selectedConversation, setSelectedConversation] = useState(null);
  const [messages, setMessages] = useState([]);
  const [editingMessageId, setEditingMessageId] = useState(null);
  const [editingContent, setEditingContent] = useState('');
  const [newMessage, setNewMessage] = useState('');
  const [loading, setLoading] = useState(true);
  const [showNewMessageModal, setShowNewMessageModal] = useState(false);
  const [newMessageRecipient, setNewMessageRecipient] = useState('');
  const [newMessageContent, setNewMessageContent] = useState('');
  const [alumniList, setAlumniList] = useState([]);
  const [filteredAlumni, setFilteredAlumni] = useState([]);
  const [recipientType, setRecipientType] = useState('');
  const [currentUserId, setCurrentUserId] = useState(null);
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    fetchConversations();
    fetchAlumniList();
  }, []);

  useEffect(() => {
    // Resolve and cache the numeric current user ID using the email/username from the JWT
    const resolveCurrentUserId = async () => {
      try {
        const tokenUser = authService.getCurrentUser();
        if (!tokenUser) return;
        const emailOrUsername = tokenUser.email && tokenUser.email !== 'No email provided'
          ? tokenUser.email
          : tokenUser.username;
        if (!emailOrUsername) return;
        const res = await api.get(`/users/email/${encodeURIComponent(emailOrUsername)}`);
        if (res.data?.id) {
          setCurrentUserId(res.data.id);
        }
      } catch (err) {
        console.error('Error resolving current user id:', err);
      }
    };
    resolveCurrentUserId();
  }, []);

  useEffect(() => {
    if (location.state?.prefillRecipientEmail) {
      setShowNewMessageModal(true);
      setRecipientType(location.state.prefillRecipientRole || 'ALUMNI');
      setNewMessageRecipient(location.state.prefillRecipientEmail);
      handleAlumniSearch(location.state.prefillRecipientEmail);
    }
  }, [location.state]);

  const fetchConversations = async () => {
    try {
      const response = await api.get('/messages/conversations');
      setConversations(response.data);
    } catch (error) {
      console.error('Error fetching conversations:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchAlumniList = async () => {
    try {
      // Fetch all active users instead of just alumni profiles
      const response = await api.get('/users/active');
      setAlumniList(response.data);
      setFilteredAlumni(response.data);
    } catch (error) {
      console.error('Error fetching users list:', error);
    }
  };

  const fetchMessages = async (otherUserId) => {
    try {
      if (!currentUserId) return;
      const response = await api.get(`/messages/conversation/${currentUserId}/${otherUserId}`);
      setMessages(response.data);
    } catch (error) {
      console.error('Error fetching messages:', error);
    }
  };

  const handleSelectConversation = (conversation) => {
    setSelectedConversation(conversation);
    fetchMessages(conversation.otherUserId);
  };

  const handleSendMessage = async (e) => {
    e.preventDefault();
    if (!newMessage.trim()) return;

    try {
      const tokenUser = authService.getCurrentUser();
      await api.post('/messages', {
        receiverId: selectedConversation.otherUserId,
        content: newMessage,
        senderEmail: tokenUser?.email || tokenUser?.username
      });
      setNewMessage('');
      fetchMessages(selectedConversation.otherUserId);
      fetchConversations();
    } catch (error) {
      console.error('Error sending message:', error);
    }
  };

  const handleStartNewConversation = async (e) => {
    e.preventDefault();
    if (!recipientType) {
      alert('Please select recipient type (Student or Alumni).');
      return;
    }
    if (!newMessageRecipient || !newMessageContent.trim()) return;

    try {
      // Try to find user by exact email first
      let selectedUser = alumniList.find(user => 
        user.email.toLowerCase() === newMessageRecipient.toLowerCase()
      );

      // If not found by exact email, try partial name/email match
      if (!selectedUser) {
        const searchResponse = await api.get(`/users/search?query=${encodeURIComponent(newMessageRecipient)}`);
        if (searchResponse.data && searchResponse.data.length > 0) {
          const roleFiltered = searchResponse.data.filter(u => !recipientType || u.role === recipientType);
          if (roleFiltered.length > 0) {
            selectedUser = roleFiltered[0];
          }
        }
      }

      if (!selectedUser) {
        alert('User not found. Please check the email address or name and try again.');
        return;
      }

      if (recipientType && selectedUser.role !== recipientType) {
        alert(`Selected user is not a ${recipientType.toLowerCase()}. Please choose a different user.`);
        return;
      }

      const tokenUser = authService.getCurrentUser();
      const response = await api.post('/messages', {
        receiverId: selectedUser.id,
        content: newMessageContent,
        senderEmail: tokenUser?.email || tokenUser?.username
      });

      // Open the conversation with the selected user and refresh messages
      const newConversation = {
        id: selectedUser.id,
        otherUserId: selectedUser.id,
        otherUserName: selectedUser.name,
        otherUserEmail: selectedUser.email
      };
      setSelectedConversation(newConversation);
      await fetchMessages(selectedUser.id);
      await fetchConversations();

      setNewMessageRecipient('');
      setNewMessageContent('');
      setRecipientType('');
      setShowNewMessageModal(false);
    } catch (error) {
      console.error('Error starting new conversation:', error);
      if (error.response?.data?.message) {
        alert(error.response.data.message);
      } else if (error.response?.data?.error) {
        alert(error.response.data.error);
      } else {
        alert('Failed to send message. Please try again.');
      }
    }
  };

  const handleAlumniSearch = async (searchTerm) => {
    setNewMessageRecipient(searchTerm);
    
    if (searchTerm && searchTerm.length >= 2) {
      try {
        // Use the new search endpoint for better results
        const response = await api.get(`/users/search?query=${encodeURIComponent(searchTerm)}`);
        const results = response.data || [];
        const roleFiltered = recipientType ? results.filter(user => user.role === recipientType) : results;
        setFilteredAlumni(roleFiltered);
      } catch (error) {
        console.error('Error searching users:', error);
        // Fallback to local filtering if search endpoint fails
        const filtered = alumniList.filter(user => 
          (!recipientType || user.role === recipientType) &&
          (user.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
          user.name.toLowerCase().includes(searchTerm.toLowerCase()))
        );
        setFilteredAlumni(filtered);
      }
    } else {
      const roleFiltered = recipientType ? alumniList.filter(user => user.role === recipientType) : alumniList;
      setFilteredAlumni(roleFiltered);
    }
  };

  const getCurrentUserId = () => currentUserId;

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
          <h1>Messages</h1>
          <p style={{ color: 'rgba(255,255,255,0.8)', margin: 0 }}>Stay connected with your network</p>
        </div>
        <nav className="advanced-nav">
          <Link to="/dashboard" className="nav-button">🏠 Dashboard</Link>
          <Link to="/profile" className="nav-button">👤 Profile</Link>
          <Link to="/messages" className="nav-button active">💬 Messages</Link>
          <Link to="/events" className="nav-button">📅 Events</Link>
          <Link to="/jobs" className="nav-button">💼 Jobs</Link>
          {(authService.getCurrentUser()?.role || '').toUpperCase() !== 'ADMIN' && (
          <Link to="/connections" className="nav-button">🤝 Connections</Link>
          )}
          <button onClick={handleLogout} className="nav-button logout-button">🚪 Logout</button>
        </nav>
      </div>
      
      <div className="messages-hero-section fade-in-up">
        <div className="hero-content">
          <div className="hero-info">
            <div className="hero-icon">💬</div>
            <div className="hero-text">
              <h2>Your Conversations</h2>
              <p>Connect and communicate with your network</p>
            </div>
          </div>
          <div className="hero-actions">
          <button 
              className="action-btn new-message-btn"
              onClick={() => {
                setRecipientType('');
                setNewMessageRecipient('');
                setNewMessageContent('');
                setFilteredAlumni(alumniList);
                setShowNewMessageModal(true);
              }}
            >
              <span className="btn-icon">✉️</span>
              <span className="btn-text">New Message</span>
          </button>
          </div>
        </div>
        </div>

      <div className="messages-main-section fade-in-up">
        <div className="messages-container">
          <div className="conversations-sidebar">
            <div className="sidebar-header">
              <div className="section-icon">💬</div>
              <h3>Conversations</h3>
              <p>{conversations.length} active chats</p>
            </div>
          <div className="conversations-list">
            {conversations.length === 0 ? (
                <div className="no-conversations">
                  <div className="no-conversations-content">
                    <div className="no-conversations-icon">💬</div>
                    <h3>No Conversations Yet</h3>
                    <p>Start connecting with others to see your conversations here</p>
                  </div>
                </div>
            ) : (
              conversations.map(conversation => (
                <div 
                  key={conversation.id} 
                  className={`conversation-item ${selectedConversation?.id === conversation.id ? 'active' : ''}`}
                  onClick={() => handleSelectConversation(conversation)}
                >
                  <div className="conversation-avatar">
                    <img
                        src={`https://ui-avatars.com/api/?name=${conversation.otherUserName}&background=random&size=80&bold=true&color=fff`}
                      alt={conversation.otherUserName}
                    />
                      <div className="avatar-glow"></div>
                  </div>
                  <div className="conversation-info">
                      <h4 className="conversation-name">{conversation.otherUserName}</h4>
                      <p className="conversation-preview">{conversation.lastMessage}</p>
                      <span className="conversation-time">{new Date(conversation.lastMessageAt).toLocaleString()}</span>
                  </div>
                </div>
              ))
            )}
            </div>
          </div>
          
          {selectedConversation && (
            <div className="chat-area">
              <div className="chat-header">
                <div className="chat-user-info">
                  <div className="chat-avatar">
                    <img
                      src={`https://ui-avatars.com/api/?name=${selectedConversation.otherUserName}&background=random&size=60&bold=true&color=fff`}
                      alt={selectedConversation.otherUserName}
                    />
                    <div className="avatar-glow"></div>
                  </div>
                  <div className="chat-user-details">
                    <h3 className="chat-user-name">{selectedConversation.otherUserName}</h3>
                    <p className="chat-user-email">{selectedConversation.otherUserEmail}</p>
                  </div>
                </div>
              </div>
              
              <div className="messages-list">
                {messages.length === 0 ? (
                  <div className="no-messages">
                    <div className="no-messages-content">
                      <div className="no-messages-icon">💬</div>
                      <h3>No Messages Yet</h3>
                      <p>Start the conversation by sending a message</p>
                    </div>
                  </div>
                ) : (
                  messages.map(message => (
                  <div 
                    key={message.id} 
                      className={`message ${(message.sender?.id || message.senderId) === getCurrentUserId() ? 'sent' : 'received'}`}
                  >
                    <div className="message-content">
                        {editingMessageId === message.id ? (
                          <div className="edit-message-form">
                            <input
                              className="edit-input"
                              value={editingContent}
                              onChange={(e) => setEditingContent(e.target.value)}
                              placeholder="Edit your message..."
                            />
                            <div className="edit-actions">
                              <button
                                type="button"
                                className="action-btn save-btn"
                                onClick={async () => {
                                  try {
                                    await api.put(`/messages/${message.id}`, { content: editingContent });
                                    setEditingMessageId(null);
                                    setEditingContent('');
                                    fetchMessages(selectedConversation.otherUserId);
                                    fetchConversations();
                                  } catch (e) {
                                    alert('Failed to update message');
                                  }
                                }}
                              >
                                <span className="btn-icon">✓</span>
                                <span className="btn-text">Save</span>
                              </button>
                              <button
                                type="button"
                                className="action-btn cancel-btn"
                                onClick={() => {
                                  setEditingMessageId(null);
                                  setEditingContent('');
                                }}
                              >
                                <span className="btn-icon">✕</span>
                                <span className="btn-text">Cancel</span>
                              </button>
                            </div>
                          </div>
                        ) : (
                          <>
                            <div className="message-text">{message.content}</div>
                            <div className="message-meta">
                              <span className="message-time">{new Date(message.timestamp || message.createdAt).toLocaleString()}</span>
                              {(message.sender?.id || message.senderId) === getCurrentUserId() && (
                                <div className="message-actions">
                                  <button
                                    type="button"
                                    className="message-action-btn edit-btn"
                                    title="Edit"
                                    onClick={() => {
                                      setEditingMessageId(message.id);
                                      setEditingContent(message.content);
                                    }}
                                  >
                                    <span className="action-icon">✏️</span>
                                  </button>
                                  <button
                                    type="button"
                                    className="message-action-btn delete-btn"
                                    title="Delete"
                                    onClick={async () => {
                                      try {
                                        await api.delete(`/messages/${message.id}`);
                                        fetchMessages(selectedConversation.otherUserId);
                                        fetchConversations();
                                      } catch (e) {
                                        alert('Failed to delete message');
                                      }
                                    }}
                                  >
                                    <span className="action-icon">🗑️</span>
                                  </button>
                                </div>
                              )}
                            </div>
                          </>
                        )}
                    </div>
                  </div>
                  ))
                )}
              </div>
              
              <form onSubmit={handleSendMessage} className="message-form">
                <div className="message-input-container">
                <input
                  type="text"
                  placeholder="Type a message..."
                  value={newMessage}
                  onChange={(e) => setNewMessage(e.target.value)}
                    className="message-input"
                />
                  <button type="submit" className="send-btn">
                    <span className="btn-icon">📤</span>
                  </button>
                </div>
              </form>
            </div>
          )}

          {!selectedConversation && (
            <div className="no-conversation-selected">
              <div className="no-conversation-content">
                <div className="no-conversation-icon">💬</div>
                <h3>Select a Conversation</h3>
                <p>Choose a conversation from the sidebar to start messaging</p>
                <button 
                  className="action-btn start-conversation-btn"
                  onClick={() => {
                    setRecipientType('');
                    setNewMessageRecipient('');
                    setNewMessageContent('');
                    setFilteredAlumni(alumniList);
                    setShowNewMessageModal(true);
                  }}
                >
                  <span className="btn-icon">✉️</span>
                  <span className="btn-text">Start New Conversation</span>
                </button>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* New Message Modal */}
      {showNewMessageModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="modal-header">
              <div className="section-icon">✉️</div>
            <h3>Start New Conversation</h3>
              <button 
                className="close-btn"
                onClick={() => {
                  setShowNewMessageModal(false);
                  setNewMessageRecipient('');
                  setNewMessageContent('');
                  setFilteredAlumni([]);
                  setRecipientType('');
                }}
              >×</button>
            </div>
            <form onSubmit={handleStartNewConversation} className="modal-form">
              <div className="form-group">
                <label className="form-label">Recipient Type:</label>
                <select
                  value={recipientType}
                  onChange={(e) => {
                    setRecipientType(e.target.value);
                    handleAlumniSearch(newMessageRecipient);
                  }}
                  required
                  className="form-select"
                >
                  <option value="">Select type...</option>
                  <option value="STUDENT">Student</option>
                  <option value="ALUMNI">Alumni</option>
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">User Email or Name:</label>
                <input
                  type="text"
                  placeholder="Enter user email or name..."
                  value={newMessageRecipient}
                  onChange={(e) => handleAlumniSearch(e.target.value)}
                  required
                  className="form-input"
                />
                {newMessageRecipient && filteredAlumni.length > 0 && (
                  <div className="user-suggestions">
                    {filteredAlumni.map(user => (
                      <div 
                        key={user.id}
                        className="user-suggestion"
                        onClick={() => {
                          setNewMessageRecipient(user.email);
                          setFilteredAlumni([]);
                        }}
                      >
                        <div className="suggestion-avatar">
                          <img
                            src={`https://ui-avatars.com/api/?name=${user.name}&background=random&size=40&bold=true&color=fff`}
                            alt={user.name}
                          />
                        </div>
                        <div className="suggestion-info">
                          <span className="suggestion-name">{user.name}</span>
                          <span className="suggestion-email">{user.email}</span>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
              
              <div className="form-group">
                <label className="form-label">Message:</label>
                <textarea
                  placeholder="Type your message..."
                  value={newMessageContent}
                  onChange={(e) => setNewMessageContent(e.target.value)}
                  required
                  rows="4"
                  className="form-textarea"
                />
              </div>
              
              <div className="modal-actions">
                <button type="submit" className="action-btn send-btn" disabled={!recipientType}>
                  <span className="btn-icon">📤</span>
                  <span className="btn-text">Send Message</span>
                </button>
                <button 
                  type="button" 
                  className="action-btn cancel-btn"
                  onClick={() => {
                    setShowNewMessageModal(false);
                    setNewMessageRecipient('');
                    setNewMessageContent('');
                    setFilteredAlumni([]);
                    setRecipientType('');
                  }}
                >
                  <span className="btn-icon">✕</span>
                  <span className="btn-text">Cancel</span>
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Messages;
