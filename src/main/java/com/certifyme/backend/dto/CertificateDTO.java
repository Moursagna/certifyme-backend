package com.certifyme.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertFalse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class CertificateDTO {

    private Long id;

    @NotBlank(message = "Certificate ID is required")
    @Size(max = 50, message = "Certificate ID must be at most 50 characters")
    private String certificateId;

    @NotBlank(message = "Certificate title is required")
    private String title;

    @NotBlank(message = "Issue authority is required")
    private String issuedBy;

    @NotNull(message = "Issue date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate issueDate;

    @NotNull(message = "Expiry date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;

    @AssertFalse(message = "A new certificate cannot be issued as revoked")
    private Boolean revoked = false;

    @NotNull(message = "Student is required")
    private Long studentId;

    public CertificateDTO() {
    }

    public CertificateDTO(Long id, String certificateId, String title, String issuedBy, LocalDate issueDate, LocalDate expiryDate, Boolean revoked, Long studentId) {
        this.id = id;
        this.certificateId = certificateId;
        this.title = title;
        this.issuedBy = issuedBy;
        this.issueDate = issueDate;
        this.expiryDate = expiryDate;
        this.revoked = revoked;
        this.studentId = studentId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCertificateId() {
        return certificateId;
    }

    public void setCertificateId(String certificateId) {
        this.certificateId = certificateId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIssuedBy() {
        return issuedBy;
    }

    public void setIssuedBy(String issuedBy) {
        this.issuedBy = issuedBy;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    @JsonIgnore
    @AssertFalse(message = "Expiry date must not be earlier than issue date")
    public boolean isExpiryDateBeforeIssueDate() {
        return issueDate != null && expiryDate != null && expiryDate.isBefore(issueDate);
    }

    public Boolean getRevoked() {
        return revoked;
    }

    public void setRevoked(Boolean revoked) {
        this.revoked = revoked;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }
}
