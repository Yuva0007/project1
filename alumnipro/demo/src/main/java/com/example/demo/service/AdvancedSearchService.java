package com.example.demo.service;

import com.example.demo.dto.AdvancedSearchRequest;
import com.example.demo.dto.AdvancedSearchResponse;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdvancedSearchService {

    @PersistenceContext
    private EntityManager entityManager;

    private final UserRepository userRepository;

    public AdvancedSearchService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public AdvancedSearchResponse searchUsers(AdvancedSearchRequest request, int page, int size) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> cq = cb.createQuery(User.class);
        Root<User> user = cq.from(User.class);

        List<Predicate> predicates = new ArrayList<>();

        // Add filters based on request
        if (request.getName() != null && !request.getName().isEmpty()) {
            predicates.add(cb.like(cb.lower(user.get("name")), "%" + request.getName().toLowerCase() + "%"));
        }

        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            predicates.add(cb.like(cb.lower(user.get("email")), "%" + request.getEmail().toLowerCase() + "%"));
        }

        if (request.getRole() != null && !request.getRole().isEmpty()) {
            predicates.add(cb.equal(user.get("role"), User.Role.valueOf(request.getRole().toUpperCase())));
        }

        // Filter by account status
        predicates.add(cb.notEqual(user.get("accountStatus"), User.AccountStatus.DELETED));

        // Join with AlumniProfile for alumni-specific filters
        if (request.getIndustry() != null || request.getSkills() != null || 
            request.getLocation() != null || request.getCompany() != null || 
            request.getGraduationYear() != null) {
            
            Join<Object, Object> alumniProfile = user.join("alumniProfile", JoinType.LEFT);
            
            if (request.getIndustry() != null && !request.getIndustry().isEmpty()) {
                predicates.add(cb.like(cb.lower(alumniProfile.get("industry")), "%" + request.getIndustry().toLowerCase() + "%"));
            }
            
            if (request.getSkills() != null && !request.getSkills().isEmpty()) {
                predicates.add(cb.like(cb.lower(alumniProfile.get("skills")), "%" + request.getSkills().toLowerCase() + "%"));
            }
            
            if (request.getLocation() != null && !request.getLocation().isEmpty()) {
                predicates.add(cb.like(cb.lower(alumniProfile.get("location")), "%" + request.getLocation().toLowerCase() + "%"));
            }
            
            if (request.getCompany() != null && !request.getCompany().isEmpty()) {
                predicates.add(cb.like(cb.lower(alumniProfile.get("company")), "%" + request.getCompany().toLowerCase() + "%"));
            }
            
            if (request.getGraduationYear() != null && !request.getGraduationYear().isEmpty()) {
                predicates.add(cb.like(cb.lower(alumniProfile.get("graduationYear")), "%" + request.getGraduationYear().toLowerCase() + "%"));
            }
        }

        // Join with StudentProfile for student-specific filters
        if (request.getDepartment() != null || request.getYearOfStudy() != null || 
            request.getRegistrationNumber() != null) {
            
            Join<Object, Object> studentProfile = user.join("studentProfile", JoinType.LEFT);
            
            if (request.getDepartment() != null && !request.getDepartment().isEmpty()) {
                predicates.add(cb.like(cb.lower(studentProfile.get("department")), "%" + request.getDepartment().toLowerCase() + "%"));
            }
            
            if (request.getYearOfStudy() != null && !request.getYearOfStudy().isEmpty()) {
                predicates.add(cb.equal(studentProfile.get("yearOfStudy"), Integer.parseInt(request.getYearOfStudy())));
            }
            
            if (request.getRegistrationNumber() != null && !request.getRegistrationNumber().isEmpty()) {
                predicates.add(cb.like(cb.lower(studentProfile.get("registrationNumber")), "%" + request.getRegistrationNumber().toLowerCase() + "%"));
            }
        }

        cq.where(cb.and(predicates.toArray(new Predicate[0])));

        // Count query
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<User> countRoot = countQuery.from(User.class);
        countQuery.select(cb.count(countRoot));
        countQuery.where(cb.and(predicates.toArray(new Predicate[0])));

        // Execute count query
        TypedQuery<Long> countTypedQuery = entityManager.createQuery(countQuery);
        long totalCount = countTypedQuery.getSingleResult();

        // Execute main query with pagination
        TypedQuery<User> typedQuery = entityManager.createQuery(cq);
        typedQuery.setFirstResult(page * size);
        typedQuery.setMaxResults(size);

        List<User> users = typedQuery.getResultList();

        return new AdvancedSearchResponse(users, totalCount, page, size);
    }
}
