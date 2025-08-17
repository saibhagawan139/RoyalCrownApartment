package com.royalcrown.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.royalcrown.model.VisitLog;

@Repository
public interface VisitLogRepository extends JpaRepository<VisitLog, Long> {

    List<VisitLog> findBySecurityGuardUsernameAndVisitTimeBetween(
            String securityGuardUsername,
            LocalDateTime from,
            LocalDateTime to
    );

    List<VisitLog> findByFlatNoAndVisitTimeBetween(
            String flatNo,
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

