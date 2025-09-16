package com.example.demo.service;

import com.example.demo.DTOs.RegisterRequest;
import com.example.demo.DTOs.LoginRequest;
import com.example.demo.DTOs.AuthResponse;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Override
    public AuthResponse register(RegisterRequest registerRequest) {
        // Check if email already exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        
        User user = new User();
        user.setName(registerRequest.getName());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(User.Role.valueOf(registerRequest.getRole().toUpperCase()));

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getName(), user.getEmail(), user.getRole().toString());

        return new AuthResponse("User registered successfully", token);
    }


    @Override
    public AuthResponse login(LoginRequest request) {
        // Authenticate credentials
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
            )
        );

        // Load user from DB
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate JWT token with user details
        String token = jwtUtil.generateToken(user.getName(), user.getEmail(), user.getRole().toString());


        return new AuthResponse("User logged in successfully", token);
    }
}
