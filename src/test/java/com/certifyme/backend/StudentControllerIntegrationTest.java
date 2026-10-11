package com.certifyme.backend;

import com.certifyme.backend.dto.StudentDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class StudentControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("Should create a student with valid request")
    void shouldCreateStudent() throws Exception {
        StudentDTO dto = new StudentDTO();
        dto.setFirstName("Asha");
        dto.setLastName("Reddy");
        dto.setEmail("asha@example.com");
        dto.setPhone("9876543210");
        dto.setDepartment("ECE");

        mockMvc.perform(post("/api/students")
                        .header("Origin", "http://localhost:5173")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(jsonPath("$.firstName").value("Asha"))
                .andExpect(jsonPath("$.email").value("asha@example.com"));
    }

    @Test
    @DisplayName("Should allow the Vite development origin to read students")
    void shouldAllowViteOrigin() throws Exception {
        mockMvc.perform(get("/api/students").header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    @Test
    @DisplayName("Should allow the Vite origin to preflight a JSON POST")
    void shouldAllowViteOriginPreflight() throws Exception {
        mockMvc.perform(options("/api/students")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Methods",
                        org.hamcrest.Matchers.allOf(
                                org.hamcrest.Matchers.containsString("GET"),
                                org.hamcrest.Matchers.containsString("POST"),
                                org.hamcrest.Matchers.containsString("PUT"),
                                org.hamcrest.Matchers.containsString("DELETE"),
                                org.hamcrest.Matchers.containsString("OPTIONS"))))
                .andExpect(header().string("Access-Control-Allow-Headers",
                        org.hamcrest.Matchers.containsStringIgnoringCase("content-type")));
    }

    @Test
    @DisplayName("Should create and verify a certificate for a student")
    void shouldCreateAndVerifyCertificate() throws Exception {
        StudentDTO dto = new StudentDTO();
        dto.setFirstName("Certificate");
        dto.setLastName("Integration");
        dto.setEmail("certificate-" + UUID.randomUUID() + "@example.test");
        dto.setPhone("9876543210");
        dto.setDepartment("QA");

        String studentJson = mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        StudentDTO createdStudent = objectMapper.readValue(studentJson, StudentDTO.class);
        String certificateId = "CERT-" + UUID.randomUUID();
        Long createdCertificateId = null;

        try {
            String certificateJson = objectMapper.writeValueAsString(Map.of(
                    "certificateId", certificateId,
                    "title", "Integration Certificate",
                    "issuedBy", "CertifyMe QA",
                    "issueDate", "2026-10-11",
                    "expiryDate", "2027-10-11",
                    "studentId", createdStudent.getId()
            ));

            String createdCertificate = mockMvc.perform(post("/api/certificates")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(certificateJson))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.certificateId").value(certificateId))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            createdCertificateId = objectMapper.readTree(createdCertificate).get("id").asLong();

            mockMvc.perform(get("/api/certificates/verify/{certificateId}", certificateId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("VALID"))
                    .andExpect(jsonPath("$.studentName").value("Certificate Integration"));
        } finally {
            if (createdCertificateId != null) {
                mockMvc.perform(delete("/api/certificates/{id}", createdCertificateId));
            }
            mockMvc.perform(delete("/api/students/{id}", createdStudent.getId()));
        }
    }

    @Test
    @DisplayName("Should create certificates as not revoked when revoked is omitted, null, or false")
    void shouldDefaultNewCertificatesToNotRevoked() throws Exception {
        StudentDTO dto = new StudentDTO();
        dto.setFirstName("Default");
        dto.setLastName("Revocation");
        dto.setEmail("revocation-" + UUID.randomUUID() + "@example.test");
        dto.setPhone("9876543210");
        dto.setDepartment("QA");

        String studentJson = mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        StudentDTO createdStudent = objectMapper.readValue(studentJson, StudentDTO.class);
        List<Long> createdCertificateIds = new ArrayList<>();

        try {
            Map<String, Object> baseRequest = new HashMap<>();
            baseRequest.put("title", "Default Revocation Certificate");
            baseRequest.put("issuedBy", "CertifyMe QA");
            baseRequest.put("issueDate", "2026-10-11");
            baseRequest.put("expiryDate", "2027-10-11");
            baseRequest.put("studentId", createdStudent.getId());

            List<Map<String, Object>> requests = new ArrayList<>();
            requests.add(new HashMap<>(baseRequest));

            Map<String, Object> nullRevokedRequest = new HashMap<>(baseRequest);
            nullRevokedRequest.put("revoked", null);
            requests.add(nullRevokedRequest);

            Map<String, Object> falseRevokedRequest = new HashMap<>(baseRequest);
            falseRevokedRequest.put("revoked", false);
            requests.add(falseRevokedRequest);

            for (Map<String, Object> request : requests) {
                request.put("certificateId", "CERT-" + UUID.randomUUID());
                String response = mockMvc.perform(post("/api/certificates")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.revoked").value(false))
                        .andExpect(jsonPath("$.studentId").value(createdStudent.getId()))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
                createdCertificateIds.add(objectMapper.readTree(response).get("id").asLong());
            }

            Map<String, Object> revokedRequest = new HashMap<>(baseRequest);
            revokedRequest.put("certificateId", "CERT-" + UUID.randomUUID());
            revokedRequest.put("revoked", true);
            mockMvc.perform(post("/api/certificates")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(revokedRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.revoked").value("A new certificate cannot be issued as revoked"));
        } finally {
            for (Long certificateId : createdCertificateIds) {
                mockMvc.perform(delete("/api/certificates/{id}", certificateId));
            }
            mockMvc.perform(delete("/api/students/{id}", createdStudent.getId()));
        }
    }

    @Test
    @DisplayName("Should distinguish valid, expired, revoked, and nonexistent certificates")
    void shouldVerifyCertificateStatusWithExpiryAndRevocationPrecedence() throws Exception {
        StudentDTO dto = createStudentDto("verification");
        StudentDTO createdStudent = createStudent(dto);
        List<Long> certificateIds = new ArrayList<>();

        try {
            LocalDate today = LocalDate.now();
            String validCertificateId = createCertificate(createdStudent.getId(), today, today, certificateIds);
            String expiredCertificateId = createCertificate(createdStudent.getId(), today.minusDays(2), today.minusDays(1), certificateIds);
            String revokedExpiredCertificateId = createCertificate(
                    createdStudent.getId(),
                    today.minusDays(3),
                    today.minusDays(2),
                    certificateIds
            );

            mockMvc.perform(put("/api/certificates/{id}/revoke", certificateIds.get(2)))
                    .andExpect(status().isNoContent());
            Map<String, Object> updateRequest = createCertificateRequest(
                    revokedExpiredCertificateId,
                    createdStudent.getId(),
                    today.minusDays(3),
                    today.minusDays(2)
            );
            mockMvc.perform(put("/api/certificates/{id}", certificateIds.get(2))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.revoked").value(true));

            mockMvc.perform(get("/api/certificates/verify/{certificateId}", validCertificateId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("VALID"));
            mockMvc.perform(get("/api/certificates/verify/{certificateId}", expiredCertificateId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("EXPIRED"));
            mockMvc.perform(get("/api/certificates/verify/{certificateId}", revokedExpiredCertificateId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("REVOKED"));
            mockMvc.perform(get("/api/certificates/verify/{certificateId}", "UNKNOWN-" + UUID.randomUUID()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("NOT_FOUND"));
        } finally {
            deleteTestRecords(certificateIds, createdStudent.getId());
        }
    }

    @Test
    @DisplayName("Should prevent deleting students who have associated certificates")
    void shouldPreventDeletingStudentWithCertificates() throws Exception {
        StudentDTO createdStudent = createStudent(createStudentDto("delete-guard"));
        List<Long> certificateIds = new ArrayList<>();

        try {
            createCertificate(createdStudent.getId(), LocalDate.now(), LocalDate.now().plusDays(30), certificateIds);

            mockMvc.perform(delete("/api/students/{id}", createdStudent.getId()))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value(
                            "Cannot delete this student because they have associated certificates."
                    ));
            mockMvc.perform(get("/api/students/{id}", createdStudent.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(createdStudent.getId()));
            mockMvc.perform(get("/api/certificates/{id}", certificateIds.get(0)))
                    .andExpect(status().isOk());
        } finally {
            deleteTestRecords(certificateIds, createdStudent.getId());
        }
    }

    @Test
    @DisplayName("Should reject certificate IDs longer than 50 characters and invalid date ranges")
    void shouldValidateCertificateIdLengthAndDateRange() throws Exception {
        StudentDTO createdStudent = createStudent(createStudentDto("certificate-validation"));

        try {
            Map<String, Object> oversizedIdRequest = createCertificateRequest(
                    "C".repeat(51),
                    createdStudent.getId(),
                    LocalDate.now(),
                    LocalDate.now().plusDays(1)
            );
            mockMvc.perform(post("/api/certificates")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(oversizedIdRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.certificateId").value("Certificate ID must be at most 50 characters"));

            Map<String, Object> invalidDatesRequest = createCertificateRequest(
                    "CERT-" + UUID.randomUUID(),
                    createdStudent.getId(),
                    LocalDate.now(),
                    LocalDate.now().minusDays(1)
            );
            mockMvc.perform(post("/api/certificates")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDatesRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.expiryDate").value("Expiry date must not be earlier than issue date"));
        } finally {
            mockMvc.perform(delete("/api/students/{id}", createdStudent.getId()));
        }
    }

    private StudentDTO createStudentDto(String emailPrefix) {
        StudentDTO dto = new StudentDTO();
        dto.setFirstName("Audit");
        dto.setLastName("Fixture");
        dto.setEmail(emailPrefix + "-" + UUID.randomUUID() + "@example.test");
        dto.setPhone("9876543210");
        dto.setDepartment("QA");
        return dto;
    }

    private StudentDTO createStudent(StudentDTO dto) throws Exception {
        String response = mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readValue(response, StudentDTO.class);
    }

    private String createCertificate(
            Long studentId,
            LocalDate issueDate,
            LocalDate expiryDate,
            List<Long> createdCertificateIds
    ) throws Exception {
        Map<String, Object> request = createCertificateRequest(
                "CERT-" + UUID.randomUUID(),
                studentId,
                issueDate,
                expiryDate
        );
        String response = mockMvc.perform(post("/api/certificates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        createdCertificateIds.add(objectMapper.readTree(response).get("id").asLong());
        return objectMapper.readTree(response).get("certificateId").asText();
    }

    private Map<String, Object> createCertificateRequest(
            String certificateId,
            Long studentId,
            LocalDate issueDate,
            LocalDate expiryDate
    ) {
        return Map.of(
                "certificateId", certificateId,
                "title", "Integration Certificate",
                "issuedBy", "CertifyMe QA",
                "issueDate", issueDate.toString(),
                "expiryDate", expiryDate.toString(),
                "studentId", studentId
        );
    }

    private void deleteTestRecords(List<Long> certificateIds, Long studentId) throws Exception {
        for (Long certificateId : certificateIds) {
            mockMvc.perform(delete("/api/certificates/{id}", certificateId))
                    .andExpect(status().isNoContent());
        }
        mockMvc.perform(delete("/api/students/{id}", studentId))
                .andExpect(status().isNoContent());
    }
}
