package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    @Column(length = 32)
    private String phone;

    @Enumerated(EnumType.STRING)
    private Role role;

    private LocalDateTime createdAt;
    
    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatus = AccountStatus.ACTIVE;
    
    @Enumerated(EnumType.STRING)
    private ProfileVisibility profileVisibility = ProfileVisibility.PUBLIC;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference
    private AlumniProfile alumniProfile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference
    private StudentProfile studentProfile;

    public enum Role {
        ALUMNI,
        STUDENT,
        ADMIN
    }
    
    public enum AccountStatus {
        ACTIVE,
        INACTIVE,
        SUSPENDED,
        DELETED
    }
    
    public enum ProfileVisibility {
        PUBLIC,
        CONNECTED,
        PRIVATE
    }

    public User() {
        this.createdAt = LocalDateTime.now();
    }

    public User(String name, String email, String password, Role role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(AccountStatus accountStatus) {
        this.accountStatus = accountStatus;
    }

    public ProfileVisibility getProfileVisibility() {
        return profileVisibility;
    }

    public void setProfileVisibility(ProfileVisibility profileVisibility) {
        this.profileVisibility = profileVisibility;
    }

    public AlumniProfile getAlumniProfile() {
        return alumniProfile;
    }

    public void setAlumniProfile(AlumniProfile alumniProfile) {
        this.alumniProfile = alumniProfile;

        if (alumniProfile != null) {
            alumniProfile.setUser(this);
        }
    }

    public StudentProfile getStudentProfile() {
        return studentProfile;
    }

    public void setStudentProfile(StudentProfile studentProfile) {
        this.studentProfile = studentProfile;

        if (studentProfile != null) {
            studentProfile.setUser(this);
        }
    }
}
