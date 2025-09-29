package com.royalcrown.service;

import com.royalcrown.config.JwtProvider;
import com.royalcrown.model.Flat;
import com.royalcrown.model.OtpEntry;
import com.royalcrown.model.User;
import com.royalcrown.model.VisitLog;
import com.royalcrown.repository.FlatRepository;
import com.royalcrown.repository.OtpEntryRepository;
import com.royalcrown.repository.UserRepository;
import com.royalcrown.repository.VisitLogRepository;
import com.royalcrown.util.OtpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ApartmentSecurityService {

    private static final Logger logger = LoggerFactory.getLogger(ApartmentSecurityService.class);

    private static final Duration OTP_VALIDITY_DURATION = Duration.ofMinutes(10);
    private static final Duration PASSWORD_CHANGE_INTERVAL = Duration.ofDays(30);

    private final UserRepository userRepository;
    private final FlatRepository flatRepository;
    private final OtpEntryRepository otpEntryRepository;
    private final VisitLogRepository visitLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public ApartmentSecurityService(UserRepository userRepository,
                                    FlatRepository flatRepository,
                                    OtpEntryRepository otpEntryRepository,
                                    VisitLogRepository visitLogRepository,
                                    PasswordEncoder passwordEncoder,
                                    JwtProvider jwtProvider) {
        this.userRepository = userRepository;
        this.flatRepository = flatRepository;
        this.otpEntryRepository = otpEntryRepository;
        this.visitLogRepository = visitLogRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    // ------------------ AUTHENTICATION & SESSION --------------------

    @Transactional
    public String login(String username, String rawPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Invalid username or password."));
        if (!user.isActive()) {
            throw new RuntimeException("User account is deactivated.");
        }
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new RuntimeException("Invalid username or password.");
        }
        return jwtProvider.generateToken(username, user.getRole());
    }

    @Transactional
    public void logout(String username, String deviceId) {
    }

    // ------------------ USER REGISTRATION & MANAGEMENT --------------------

    @Transactional
    public User registerUser(User user) {
        logger.info("Attempting to register user: {}", user.getUsername());

        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username '" + user.getUsername() + "' already exists.");
        }

        validateUserFlatNo(user);

        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        user.setPasswordChangedAt(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));  // IST only
        user.setActive(true);

        User savedUser = userRepository.save(user);
        logger.info("User '{}' registered successfully.", savedUser.getUsername());
        return savedUser;
    }

    @Transactional
    public List<User> registerUsersBulk(List<User> users) {
        logger.info("Bulk registration attempt for {} users", users.size());

        Set<String> usernamesInBatch = new HashSet<>();
        List<String> duplicates = new ArrayList<>();
        for (User user : users) {
            if (!usernamesInBatch.add(user.getUsername())) duplicates.add(user.getUsername());
            if (userRepository.existsByUsername(user.getUsername())) duplicates.add(user.getUsername());
        }
        if (!duplicates.isEmpty()) {
            throw new RuntimeException("Duplicate usernames: " + String.join(", ", duplicates));
        }

        users.forEach(this::validateUserFlatNo);
        users.forEach(u -> {
            u.setPasswordHash(passwordEncoder.encode(u.getPasswordHash()));
            u.setPasswordChangedAt(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));  // IST
            u.setActive(true);
        });

        List<User> saved = userRepository.saveAll(users);
        logger.info("Bulk registration completed for {} users", saved.size());
        return saved;
    }

    private void validateUserFlatNo(User user) {
        if ("SECURITY_GUARD".equalsIgnoreCase(user.getRole())) {
            if (!user.getFlatNo().matches("SE\\d+")) {
                throw new RuntimeException("Security guards must have flatNo like 'SE1', 'SE2', etc.");
            }
        } else {
            if (!flatRepository.existsById(user.getFlatNo())) {
                throw new RuntimeException("FlatNo '" + user.getFlatNo() + "' does not exist.");
            }
        }
    }

    @Transactional
    public void changePassword(String username, String currentPassword, String newPassword, boolean isAdminReset) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        if (!user.isActive()) {
            throw new RuntimeException("User account is deactivated.");
        }
        if (!isAdminReset) {
            if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
                throw new RuntimeException("Current password is incorrect.");
            }
            LocalDateTime lastChanged = user.getPasswordChangedAt();
            if (lastChanged != null && lastChanged.plus(PASSWORD_CHANGE_INTERVAL).isAfter(LocalDateTime.now(ZoneId.of("Asia/Kolkata")))) {
                throw new RuntimeException("Password can only be changed once every 30 days.");
            }
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));  // IST
        userRepository.save(user);

        logger.info("Password changed for user '{}', admin reset={}", username, isAdminReset);
    }


    @Transactional
    public void adminResetPassword(String targetUsername, String newPassword) {
        changePassword(targetUsername, null, newPassword, true);
    }

    @Transactional
    public void setActiveStatus(String username, boolean active) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        user.setActive(active);
        userRepository.save(user);
        logger.info("User '{}' is now {}", username, active ? "activated" : "deactivated");
    }

    @Transactional
    public void deleteUser(String username) {
        if (!userRepository.existsByUsername(username)) {
            throw new RuntimeException("User not found: " + username);
        }

        // Use custom delete method instead of deleteById
        userRepository.deleteByUsername(username);
        logger.info("User '{}' deleted", username);
    }


    @Transactional
    public void deleteUsersBulk(List<String> usernames) {
        List<String> missing = usernames.stream()
                .filter(u -> !userRepository.existsByUsername(u))
                .collect(Collectors.toList());
        if (!missing.isEmpty()) {
            throw new RuntimeException("Users not found: " + String.join(", ", missing));
        }
        userRepository.deleteAllById(usernames);
        logger.info("Bulk deleted {} users", usernames.size());
    }

    public User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    public List<User> getUsersByFlatNo(String flatNo) {
        if (!flatRepository.existsById(flatNo)) {
            throw new RuntimeException("Flat not found: " + flatNo);
        }
        return userRepository.findByFlatNo(flatNo);
    }

    // ------------------ FLAT MANAGEMENT ---------------------

    public List<Flat> getAllFlats() {
        return flatRepository.findAll();
    }

    public Flat getFlat(String flatNo) {
        return flatRepository.findById(flatNo)
                .orElseThrow(() -> new RuntimeException("Flat not found: " + flatNo));
    }

    @Transactional
    public Flat updateFlat(String flatNo, Flat updatedFlat) {
        Flat existing = getFlat(flatNo);
        if (updatedFlat.getFlatNo() != null && !flatNo.equals(updatedFlat.getFlatNo())) {
            throw new RuntimeException("Cannot change flatNo of a flat.");
        }
        existing.setPresident(updatedFlat.isPresident());
        flatRepository.save(existing);
        logger.info("Flat '{}' updated", flatNo);
        return existing;
    }

    @Transactional
    public void assignUserToFlat(String flatNo, String username, String role) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        if (!flatRepository.existsById(flatNo)) {
            throw new RuntimeException("Flat not found: " + flatNo);
        }
        user.setFlatNo(flatNo);
        user.setRole(role.toUpperCase());
        userRepository.save(user);
        logger.info("Assigned user '{}' to flat '{}' with role '{}'", username, flatNo, role);
    }

    // ------------------ OTP MANAGEMENT ---------------------

    @Transactional
    public OtpEntry generateOtp(String flatNo, String guestType) {
        if (!flatRepository.existsById(flatNo)) {
            throw new RuntimeException("Flat not found: " + flatNo);
        }

        String otpCode = OtpUtil.generateOtpCode();
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));  // IST only

        OtpEntry otpEntry = new OtpEntry();
        otpEntry.setFlatNo(flatNo);
        otpEntry.setGuestType(guestType);
        otpEntry.setOtpCode(otpCode);
        otpEntry.setIssuedAt(now);
        otpEntry.setExpiresAt(now.plus(OTP_VALIDITY_DURATION));
        otpEntry.setValid(true);

        OtpEntry saved = otpEntryRepository.save(otpEntry);
        logger.info("Generated OTP {} for flat '{}', guestType '{}', expires at {}", 
                    saved.getOtpCode(), flatNo, guestType, saved.getExpiresAt());
        return saved;
    }

    @Transactional
    public boolean validateOtp(String flatNo, String otpCode, String securityGuardUsername) {
        User guard = userRepository.findByUsername(securityGuardUsername)
                .orElseThrow(() -> new RuntimeException("Security guard not found"));
        if (!"SECURITY_GUARD".equalsIgnoreCase(guard.getRole())) {
            throw new RuntimeException("User is not authorized as security guard.");
        }

        invalidateExpiredOtps();

        OtpEntry otp = otpEntryRepository
                .findByFlatNoAndOtpCodeAndIsValidTrue(flatNo, otpCode)
                .orElseThrow(() -> new RuntimeException("Invalid or expired OTP."));

        if (otp.getExpiresAt().isBefore(LocalDateTime.now(ZoneId.of("Asia/Kolkata")))) {
            otp.setValid(false);
            otpEntryRepository.save(otp);
            throw new RuntimeException("OTP has expired.");
        }

        otp.setValid(false);
        otpEntryRepository.save(otp);

        VisitLog visit = new VisitLog();
        visit.setFlatNo(flatNo);
        visit.setOwnerUsername(getOwnerUsernameByFlat(flatNo));
        visit.setVisitorType(otp.getGuestType());
        visit.setSecurityGuardUsername(securityGuardUsername);
        visit.setVisitTime(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));  // Always IST
        visit.setOtpCode(otpCode);
        visitLogRepository.save(visit);

        logger.info("OTP validated for flat '{}', visit logged by '{}'", flatNo, securityGuardUsername);
        return true;
    }


    private String getOwnerUsernameByFlat(String flatNo) {
        List<User> users = userRepository.findByFlatNo(flatNo);
        return users.stream()
                .filter(u -> "OWNER".equalsIgnoreCase(u.getRole()) || "PRESIDENT".equalsIgnoreCase(u.getRole()))
                .map(User::getUsername)
                .findFirst()
                .orElse("unknown");
    }

    @Transactional
    public int invalidateExpiredOtps() {
        LocalDateTime now = LocalDateTime.now();
        List<OtpEntry> expiredOtps = otpEntryRepository.findByIsValidTrueAndExpiresAtBefore(now);
        for (OtpEntry otp : expiredOtps) {
            otp.setValid(false);
        }
        otpEntryRepository.saveAll(expiredOtps);
        if (!expiredOtps.isEmpty()) {
            logger.info("Invalidated {} expired OTPs", expiredOtps.size());
        }
        return expiredOtps.size();
    }

    // ------------------ VISIT LOG MANAGEMENT ---------------------

    public List<VisitLog> listVisits(String securityGuardUsername, LocalDateTime from, LocalDateTime to) {
        List<VisitLog> visits;

        if (securityGuardUsername != null && !securityGuardUsername.isBlank()) {
            // Fetch visits filtered by security guard username
            visits = visitLogRepository.findBySecurityGuardUsernameAndVisitTimeBetween(
                    securityGuardUsername, from, to);
        } else {
            // Fetch all visits within date range if username is not used
            visits = visitLogRepository.findByVisitTimeBetween(from, to);
        }

        return visits; // no extra filtering needed now
    }

    @Transactional
    public long deleteVisits(LocalDateTime from, LocalDateTime to) {
        long count = visitLogRepository.countByVisitTimeBetween(from, to);
        visitLogRepository.deleteByVisitTimeBetween(from, to);
        logger.info("Deleted {} visit logs from {} to {}", count, from, to);
        return count;
    }

    @Scheduled(fixedRate = 600000) // every 10 minutes
    @Transactional
    public void scheduledInvalidateExpiredOtps() {
        int count = invalidateExpiredOtps();
        if (count > 0) {
            logger.info("Scheduled task invalidated {} expired OTPs", count);
        }
    }

    // ------------------ GET CURRENT USER PROFILE -----------------

    public User getCurrentUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        user.setPasswordHash(null);
        return user;
    }

    public long getUserCount() {
        return userRepository.count();
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
