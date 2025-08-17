package com.royalcrown.controller;

import com.royalcrown.model.Flat;
import com.royalcrown.service.ApartmentSecurityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/flat")
@PreAuthorize("hasAnyRole('PRESIDENT','ADMIN')")
public class FlatAdminController {
    private static final Logger logger = LoggerFactory.getLogger(FlatAdminController.class);
    private final ApartmentSecurityService service;

    public FlatAdminController(ApartmentSecurityService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Flat>> listAllFlats() {
        return ResponseEntity.ok(service.getAllFlats());
    }

    @GetMapping("/{flatNo}")
    public ResponseEntity<?> getFlat(@PathVariable("flatNo") String flatNo) {
        try {
            Flat flat = service.getFlat(flatNo);
            return ResponseEntity.ok(flat);
        } catch (RuntimeException ex) {
            logger.error("Get flat error: {}", ex.getMessage());
            return ResponseEntity.status(404).body(Map.of("error", ex.getMessage()));
        }
    }

    @PutMapping("/{flatNo}")
    public ResponseEntity<?> updateFlat(@PathVariable("flatNo") String flatNo, @RequestBody Flat updatedFlat) {
        try {
            Flat flat = service.updateFlat(flatNo, updatedFlat);
            return ResponseEntity.ok(flat);
        } catch (RuntimeException ex) {
            logger.error("Update flat error: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/{flatNo}/assign-user")
    public ResponseEntity<?> assignUserToFlat(@PathVariable("flatNo") String flatNo,
                                              @RequestBody Map<String, String> assignRequest) {
        try {
            String username = assignRequest.get("username");
            String role = assignRequest.get("role");
            if (username == null || role == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "username and role are required"));
            }
            service.assignUserToFlat(flatNo, username, role);
            return ResponseEntity.ok(Map.of("message", "User assigned"));
        } catch (RuntimeException ex) {
            logger.error("Assign user error: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

}
