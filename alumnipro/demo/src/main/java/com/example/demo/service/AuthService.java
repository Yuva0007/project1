package com.example.demo.service;

import com.example.demo.DTOs.RegisterRequest;
import com.example.demo.DTOs.LoginRequest;
import com.example.demo.DTOs.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
