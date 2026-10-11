package com.certifyme.backend.service;

import com.certifyme.backend.dto.StudentDTO;
import com.certifyme.backend.entity.Student;
import com.certifyme.backend.exception.DuplicateResourceException;
import com.certifyme.backend.exception.ResourceNotFoundException;
import com.certifyme.backend.repository.CertificateRepository;
import com.certifyme.backend.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final CertificateRepository certificateRepository;

    public StudentService(StudentRepository studentRepository, CertificateRepository certificateRepository) {
        this.studentRepository = studentRepository;
        this.certificateRepository = certificateRepository;
    }

    public List<StudentDTO> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public StudentDTO getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
        return convertToDto(student);
    }

    public StudentDTO createStudent(StudentDTO dto) {
        if (studentRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("A student with this email already exists.");
        }

        Student student = new Student();
        student.setFirstName(dto.getFirstName());
        student.setLastName(dto.getLastName());
        student.setEmail(dto.getEmail());
        student.setPhone(dto.getPhone());
        student.setDepartment(dto.getDepartment());

        Student savedStudent = studentRepository.save(student);
        return convertToDto(savedStudent);
    }

    public StudentDTO updateStudent(Long id, StudentDTO dto) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));

        if (!student.getEmail().equalsIgnoreCase(dto.getEmail()) && studentRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("A student with this email already exists.");
        }

        student.setFirstName(dto.getFirstName());
        student.setLastName(dto.getLastName());
        student.setEmail(dto.getEmail());
        student.setPhone(dto.getPhone());
        student.setDepartment(dto.getDepartment());

        Student updatedStudent = studentRepository.save(student);
        return convertToDto(updatedStudent);
    }

    public void deleteStudent(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Student not found with id: " + id);
        }
        if (certificateRepository.existsByStudentId(id)) {
            throw new DuplicateResourceException(
                    "Cannot delete this student because they have associated certificates."
            );
        }
        studentRepository.deleteById(id);
    }

    private StudentDTO convertToDto(Student student) {
        return new StudentDTO(
                student.getId(),
                student.getFirstName(),
                student.getLastName(),
                student.getEmail(),
                student.getPhone(),
                student.getDepartment()
        );
    }
}
