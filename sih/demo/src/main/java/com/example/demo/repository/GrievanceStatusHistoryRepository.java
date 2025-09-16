package com.example.demo.repository;

import com.example.demo.model.GrievanceStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GrievanceStatusHistoryRepository extends JpaRepository<GrievanceStatusHistory, Long> {

    @Query("SELECT h FROM GrievanceStatusHistory h WHERE h.grievance.id = :grievanceId ORDER BY h.changedAt ASC")
    List<GrievanceStatusHistory> findByGrievanceIdOrderByChangedAt(@Param("grievanceId") Long grievanceId);
}


