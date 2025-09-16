package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.example.demo.security.JwtService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public record SignupRequest(String name, String email, String password) {}
    public record LoginRequest(String email, String password) {}
    public record AuthResponse(String token, Long userId, String name, String email, String role) {}

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        try {
            if (request.name() == null || request.name().isBlank() ||
                request.email() == null || request.email().isBlank() ||
                request.password() == null || request.password().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("message", "All fields are required"));
            }

            if (userRepository.existsByEmail(request.email())) {
                return ResponseEntity.status(409).body(Map.of("message", "Email already registered"));
            }

            User user = new User();
            user.setName(request.name());
            user.setEmail(request.email());
            user.setPassword(passwordEncoder.encode(request.password()));
            // defaults: role CITIZEN, status ACTIVE set in entity

            User saved = userRepository.save(user);

            // Return minimal response
            Map<String, Object> resp = new HashMap<>();
            resp.put("id", saved.getId());
            resp.put("name", saved.getName());
            resp.put("email", saved.getEmail());
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            log.error("Signup error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("message", "Failed to create account"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Optional<User> opt = userRepository.findByEmail(request.email());
            if (opt.isEmpty()) {
                return ResponseEntity.status(401).body(Map.of("message", "Invalid credentials"));
            }
            User user = opt.get();
            if (!passwordEncoder.matches(request.password(), user.getPassword())) {
                return ResponseEntity.status(401).body(Map.of("message", "Invalid credentials"));
            }

            String token = jwtService.generateToken(user.getEmail(), Map.of(
                    "userId", user.getId(),
                    "role", user.getRole().name(),
                    "name", user.getName()
            ));
            return ResponseEntity.ok(new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole().name()));
        } catch (Exception e) {
            log.error("Login error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("message", "Login failed"));
        }
    }
}


