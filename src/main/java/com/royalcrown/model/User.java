package com.royalcrown.model;

import java.time.LocalDateTime;
import java.time.ZoneId;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "users")
public class User {
    @Id
    @NotBlank
    @Column(nullable = false, unique = true)
    private String username;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Column(nullable = false)
    private String passwordHash;

    @NotBlank
    @Column(nullable = false)
    private String role;

    @NotBlank
    @Column(nullable = false)
    private String flatNo;

    @Column(nullable = false)
    private boolean isActive = true;

    private LocalDateTime passwordChangedAt;

    public User() {}

    public User(String username, String name, String passwordHash, String role, String flatNo, boolean isActive) {
        this.username = username;
        this.name = name;
        this.passwordHash = passwordHash;
        this.role = role;
        this.flatNo = flatNo;
        this.isActive = isActive;
        this.passwordChangedAt = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
    }

    // Getters and setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getFlatNo() { return flatNo; }
    public void setFlatNo(String flatNo) { this.flatNo = flatNo; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

	public LocalDateTime getPasswordChangedAt() {
		return passwordChangedAt;
	}

	public void setPasswordChangedAt(LocalDateTime passwordChangedAt) {
		this.passwordChangedAt = passwordChangedAt;
	}

  
}
