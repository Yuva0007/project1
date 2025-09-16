package com.example.demo.service;

import com.example.demo.model.PasswordResetToken;
import com.example.demo.model.User;
import com.example.demo.repository.PasswordResetTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetTokenService {

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    public PasswordResetToken createToken(User user) {
        // Generate a random token
        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(30); // valid for 30 min

        // Remove old token if exists
        tokenRepository.deleteByUser(user);

        PasswordResetToken resetToken = new PasswordResetToken(token, user, expiryDate);
        return tokenRepository.save(resetToken);
    }

    public Optional<PasswordResetToken> getByToken(String token) {
        return tokenRepository.findByToken(token);
    }

    public boolean isTokenValid(PasswordResetToken token) {
        return token.getExpiryDate().isAfter(LocalDateTime.now());
    }
}
