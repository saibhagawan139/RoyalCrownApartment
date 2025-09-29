package com.royalcrown.controller;

import com.royalcrown.model.OtpEntry;
import com.royalcrown.model.User;
import com.royalcrown.service.ApartmentSecurityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/otp")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class OtpController {
    private static final Logger logger = LoggerFactory.getLogger(OtpController.class);
    private final ApartmentSecurityService service;

    public OtpController(ApartmentSecurityService service) {
        this.service = service;
    }
    
    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('OWNER','TENANT')")
    public ResponseEntity<?> generateOtp(Authentication authentication,
                                         @RequestBody Map<String, String> request) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
            }
            User user = (User) authentication.getPrincipal();
            String guestType = request.get("guestType");
            if (guestType == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "guestType is required"));
            }

            // Always use the flatNo from the authenticated user!
            String flatNo = user.getFlatNo();

            OtpEntry otpEntry = service.generateOtp(flatNo, guestType);
            return ResponseEntity.ok(otpEntry);
        } catch (RuntimeException e) {
            logger.error("OTP generation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/validate")
    @PreAuthorize("hasRole('SECURITY_GUARD')")
    public ResponseEntity<?> validateOtp(Authentication authentication,
                                         @RequestBody Map<String, String> request) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
            }
            String flatNo = request.get("flatNo");
            String otpCode = request.get("otpCode");
            if (flatNo == null || otpCode == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "flatNo and otpCode are required"));
            }
            com.royalcrown.model.User user = (com.royalcrown.model.User) authentication.getPrincipal();
            boolean valid = service.validateOtp(flatNo, otpCode, user.getUsername());
            return ResponseEntity.ok(Map.of("valid", valid));
        } catch (RuntimeException e) {
            logger.error("OTP validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


//    @PostMapping("/validate")
//    @PreAuthorize("hasRole('SECURITY_GUARD')")
//    public ResponseEntity<?> validateOtp(@AuthenticationPrincipal UserDetails userDetails,
//                                         @RequestBody Map<String, String> request) {
//        try {
//            String flatNo = request.get("flatNo");
//            String otpCode = request.get("otpCode");
//            if (flatNo == null || otpCode == null)
//                return ResponseEntity.badRequest().body(Map.of("error", "flatNo and otpCode are required"));
//
//            boolean valid = service.validateOtp(flatNo, otpCode, userDetails.getUsername());
//            return ResponseEntity.ok(Map.of("valid", valid));
//        } catch (RuntimeException e) {
//            logger.error("OTP validation error: {}", e.getMessage());
//            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
//        }
//    }
}
