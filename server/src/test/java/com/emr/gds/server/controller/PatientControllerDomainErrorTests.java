package com.emr.gds.server.controller;

import com.emr.gds.server.api.ApiExceptionHandler;
import com.emr.gds.server.dto.PatientRequest;
import com.emr.gds.server.repository.PatientRepository;
import com.emr.gds.server.service.PatientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PatientController.class)
@Import({ApiExceptionHandler.class, PatientService.class})
class PatientControllerDomainErrorTests {

    @Autowired
    private org.springframework.test.web.servlet.MockMvc mockMvc;

    @MockBean
    private PatientRepository patientRepository;

    @Test
    void getPatient_notFoundHasContract() throws Exception {
        UUID missing = UUID.randomUUID();
        when(patientRepository.findById(missing)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/patients/{id}", missing))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("patient_not_found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/api/v1/patients/" + missing))
                .andExpect(jsonPath("$.message").value("Patient not found"))
                .andExpect(jsonPath("$.details[0]").value("Patient not found"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void updatePatient_notFoundHasContract() throws Exception {
        UUID missing = UUID.randomUUID();
        when(patientRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        String body = """
                {
                  "firstName": "John",
                  "lastName": "Doe"
                }
                """;

        mockMvc.perform(put("/api/v1/patients/{id}", missing)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("patient_not_found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/api/v1/patients/" + missing))
                .andExpect(jsonPath("$.message").value("Patient not found"))
                .andExpect(jsonPath("$.details[0]").value("Patient not found"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void deletePatient_notFoundHasContract() throws Exception {
        UUID missing = UUID.randomUUID();
        when(patientRepository.deleteById(missing)).thenReturn(false);

        mockMvc.perform(delete("/api/v1/patients/{id}", missing))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("patient_not_found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/api/v1/patients/" + missing))
                .andExpect(jsonPath("$.message").value("Patient not found"))
                .andExpect(jsonPath("$.details[0]").value("Patient not found"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
