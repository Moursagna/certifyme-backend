package com.certifyme.backend.dto;

public class VerificationResponseDTO {
    private String certificateId;
    private String status;
    private String message;
    private String studentName;

    public VerificationResponseDTO() {
    }

    public VerificationResponseDTO(String certificateId, String status, String message, String studentName) {
        this.certificateId = certificateId;
        this.status = status;
        this.studentName = studentName;
        this.message = message;
    }

    public String getCertificateId() {
        return certificateId;
    }

    public void setCertificateId(String certificateId) {
        this.certificateId = certificateId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }
}
