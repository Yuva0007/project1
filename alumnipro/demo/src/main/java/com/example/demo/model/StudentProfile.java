package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "student_profiles")
public class StudentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String department;

    private int yearOfStudy;

    private String registrationNumber;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @JsonBackReference
    private User user;

    public StudentProfile() {}

    public StudentProfile(String department, int yearOfStudy, String registrationNumber, User user) {
        this.department = department;
        this.yearOfStudy = yearOfStudy;
        this.registrationNumber = registrationNumber;
        this.user = user;
    }

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getDepartment() { return department; }

    public void setDepartment(String department) { this.department = department; }

    public int getYearOfStudy() { return yearOfStudy; }

    public void setYearOfStudy(int yearOfStudy) { this.yearOfStudy = yearOfStudy; }

    public String getRegistrationNumber() { return registrationNumber; }

    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }

    public User getUser() { return user; }

    public void setUser(User user) { this.user = user; }
}
