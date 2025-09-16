package com.example.demo.controller;

import com.example.demo.model.Message;
import com.example.demo.model.User;
import com.example.demo.service.MessageService;
import com.example.demo.service.UserService;
import com.example.demo.security.JwtUtil;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<Message> getAllMessages() {
        return messageService.getAllMessages();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Message> getMessageById(@PathVariable Long id) {
        return messageService.getMessageById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createMessage(@RequestBody Map<String, Object> messageRequest, @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            System.out.println("=== MESSAGE SEND ATTEMPT ===");
            System.out.println("Request: " + messageRequest);
            
            // Resolve sender from JWT if available, otherwise fall back to senderEmail in body
            User sender = null;
            String email = null;
            if (token != null && token.startsWith("Bearer ")) {
                try {
                    String jwt = token.substring(7);
                    email = jwtUtil.extractEmail(jwt);
                    if (email == null || email.isBlank()) {
                        // Fallback to subject if email is missing
                        email = jwtUtil.extractUsername(jwt);
                    }
                } catch (Exception ignored) {}
            }

            if (email != null && !email.isBlank()) {
                sender = userRepository.findByEmail(email).orElse(null);
            }

            if (sender == null && messageRequest.get("senderEmail") != null) {
                String senderEmail = messageRequest.get("senderEmail").toString();
                sender = userRepository.findByEmail(senderEmail).orElse(null);
            }

            if (sender == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Unauthorized",
                    "message", "Unable to identify sender. Please log in again."
                ));
            }
            
            System.out.println("Sender found: " + sender.getName() + " (ID: " + sender.getId() + ")");
            
            // Validate sender account status
            if (sender.getAccountStatus() != User.AccountStatus.ACTIVE) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Sender account not active",
                    "message", "Your account is " + sender.getAccountStatus().toString().toLowerCase() + " and cannot send messages."
                ));
            }
            
            Long receiverId;
            try {
                receiverId = Long.valueOf(messageRequest.get("receiverId").toString());
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid receiver ID",
                    "message", "Please provide a valid user ID."
                ));
            }
            
            String content = messageRequest.get("content").toString();
            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Empty message",
                    "message", "Message content cannot be empty."
                ));
            }
            
            User receiver = userRepository.findById(receiverId)
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + receiverId));
            
            System.out.println("Receiver found: " + receiver.getName() + " (ID: " + receiver.getId() + ")");
            System.out.println("Receiver status: " + receiver.getAccountStatus());
            
            // Check if receiver can receive messages
            if (receiver.getAccountStatus() == User.AccountStatus.DELETED) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Cannot message deleted user",
                    "message", "This user account has been deleted and cannot receive messages."
                ));
            }
            
            if (receiver.getAccountStatus() == User.AccountStatus.INACTIVE) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Cannot message inactive user",
                    "message", "This user account is inactive and cannot receive messages."
                ));
            }
            
            if (receiver.getAccountStatus() == User.AccountStatus.SUSPENDED) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Cannot message suspended user",
                    "message", "This user account is suspended and cannot receive messages."
                ));
            }
            
            Message message = new Message(sender, receiver, content.trim());
            Message savedMessage = messageService.createMessage(message);
            
            System.out.println("Message sent successfully: ID " + savedMessage.getId());
            return ResponseEntity.ok(savedMessage);
            
        } catch (RuntimeException e) {
            System.err.println("Runtime error sending message: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Runtime error",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            System.err.println("Unexpected error sending message: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Unknown error",
                "message", "An unexpected error occurred while sending the message."
            ));
        }
    }

    @GetMapping("/conversation")
    public List<Message> getMessagesBetweenUsers(
            @RequestParam Long senderId,
            @RequestParam Long receiverId
    ) {
        return messageService.getMessagesBetweenUsers(senderId, receiverId);
    }

    @GetMapping("/conversation/{userId1}/{userId2}")
    public List<Message> getConversationBetweenUsers(
            @PathVariable Long userId1,
            @PathVariable Long userId2
    ) {
        return messageService.getConversationBetweenUsers(userId1, userId2);
    }

    @GetMapping("/conversations/{userId}")
    public List<Map<String, Object>> getConversationsForUser(@PathVariable Long userId) {
        List<Message> conversations = messageService.getConversationsForUser(userId);
        
        return conversations.stream().map(message -> {
            Map<String, Object> conversation = new HashMap<>();
            User otherUser = message.getSender().getId().equals(userId) 
                ? message.getReceiver() 
                : message.getSender();
            
            conversation.put("id", otherUser.getId());
            conversation.put("otherUserId", otherUser.getId());
            conversation.put("otherUserName", otherUser.getName());
            conversation.put("otherUserEmail", otherUser.getEmail());
            conversation.put("lastMessage", message.getContent());
            conversation.put("lastMessageAt", message.getTimestamp());
            conversation.put("senderId", message.getSender().getId());
            conversation.put("receiverId", message.getReceiver().getId());
            
            return conversation;
        }).collect(Collectors.toList());
    }

    @GetMapping("/conversations")
    public List<Map<String, Object>> getCurrentUserConversations(@RequestHeader("Authorization") String token) {
        String jwt = token.substring(7);
        String email = jwtUtil.extractEmail(jwt);
        if (email == null || email.isBlank()) {
            email = jwtUtil.extractUsername(jwt);
        }
        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found: " + email);
        }
        User user = userOpt.get();
        
        return getConversationsForUser(user.getId());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
        messageService.deleteMessage(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMessage(@PathVariable Long id,
                                           @RequestBody Map<String, Object> body,
                                           @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            String newContent = body.get("content") != null ? body.get("content").toString().trim() : null;
            if (newContent == null || newContent.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Empty content",
                    "message", "Message content cannot be empty."
                ));
            }

            // Identify requester
            String requesterEmail = null;
            if (token != null && token.startsWith("Bearer ")) {
                String jwt = token.substring(7);
                requesterEmail = jwtUtil.extractEmail(jwt);
                if (requesterEmail == null || requesterEmail.isBlank()) {
                    requesterEmail = jwtUtil.extractUsername(jwt);
                }
            }
            if (requesterEmail == null || requesterEmail.isBlank()) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Unauthorized",
                    "message", "Missing or invalid Authorization token."
                ));
            }

            // Load message and validate ownership (only sender can edit)
            var msgOpt = messageService.getMessageById(id);
            if (msgOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            var msg = msgOpt.get();
            var requester = userRepository.findByEmail(requesterEmail).orElse(null);
            if (requester == null || msg.getSender() == null || !msg.getSender().getId().equals(requester.getId())) {
                return ResponseEntity.status(403).body(Map.of(
                    "error", "Forbidden",
                    "message", "Only the sender can edit this message."
                ));
            }

            msg.setContent(newContent);
            var updated = messageService.createMessage(msg);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Update failed",
                "message", e.getMessage()
            ));
        }
    }
}
