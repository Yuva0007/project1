package com.example.demo.repository;

import com.example.demo.model.Grievance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GrievanceRepository extends JpaRepository<Grievance, Long>, JpaSpecificationExecutor<Grievance> {
    
    Optional<Grievance> findByTrackingNumber(String trackingNumber);
    @Query("SELECT g FROM Grievance g WHERE TRIM(g.trackingNumber) = TRIM(:trackingNumber)")
    Optional<Grievance> findByTrackingNumberTrim(@Param("trackingNumber") String trackingNumber);

    @Query("SELECT g FROM Grievance g WHERE LOWER(TRIM(g.trackingNumber)) = LOWER(TRIM(:trackingNumber))")
    Optional<Grievance> findByTrackingNumberFlexible(@Param("trackingNumber") String trackingNumber);
    
    List<Grievance> findByUserId(Long userId);
    
    List<Grievance> findByDepartmentId(Long departmentId);
    
    Page<Grievance> findByDepartmentId(Long departmentId, Pageable pageable);
    
    List<Grievance> findByAssignedOfficerId(Long officerId);

    Optional<Grievance> findByIdAndUserId(Long id, Long userId);

    void deleteByIdAndUserId(Long id, Long userId);
    
    List<Grievance> findByStatus(Grievance.GrievanceStatus status);
    
    List<Grievance> findByType(Grievance.GrievanceType type);
    
    List<Grievance> findByPriority(Grievance.GrievancePriority priority);
    
    Page<Grievance> findByStatus(Grievance.GrievanceStatus status, Pageable pageable);
    
    @Query("SELECT g FROM Grievance g WHERE g.submittedAt BETWEEN :startDate AND :endDate")
    List<Grievance> findBySubmittedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT g FROM Grievance g WHERE g.expectedResolutionDate < :currentDate AND g.status NOT IN ('RESOLVED', 'CLOSED')")
    List<Grievance> findOverdueGrievances(@Param("currentDate") LocalDateTime currentDate);
    
    @Query("SELECT g FROM Grievance g WHERE g.title LIKE %:keyword% OR g.description LIKE %:keyword%")
    List<Grievance> findByKeyword(@Param("keyword") String keyword);
    
    @Query("SELECT g FROM Grievance g WHERE g.aiCategory = :category")
    List<Grievance> findByAiCategory(@Param("category") String category);
    
    @Query("SELECT COUNT(g) FROM Grievance g WHERE g.status = :status")
    Long countByStatus(@Param("status") Grievance.GrievanceStatus status);
    
    @Query("SELECT COUNT(g) FROM Grievance g WHERE g.department.id = :departmentId AND g.status = :status")
    Long countByDepartmentAndStatus(@Param("departmentId") Long departmentId, @Param("status") Grievance.GrievanceStatus status);
    
    @Query("SELECT g FROM Grievance g WHERE g.user.id = :userId ORDER BY g.submittedAt DESC")
    Page<Grievance> findByUserIdOrderBySubmittedAtDesc(@Param("userId") Long userId, Pageable pageable);
}
