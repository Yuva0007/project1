package com.example.demo.DTOs;

public class AuthResponse {
    private String message;
    private String token;

    // ✅ Constructor
    public AuthResponse(String message, String token) {
        this.message = message;
        this.token = token;
    }

    // ✅ Getters
    public String getMessage() {
        return message;
    }

    public String getToken() {
        return token;
    }
}
