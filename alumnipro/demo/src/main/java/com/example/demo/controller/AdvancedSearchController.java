package com.example.demo.controller;

import com.example.demo.dto.AdvancedSearchRequest;
import com.example.demo.dto.AdvancedSearchResponse;
import com.example.demo.service.AdvancedSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = "*")
public class AdvancedSearchController {

    private final AdvancedSearchService advancedSearchService;

    public AdvancedSearchController(AdvancedSearchService advancedSearchService) {
        this.advancedSearchService = advancedSearchService;
    }

    @PostMapping("/advanced")
    public ResponseEntity<AdvancedSearchResponse> advancedSearch(
            @RequestBody AdvancedSearchRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        AdvancedSearchResponse response = advancedSearchService.searchUsers(request, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users")
    public ResponseEntity<AdvancedSearchResponse> searchUsers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String industry,
            @RequestParam(required = false) String skills,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String graduationYear,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String yearOfStudy,
            @RequestParam(required = false) String registrationNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        AdvancedSearchRequest request = new AdvancedSearchRequest();
        request.setName(name);
        request.setEmail(email);
        request.setRole(role);
        request.setIndustry(industry);
        request.setSkills(skills);
        request.setLocation(location);
        request.setCompany(company);
        request.setGraduationYear(graduationYear);
        request.setDepartment(department);
        request.setYearOfStudy(yearOfStudy);
        request.setRegistrationNumber(registrationNumber);
        
        AdvancedSearchResponse response = advancedSearchService.searchUsers(request, page, size);
        return ResponseEntity.ok(response);
    }
}
