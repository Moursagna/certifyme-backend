package com.certifyme.backend.controller;

import com.certifyme.backend.repository.CertificateRepository;
import com.certifyme.backend.repository.StudentRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DashboardController {

    private final StudentRepository studentRepository;
    private final CertificateRepository certificateRepository;

    public DashboardController(StudentRepository studentRepository, CertificateRepository certificateRepository) {
        this.studentRepository = studentRepository;
        this.certificateRepository = certificateRepository;
    }

    @GetMapping("/dashboard")
    public Map<String, Long> getDashboardStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("students", studentRepository.count());
        stats.put("certificates", certificateRepository.count());
        return stats;
    }
}
