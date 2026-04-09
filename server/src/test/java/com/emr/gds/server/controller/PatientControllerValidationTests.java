package com.emr.gds.server.controller;

import com.emr.gds.server.api.ApiExceptionHandler;
import com.emr.gds.server.repository.PatientRepository;
import com.emr.gds.server.service.PatientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PatientController.class)
@Import({ApiExceptionHandler.class, PatientService.class, PatientControllerValidationTests.TestConfig.class})
class PatientControllerValidationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PatientRepository patientRepository;

    @Test
    void createPatient_requiresNames() throws Exception {
        String body = objectMapper.writeValueAsString(new java.util.HashMap<>());

        mockMvc.perform(post("/api/v1/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("validation_failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/api/v1/patients"))
                .andExpect(jsonPath("$.details", containsInAnyOrder(
                        "firstName: First name is required",
                        "lastName: Last name is required"
                )))
                .andExpect(jsonPath("$.timestamp").exists());

    }

    @Test
    void createPatient_validatesNestedVisit() throws Exception {
        String body = """
                {
                  "firstName": "John",
                  "lastName": "Doe",
                  "visits": [
                    {
                      "occurredAt": "3025-01-01T10:00:00",
                      "reason": ""
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/v1/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("validation_failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/api/v1/patients"))
                .andExpect(jsonPath("$.details", containsInAnyOrder(
                        "visits[0].occurredAt: Visit time cannot be in the future",
                        "visits[0].reason: Visit reason is required"
                )))
                .andExpect(jsonPath("$.timestamp").exists());

    }

    @TestConfiguration
    static class TestConfig {
        // Additional test beans can be declared here if needed.
    }
}
