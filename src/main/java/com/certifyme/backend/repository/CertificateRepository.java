package com.certifyme.backend.repository;

import com.certifyme.backend.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    Optional<Certificate> findByCertificateId(String certificateId);
    boolean existsByCertificateId(String certificateId);
    boolean existsByStudentId(Long studentId);
    List<Certificate> findByStudentId(Long studentId);
}
