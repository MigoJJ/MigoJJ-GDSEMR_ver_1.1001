package com.emr.gds.server.service;

import com.emr.gds.server.dto.PatientRequest;
import com.emr.gds.server.dto.VisitRequest;
import com.emr.gds.server.model.Patient;
import com.emr.gds.server.model.Visit;
import com.emr.gds.server.repository.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PatientService {

    private final PatientRepository repository;

    public PatientService(PatientRepository repository) {
        this.repository = repository;
    }

    public List<Patient> listPatients() {
        return repository.findAll();
    }

    public Patient createPatient(PatientRequest request) {
        Patient patient = toPatient(UUID.randomUUID(), request);
        return repository.save(patient);
    }

    public Patient getPatient(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));
    }

    public Patient updatePatient(UUID id, PatientRequest request) {
        if (repository.findById(id).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found");
        }

        Patient updated = toPatient(id, request);
        return repository.save(updated);
    }

    public void deletePatient(UUID id) {
        boolean removed = repository.deleteById(id);
        if (!removed) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found");
        }
    }

    public Visit addVisit(UUID patientId, VisitRequest request) {
        Patient patient = getPatient(patientId);
        Visit newVisit = toVisit(request);

        List<Visit> visits = new ArrayList<>(patient.visits());
        visits.add(newVisit);

        Patient updated = new Patient(
                patient.id(),
                patient.firstName(),
                patient.lastName(),
                patient.dateOfBirth(),
                patient.phone(),
                visits
        );

        repository.save(updated);
        return newVisit;
    }

    public List<Visit> listVisits(UUID patientId) {
        return getPatient(patientId).visits();
    }

    private Patient toPatient(UUID id, PatientRequest request) {
        List<Visit> visits = request.visits() == null ? List.of() :
                request.visits().stream().map(this::toVisit).toList();

        return new Patient(
                id,
                request.firstName(),
                request.lastName(),
                request.dateOfBirth(),
                request.phone(),
                visits
        );
    }

    private Visit toVisit(VisitRequest request) {
        return new Visit(
                UUID.randomUUID(),
                request.occurredAt(),
                request.reason(),
                request.notes()
        );
    }
}
