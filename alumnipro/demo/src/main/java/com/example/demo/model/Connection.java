package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "connections")
public class Connection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "alumni_id", nullable = false)
    @JsonIgnoreProperties({"user"})
    private AlumniProfile alumni;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIgnoreProperties({"user"})
    private StudentProfile student;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum Status {
        PENDING,
        ACCEPTED,
        REJECTED
    }

    public Connection() {}

    public Connection(AlumniProfile alumni, StudentProfile student, Status status) {
        this.alumni = alumni;
        this.student = student;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public AlumniProfile getAlumni() {
        return alumni;
    }

    public void setAlumni(AlumniProfile alumni) {
        this.alumni = alumni;
    }

    public StudentProfile getStudent() {
        return student;
    }

    public void setStudent(StudentProfile student) {
        this.student = student;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
