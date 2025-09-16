package com.example.demo.repository;

import com.example.demo.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    
    Optional<Department> findByCode(String code);
    
    Optional<Department> findByName(String name);
    
    List<Department> findByStatus(Department.DepartmentStatus status);
    
    @Query("SELECT d FROM Department d WHERE d.name LIKE %:name% OR d.description LIKE %:description%")
    List<Department> findByNameOrDescriptionContaining(@Param("name") String name, @Param("description") String description);
    
    @Query("SELECT d FROM Department d WHERE d.status = 'ACTIVE' ORDER BY d.name")
    List<Department> findActiveDepartments();
}
