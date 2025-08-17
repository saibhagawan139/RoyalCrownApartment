package com.royalcrown.controller;

import com.royalcrown.model.VisitLog;
import com.royalcrown.service.ApartmentSecurityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping
public class VisitLogController {
    private static final Logger logger = LoggerFactory.getLogger(VisitLogController.class);
    private final ApartmentSecurityService service;

    public VisitLogController(ApartmentSecurityService service) {
        this.service = service;
    }

    @GetMapping("/security/visits")
    @PreAuthorize("hasAnyRole('PRESIDENT','ADMIN')")
    public ResponseEntity<?> listVisits(Authentication authentication,
            @RequestParam(name = "fromDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fromDate,

            @RequestParam(name = "toDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime toDate,

            @RequestParam(name = "flatNo", required = false) String flatNo,
            @RequestParam(name = "ownerName", required = false) String ownerName) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
            }

            com.royalcrown.model.User user = (com.royalcrown.model.User) authentication.getPrincipal();

            // ✅ Convert to IST LocalDateTime
            LocalDateTime fromIst = fromDate.atZoneSameInstant(ZoneId.of("Asia/Kolkata")).toLocalDateTime();
            LocalDateTime toIst   = toDate.atZoneSameInstant(ZoneId.of("Asia/Kolkata")).toLocalDateTime();

            List<VisitLog> visits = service.listVisits(
                    user.getUsername(),
                    fromIst,
                    toIst,
                    Optional.ofNullable(flatNo),
                    Optional.ofNullable(ownerName));

            return ResponseEntity.ok(visits);
        } catch (RuntimeException e) {
            logger.error("List visits error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @DeleteMapping("/security/visits")
    @PreAuthorize("hasAnyRole('PRESIDENT','ADMIN')")
    public ResponseEntity<?> deleteVisits(@RequestBody Map<String, String> request) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

            ZonedDateTime fromDate = ZonedDateTime.parse(request.get("fromDate"), formatter);
            ZonedDateTime toDate = ZonedDateTime.parse(request.get("toDate"), formatter);

            // ✅ Convert to IST before querying DB
            LocalDateTime from = fromDate.withZoneSameInstant(ZoneId.of("Asia/Kolkata")).toLocalDateTime();
            LocalDateTime to = toDate.withZoneSameInstant(ZoneId.of("Asia/Kolkata")).toLocalDateTime();

            long count = service.deleteVisits(from, to);
            return ResponseEntity.ok(Map.of("deletedCount", count));
        } catch (RuntimeException e) {
            logger.error("Delete visits error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    
//    @GetMapping("/security/visits")
//    @PreAuthorize("hasAnyRole('PRESIDENT','ADMIN')")
//    public ResponseEntity<?> listVisits(Authentication authentication,
//            @RequestParam(name = "fromDate") 
//            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
//
//            @RequestParam(name = "toDate") 
//            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
//
//            @RequestParam(name = "flatNo", required = false) String flatNo,
//            @RequestParam(name = "ownerName", required = false) String ownerName) {
//        try {
//            if (authentication == null || !authentication.isAuthenticated()) {
//                return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
//            }
//
//            com.royalcrown.model.User user = (com.royalcrown.model.User) authentication.getPrincipal();
//            List<VisitLog> visits = service.listVisits(
//                    user.getUsername(),
//                    fromDate,
//                    toDate,
//                    Optional.ofNullable(flatNo),
//                    Optional.ofNullable(ownerName));
//
//            return ResponseEntity.ok(visits);
//        } catch (RuntimeException e) {
//            logger.error("List visits error: {}", e.getMessage());
//            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
//        }
//    }
//
//    @DeleteMapping("/security/visits")
//    @PreAuthorize("hasAnyRole('PRESIDENT','ADMIN')")
//    public ResponseEntity<?> deleteVisits(@RequestBody Map<String, String> request) {
//        try {
//            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
//
//            LocalDateTime fromDate = LocalDateTime.parse(request.get("fromDate"), formatter);
//            LocalDateTime toDate = LocalDateTime.parse(request.get("toDate"), formatter);
//
//            long count = service.deleteVisits(fromDate, toDate);
//            return ResponseEntity.ok(Map.of("deletedCount", count));
//        } catch (RuntimeException e) {
//            logger.error("Delete visits error: {}", e.getMessage());
//            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
//        }
//    }

}
