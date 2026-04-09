package com.emr.gds.server.controller;

import com.emr.gds.server.dto.PatientRequest;
import com.emr.gds.server.dto.PatientResponse;
import com.emr.gds.server.dto.VisitRequest;
import com.emr.gds.server.dto.VisitResponse;
import com.emr.gds.server.model.Patient;
import com.emr.gds.server.model.Visit;
import com.emr.gds.server.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    public List<PatientResponse> listPatients() {
        return patientService.listPatients()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PatientResponse createPatient(@Valid @RequestBody PatientRequest request) {
        Patient patient = patientService.createPatient(request);
        return toResponse(patient);
    }

    @GetMapping("/{id}")
    public PatientResponse getPatient(@PathVariable UUID id) {
        Patient patient = patientService.getPatient(id);
        return toResponse(patient);
    }

    @PutMapping("/{id}")
    public PatientResponse updatePatient(@PathVariable UUID id, @Valid @RequestBody PatientRequest request) {
        Patient updated = patientService.updatePatient(id, request);
        return toResponse(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePatient(@PathVariable UUID id) {
        patientService.deletePatient(id);
    }

    @GetMapping("/{id}/visits")
    public List<VisitResponse> listVisits(@PathVariable UUID id) {
        return patientService.listVisits(id).stream()
                .map(this::toVisitResponse)
                .toList();
    }

    @PostMapping("/{id}/visits")
    @ResponseStatus(HttpStatus.CREATED)
    public VisitResponse addVisit(@PathVariable UUID id, @Valid @RequestBody VisitRequest request) {
        Visit visit = patientService.addVisit(id, request);
        return toVisitResponse(visit);
    }

    private PatientResponse toResponse(Patient patient) {
        List<VisitResponse> visits = patient.visits().stream()
                .map(this::toVisitResponse)
                .toList();

        return new PatientResponse(
                patient.id(),
                patient.firstName(),
                patient.lastName(),
                patient.dateOfBirth(),
                patient.phone(),
                visits
        );
    }

    private VisitResponse toVisitResponse(Visit visit) {
        return new VisitResponse(
                visit.id(),
                visit.occurredAt(),
                visit.reason(),
                visit.notes()
        );
    }
}
