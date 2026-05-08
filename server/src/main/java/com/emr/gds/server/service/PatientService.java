package com.emr.gds.server.service;

import com.emr.gds.server.dto.PatientRequest;
import com.emr.gds.server.dto.VisitRequest;
import com.emr.gds.server.entity.PatientEntity;
import com.emr.gds.server.entity.VisitEntity;
import com.emr.gds.server.model.Patient;
import com.emr.gds.server.model.Visit;
import com.emr.gds.server.repository.JpaPatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PatientService {

    private final JpaPatientRepository repository;

    public PatientService(JpaPatientRepository repository) {
        this.repository = repository;
    }

    public List<Patient> listPatients() {
        return repository.findAll().stream()
                .map(this::toModel)
                .toList();
    }

    public Patient createPatient(PatientRequest request) {
        PatientEntity entity = new PatientEntity();
        updateEntity(entity, request);
        PatientEntity saved = repository.save(entity);
        return toModel(saved);
    }

    public Patient getPatient(UUID id) {
        return repository.findById(id)
                .map(this::toModel)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));
    }

    public Patient updatePatient(UUID id, PatientRequest request) {
        PatientEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));
        
        updateEntity(entity, request);
        PatientEntity saved = repository.save(entity);
        return toModel(saved);
    }

    public void deletePatient(UUID id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found");
        }
        repository.deleteById(id);
    }

    public Visit addVisit(UUID patientId, VisitRequest request) {
        PatientEntity patient = repository.findById(patientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));
        
        VisitEntity visit = new VisitEntity();
        visit.setOccurredAt(request.occurredAt());
        visit.setReason(request.reason());
        visit.setNotes(request.notes());
        
        patient.addVisit(visit);
        repository.save(patient);
        
        return toVisitModel(visit);
    }

    public List<Visit> listVisits(UUID patientId) {
        PatientEntity patient = repository.findById(patientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));
        
        return patient.getVisits().stream()
                .map(this::toVisitModel)
                .toList();
    }

    private void updateEntity(PatientEntity entity, PatientRequest request) {
        entity.setFirstName(request.firstName());
        entity.setLastName(request.lastName());
        entity.setDateOfBirth(request.dateOfBirth());
        entity.setPhone(request.phone());
        
        if (request.visits() != null) {
            // In a real app, we might want to reconcile visits instead of just adding new ones
            // But for this prototype, we'll just handle the basic fields.
        }
    }

    private Patient toModel(PatientEntity entity) {
        List<Visit> visits = entity.getVisits().stream()
                .map(this::toVisitModel)
                .toList();

        return new Patient(
                entity.getId(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getDateOfBirth(),
                entity.getPhone(),
                visits
        );
    }

    private Visit toVisitModel(VisitEntity entity) {
        return new Visit(
                entity.getId(),
                entity.getOccurredAt(),
                entity.getReason(),
                entity.getNotes()
        );
    }
}
