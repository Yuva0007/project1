package com.example.demo.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alumni")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"}, allowCredentials = "true")
public class AlumniController {

    @GetMapping("/dashboard")
    public String alumniDashboard() {
        return "Welcome to Alumni Dashboard!";
    }
}
