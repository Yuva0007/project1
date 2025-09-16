package com.example.demo.service;

import com.example.demo.model.Message;
import com.example.demo.model.User;
import com.example.demo.repository.MessageRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Message> getAllMessages() {
        return messageRepository.findAll();
    }

    public Optional<Message> getMessageById(Long id) {
        return messageRepository.findById(id);
    }

    public Message createMessage(Message message) {
        return messageRepository.save(message);
    }

    public List<Message> getMessagesBetweenUsers(Long senderId, Long receiverId) {
        Optional<User> sender = userRepository.findById(senderId);
        Optional<User> receiver = userRepository.findById(receiverId);

        if (sender.isPresent() && receiver.isPresent()) {
            return messageRepository.findBySenderAndReceiver(sender.get(), receiver.get());
        } else {
            return List.of();
        }
    }

    public List<Message> getConversationBetweenUsers(Long user1Id, Long user2Id) {
        Optional<User> user1 = userRepository.findById(user1Id);
        Optional<User> user2 = userRepository.findById(user2Id);

        if (user1.isPresent() && user2.isPresent()) {
            return messageRepository.findConversationBetweenUsers(user1.get(), user2.get());
        } else {
            return List.of();
        }
    }

    public List<User> getConversationPartners(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        return user.map(messageRepository::findConversationPartners).orElse(List.of());
    }

    public List<Message> getAllMessagesForUser(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        return user.map(messageRepository::findAllMessagesForUser).orElse(List.of());
    }

    public List<Message> getConversationsForUser(Long userId) {
        List<Message> allMessages = getAllMessagesForUser(userId);
        
        // Group messages by conversation partner
        return allMessages.stream()
                .collect(Collectors.groupingBy(m -> {
                    if (m.getSender().getId().equals(userId)) {
                        return m.getReceiver().getId();
                    } else {
                        return m.getSender().getId();
                    }
                }))
                .entrySet().stream()
                .map(entry -> entry.getValue().stream()
                        .max((m1, m2) -> m1.getTimestamp().compareTo(m2.getTimestamp()))
                        .orElse(null))
                .filter(m -> m != null)
                .collect(Collectors.toList());
    }

    public void deleteMessage(Long id) {
        messageRepository.deleteById(id);
    }
}
