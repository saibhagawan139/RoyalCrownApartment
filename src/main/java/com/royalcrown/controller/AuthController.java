package com.royalcrown.controller;

import com.royalcrown.model.User;
import com.royalcrown.service.ApartmentSecurityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final ApartmentSecurityService service;

    public AuthController(ApartmentSecurityService service) {
        this.service = service;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        try {
            String username = loginRequest.get("username");
            String password = loginRequest.get("password");
            String token = service.login(username, password);
            return ResponseEntity.ok(Map.of("token", token));
        } catch (RuntimeException ex) {
            logger.error("Login failed: {}", ex.getMessage());
            return ResponseEntity.status(401).body(Map.of("error", "Login failed", "message", ex.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
            }
            User user = (User) authentication.getPrincipal();
            service.logout(user.getUsername(), null);
            return ResponseEntity.ok(Map.of("message", "Logout successful"));
        } catch (RuntimeException ex) {
            logger.error("Logout error: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }


    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            long userCount = service.getUserCount();

            if (userCount > 0) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("error", "Unauthorized: Please login as PRESIDENT or ADMIN to register users"));
                }
                boolean hasRole = auth.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_PRESIDENT") || a.getAuthority().equals("ROLE_ADMIN"));
                if (!hasRole) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("error", "Access denied: Only PRESIDENT or ADMIN can register new users"));
                }
            }

            User registered = service.registerUser(user);
            registered.setPasswordHash(null);
            return ResponseEntity.status(HttpStatus.CREATED).body(registered);

        } catch (RuntimeException ex) {
            logger.error("User registration failed: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/register-batch")
    public ResponseEntity<?> registerUsersBulk(@RequestBody List<User> users) {
        try {
            List<User> registered = service.registerUsersBulk(users);
            registered.forEach(u -> u.setPasswordHash(null));
            return ResponseEntity.status(HttpStatus.CREATED).body(registered);
        } catch (RuntimeException ex) {
            logger.error("Bulk user registration failed: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
            }
            User user = (User) authentication.getPrincipal();
            user.setPasswordHash(null); // Hide password hash
            return ResponseEntity.ok(user);
        } catch (RuntimeException ex) {
            logger.error("Error fetching current user: {}", ex.getMessage());
            return ResponseEntity.status(404).body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(Authentication authentication,
                                            @RequestBody Map<String, String> passwords) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
            }
            User user = (User) authentication.getPrincipal();

            service.changePassword(
                    user.getUsername(),
                    passwords.get("currentPassword"),
                    passwords.get("newPassword"),
                    false);

            return ResponseEntity.ok(Map.of(
                    "message", "Password changed successfully",
                    "flatNo", user.getFlatNo()
            ));
        } catch (RuntimeException ex) {
            logger.error("Password change failed: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }
}
