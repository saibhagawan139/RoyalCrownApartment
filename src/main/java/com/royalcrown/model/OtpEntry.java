package com.royalcrown.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "otp_entries")
public class OtpEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String flatNo;

    @NotBlank
    @Column(nullable = false)
    private String otpCode;

    @NotBlank
    @Column(nullable = false)
    private String guestType;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime issuedAt;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean isValid;

    public OtpEntry() {}

    public OtpEntry(Long id, String flatNo, String otpCode, String guestType, LocalDateTime issuedAt, LocalDateTime expiresAt, boolean isValid) {
        this.id = id;
        this.flatNo = flatNo;
        this.otpCode = otpCode;
        this.guestType = guestType;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.isValid = isValid;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFlatNo() { return flatNo; }
    public void setFlatNo(String flatNo) { this.flatNo = flatNo; }

    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }

    public String getGuestType() { return guestType; }
    public void setGuestType(String guestType) { this.guestType = guestType; }

    

    public LocalDateTime getIssuedAt() {
		return issuedAt;
	}

	public void setIssuedAt(LocalDateTime issuedAt) {
		this.issuedAt = issuedAt;
	}

	public LocalDateTime getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(LocalDateTime expiresAt) {
		this.expiresAt = expiresAt;
	}

	public boolean isValid() { return isValid; }
    public void setValid(boolean valid) { isValid = valid; }
}
