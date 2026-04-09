package com.emr.gds.server.repository;

import com.emr.gds.server.model.Patient;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PatientRepository {
    List<Patient> findAll();

    Optional<Patient> findById(UUID id);

    Patient save(Patient patient);

    boolean deleteById(UUID id);
}
