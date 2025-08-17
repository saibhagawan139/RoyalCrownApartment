package com.royalcrown.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "visit_logs")
public class VisitLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String flatNo;

    @NotBlank
    @Column(nullable = false)
    private String ownerUsername;

    @NotBlank
    @Column(nullable = false)
    private String visitorType;

    @NotBlank
    @Column(nullable = false)
    private String securityGuardUsername;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime  visitTime;

    @NotBlank
    @Column(nullable = false)
    private String otpCode;

    public VisitLog() {}

    public VisitLog(Long id, String flatNo, String ownerUsername, String visitorType, String securityGuardUsername, LocalDateTime  visitTime, String otpCode) {
        this.id = id;
        this.flatNo = flatNo;
        this.ownerUsername = ownerUsername;
        this.visitorType = visitorType;
        this.securityGuardUsername = securityGuardUsername;
        this.visitTime = visitTime;
        this.otpCode = otpCode;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFlatNo() { return flatNo; }
    public void setFlatNo(String flatNo) { this.flatNo = flatNo; }

    public String getOwnerUsername() { return ownerUsername; }
    public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }

    public String getVisitorType() { return visitorType; }
    public void setVisitorType(String visitorType) { this.visitorType = visitorType; }

    public String getSecurityGuardUsername() { return securityGuardUsername; }
    public void setSecurityGuardUsername(String securityGuardUsername) { this.securityGuardUsername = securityGuardUsername; }

    public LocalDateTime getVisitTime() {
		return visitTime;
	}

	public void setVisitTime(LocalDateTime visitTime) {
		this.visitTime = visitTime;
	}

	public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }
}
