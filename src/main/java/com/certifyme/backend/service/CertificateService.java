package com.certifyme.backend.service;

import com.certifyme.backend.dto.CertificateDTO;
import com.certifyme.backend.dto.VerificationResponseDTO;
import com.certifyme.backend.entity.Certificate;
import com.certifyme.backend.entity.Student;
import com.certifyme.backend.exception.DuplicateResourceException;
import com.certifyme.backend.exception.ResourceNotFoundException;
import com.certifyme.backend.repository.CertificateRepository;
import com.certifyme.backend.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final StudentRepository studentRepository;

    public CertificateService(CertificateRepository certificateRepository, StudentRepository studentRepository) {
        this.certificateRepository = certificateRepository;
        this.studentRepository = studentRepository;
    }

    public List<CertificateDTO> getAllCertificates() {
        return certificateRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public CertificateDTO getCertificateById(Long id) {
        Certificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found with id: " + id));
        return convertToDto(certificate);
    }

    public List<CertificateDTO> getCertificatesByStudentId(Long studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student not found with id: " + studentId);
        }
        return certificateRepository.findByStudentId(studentId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public CertificateDTO createCertificate(CertificateDTO dto) {
        Student student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + dto.getStudentId()));

        if (certificateRepository.existsByCertificateId(dto.getCertificateId())) {
            throw new DuplicateResourceException("Certificate ID already exists: " + dto.getCertificateId());
        }

        Certificate certificate = new Certificate();
        certificate.setCertificateId(dto.getCertificateId());
        certificate.setTitle(dto.getTitle());
        certificate.setIssuedBy(dto.getIssuedBy());
        certificate.setIssueDate(dto.getIssueDate());
        certificate.setExpiryDate(dto.getExpiryDate());
        certificate.setRevoked(false);
        certificate.setStudent(student);

        Certificate saved = certificateRepository.save(certificate);
        return convertToDto(saved);
    }

    public CertificateDTO updateCertificate(Long id, CertificateDTO dto) {
        Certificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found with id: " + id));

        if (!certificate.getCertificateId().equals(dto.getCertificateId()) && certificateRepository.existsByCertificateId(dto.getCertificateId())) {
            throw new DuplicateResourceException("Certificate ID already exists: " + dto.getCertificateId());
        }

        Student student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + dto.getStudentId()));

        certificate.setCertificateId(dto.getCertificateId());
        certificate.setTitle(dto.getTitle());
        certificate.setIssuedBy(dto.getIssuedBy());
        certificate.setIssueDate(dto.getIssueDate());
        certificate.setExpiryDate(dto.getExpiryDate());
        certificate.setStudent(student);

        Certificate updated = certificateRepository.save(certificate);
        return convertToDto(updated);
    }

    public void revokeCertificate(Long id) {
        Certificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found with id: " + id));
        certificate.setRevoked(true);
        certificateRepository.save(certificate);
    }

    public VerificationResponseDTO verifyCertificate(String certificateId) {
        Certificate certificate = certificateRepository.findByCertificateId(certificateId)
                .orElse(null);

        if (certificate == null) {
            return new VerificationResponseDTO(certificateId, "NOT_FOUND", "Certificate not found for the provided ID.", null);
        }

        if (certificate.isRevoked()) {
            return new VerificationResponseDTO(
                    certificate.getCertificateId(),
                    "REVOKED",
                    "This certificate has been revoked.",
                    certificate.getStudent().getFirstName() + " " + certificate.getStudent().getLastName()
            );
        }

        if (certificate.getExpiryDate().isBefore(LocalDate.now())) {
            return new VerificationResponseDTO(
                    certificate.getCertificateId(),
                    "EXPIRED",
                    "This certificate has expired.",
                    certificate.getStudent().getFirstName() + " " + certificate.getStudent().getLastName()
            );
        }

        return new VerificationResponseDTO(
                certificate.getCertificateId(),
                "VALID",
                "Certificate is valid and active.",
                certificate.getStudent().getFirstName() + " " + certificate.getStudent().getLastName()
        );
    }

    public void deleteCertificate(Long id) {
        if (!certificateRepository.existsById(id)) {
            throw new ResourceNotFoundException("Certificate not found with id: " + id);
        }
        certificateRepository.deleteById(id);
    }

    private CertificateDTO convertToDto(Certificate certificate) {
        return new CertificateDTO(
                certificate.getId(),
                certificate.getCertificateId(),
                certificate.getTitle(),
                certificate.getIssuedBy(),
                certificate.getIssueDate(),
                certificate.getExpiryDate(),
                certificate.isRevoked(),
                certificate.getStudent() != null ? certificate.getStudent().getId() : null
        );
    }
}
