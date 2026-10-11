package com.certifyme.backend.controller;

import com.certifyme.backend.dto.CertificateDTO;
import com.certifyme.backend.dto.VerificationResponseDTO;
import com.certifyme.backend.service.CertificateService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/certificates")
public class CertificateController {

    private final CertificateService certificateService;

    public CertificateController(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    @GetMapping
    public ResponseEntity<List<CertificateDTO>> getAllCertificates() {
        return ResponseEntity.ok(certificateService.getAllCertificates());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CertificateDTO> getCertificateById(@PathVariable Long id) {
        return ResponseEntity.ok(certificateService.getCertificateById(id));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<CertificateDTO>> getCertificatesByStudentId(@PathVariable Long studentId) {
        return ResponseEntity.ok(certificateService.getCertificatesByStudentId(studentId));
    }

    @PostMapping
    public ResponseEntity<CertificateDTO> createCertificate(@Valid @RequestBody CertificateDTO certificateDTO) {
        CertificateDTO saved = certificateService.createCertificate(certificateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CertificateDTO> updateCertificate(@PathVariable Long id, @Valid @RequestBody CertificateDTO certificateDTO) {
        return ResponseEntity.ok(certificateService.updateCertificate(id, certificateDTO));
    }

    @PutMapping("/{id}/revoke")
    public ResponseEntity<Void> revokeCertificate(@PathVariable Long id) {
        certificateService.revokeCertificate(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCertificate(@PathVariable Long id) {
        certificateService.deleteCertificate(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/verify/{certificateId}")
    public ResponseEntity<VerificationResponseDTO> verifyCertificate(@PathVariable String certificateId) {
        return ResponseEntity.ok(certificateService.verifyCertificate(certificateId));
    }
}
