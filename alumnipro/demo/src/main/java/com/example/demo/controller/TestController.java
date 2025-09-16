package com.example.demo.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/admin")
    public String adminAccess() {
        return "Hello Admin!";
    }

    @GetMapping("/alumni")
    public String alumniAccess() {
        return "Hello Alumni!";
    }

    @GetMapping("/student")
    public String studentAccess() {
        return "Hello Student!";
    }

    @GetMapping("/public")
    public String publicAccess() {
        return "Hello Public!";
    }
}
