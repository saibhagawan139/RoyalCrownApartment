package com.royalcrown.controller;

import com.royalcrown.model.User;
import com.royalcrown.service.ApartmentSecurityService;

import jakarta.annotation.security.PermitAll;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/user")
@PreAuthorize("hasAnyRole('PRESIDENT','ADMIN')")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class UserAdminController {

    private static final Logger logger = LoggerFactory.getLogger(UserAdminController.class);
    private final ApartmentSecurityService service;

    public UserAdminController(ApartmentSecurityService service) {
        this.service = service;
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<?> deleteUser(@PathVariable("username") String username) {
        try {
            service.deleteUser(username);
            return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
        } catch (RuntimeException e) {
            logger.error("Delete user error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @PostMapping("/delete-batch")
    public ResponseEntity<?> deleteUsersBatch(@RequestBody List<String> usernames) {
        try {
            service.deleteUsersBulk(usernames);
            return ResponseEntity.ok(Map.of("message", "Users deleted"));
        } catch (RuntimeException e) {
            logger.error("Bulk delete error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{username}/reset-password")
    public ResponseEntity<?> resetPassword(@PathVariable String username,
                                           @RequestBody Map<String, String> passwordRequest) {
        try {
            service.adminResetPassword(username, passwordRequest.get("newPassword"));
            return ResponseEntity.ok(Map.of("message", "Password reset successful"));
        } catch (RuntimeException e) {
            logger.error("Password reset error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{username}/deactivate")
    public ResponseEntity<?> deactivateUser(@PathVariable("username") String username,
                                            @RequestBody(required = false) Map<String, Boolean> statusRequest) {
        try {
            boolean active = statusRequest == null || !Boolean.FALSE.equals(statusRequest.get("active"));
            service.setActiveStatus(username, active);
            return ResponseEntity.ok(Map.of("message", "User " + (active ? "activated" : "deactivated")));
        } catch (RuntimeException e) {
            logger.error("Deactivate/reactivate error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{username}")
    public ResponseEntity<?> getUser(@PathVariable String username) {
        try {
            User user = service.getUser(username);
            user.setPasswordHash(null);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            logger.error("Get user error: {}", e.getMessage());
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getUsersByFlatNo(@RequestParam("flatNo") String flatNo) {
        try {
            List<User> users = service.getUsersByFlatNo(flatNo);
            users.forEach(u -> u.setPasswordHash(null));
            return ResponseEntity.ok(users);
        } catch (RuntimeException e) {
            logger.error("Get users by flatNo error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
