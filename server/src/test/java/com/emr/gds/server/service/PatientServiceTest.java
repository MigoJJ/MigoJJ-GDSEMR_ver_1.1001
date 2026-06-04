package com.emr.gds.server.service;

import com.emr.gds.server.dto.PatientRequest;
import com.emr.gds.server.entity.PatientEntity;
import com.emr.gds.server.model.Patient;
import com.emr.gds.server.repository.JpaPatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private JpaPatientRepository repository;

    private PatientService service;

    @BeforeEach
    void setUp() {
        service = new PatientService(repository);
    }

    // 편의 팩토리
    private PatientEntity entity(String firstName, String lastName) {
        PatientEntity e = new PatientEntity();
        e.setFirstName(firstName);
        e.setLastName(lastName);
        e.setDateOfBirth(LocalDate.of(1985, 3, 15));
        e.setPhone("010-1234-5678");
        return e;
    }

    private PatientRequest request(String first, String last) {
        return new PatientRequest(first, last, LocalDate.of(1985, 3, 15), "010-1234-5678", null);
    }

    // ── listPatients ─────────────────────────────────────────────────────

    @Test
    void listPatients_emptyRepository_returnsEmptyList() {
        when(repository.findAll()).thenReturn(List.of());
        assertThat(service.listPatients()).isEmpty();
    }

    @Test
    void listPatients_delegatesToRepository() {
        PatientEntity e = entity("Hong", "Gildong");
        when(repository.findAll()).thenReturn(List.of(e));

        List<Patient> result = service.listPatients();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).firstName()).isEqualTo("Hong");
    }

    // ── createPatient ─────────────────────────────────────────────────────

    @Test
    void createPatient_savesAndReturnsPatient() {
        PatientEntity saved = entity("Hong", "Gildong");
        when(repository.save(any())).thenReturn(saved);

        Patient result = service.createPatient(request("Hong", "Gildong"));

        verify(repository).save(any(PatientEntity.class));
        assertThat(result.firstName()).isEqualTo("Hong");
        assertThat(result.lastName()).isEqualTo("Gildong");
    }

    @Test
    void createPatient_setsAllFieldsFromRequest() {
        PatientEntity saved = entity("Kim", "Cheolsu");
        when(repository.save(any())).thenReturn(saved);

        service.createPatient(request("Kim", "Cheolsu"));

        verify(repository).save(argThat(e ->
                "Kim".equals(e.getFirstName()) &&
                "Cheolsu".equals(e.getLastName())
        ));
    }

    // ── getPatient ────────────────────────────────────────────────────────

    @Test
    void getPatient_existingId_returnsPatient() {
        UUID id = UUID.randomUUID();
        PatientEntity e = entity("Lee", "Younghee");
        when(repository.findById(id)).thenReturn(Optional.of(e));

        Patient result = service.getPatient(id);

        assertThat(result.firstName()).isEqualTo("Lee");
    }

    @Test
    void getPatient_unknownId_throwsNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getPatient(id))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Patient not found");
    }

    // ── updatePatient ─────────────────────────────────────────────────────

    @Test
    void updatePatient_existingId_updatesAndReturns() {
        UUID id = UUID.randomUUID();
        PatientEntity existing = entity("Old", "Name");
        PatientEntity updated = entity("New", "Name");
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenReturn(updated);

        Patient result = service.updatePatient(id, request("New", "Name"));

        assertThat(result.firstName()).isEqualTo("New");
    }

    @Test
    void updatePatient_unknownId_throwsNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updatePatient(id, request("Any", "Name")))
                .isInstanceOf(ResponseStatusException.class);
    }

    // ── deletePatient ─────────────────────────────────────────────────────

    @Test
    void deletePatient_existingId_deletesSuccessfully() {
        UUID id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(true);

        service.deletePatient(id);

        verify(repository).deleteById(id);
    }

    @Test
    void deletePatient_unknownId_throwsNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> service.deletePatient(id))
                .isInstanceOf(ResponseStatusException.class);
    }
}
