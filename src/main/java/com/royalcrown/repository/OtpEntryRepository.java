package com.royalcrown.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.royalcrown.model.OtpEntry;

@Repository
public interface OtpEntryRepository extends JpaRepository<OtpEntry, Long> {
    List<OtpEntry> findByFlatNoAndIsValidTrue(String flatNo);
    Optional<OtpEntry> findByFlatNoAndOtpCodeAndIsValidTrue(String flatNo, String otpCode);
    List<OtpEntry> findByIsValidTrueAndExpiresAtBefore(LocalDateTime now);

}
