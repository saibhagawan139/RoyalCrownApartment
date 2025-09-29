package com.royalcrown.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.royalcrown.model.VisitLog;

@Repository
public interface VisitLogRepository extends JpaRepository<VisitLog, Long> {

    List<VisitLog> findBySecurityGuardUsernameAndVisitTimeBetween(
            String securityGuardUsername,
            LocalDateTime from,
            LocalDateTime to
    );

    // New repository method to fetch all visits by date range
    List<VisitLog> findByVisitTimeBetween(
            LocalDateTime from,
            LocalDateTime to
    );

    void deleteByVisitTimeBetween(
            LocalDateTime from,
            LocalDateTime to
    );

    long countByVisitTimeBetween(
            LocalDateTime from,
            LocalDateTime to
    );
}