package com.example.demo.controller;

import com.example.demo.model.PasswordResetToken;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.PasswordResetTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/password-reset")
public class PasswordResetController {

    @Autowired
    private PasswordResetTokenService tokenService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/create-token")
    public ResponseEntity<String> createToken(@RequestParam String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found.");
        }

        PasswordResetToken token = tokenService.createToken(user.get());
        return ResponseEntity.ok("Token: " + token.getToken()); // In real app, email this
    }

    @GetMapping("/validate")
    public ResponseEntity<String> validateToken(@RequestParam String token) {
        Optional<PasswordResetToken> resetToken = tokenService.getByToken(token);
        if (resetToken.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid token.");
        }

        if (!tokenService.isTokenValid(resetToken.get())) {
            return ResponseEntity.badRequest().body("Token expired.");
        }

        return ResponseEntity.ok("Token is valid.");
    }
}
