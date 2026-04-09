package com.emr.gds.server.repository;

import com.emr.gds.server.model.Patient;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class InMemoryPatientRepository implements PatientRepository {

    private final ConcurrentMap<UUID, Patient> store = new ConcurrentHashMap<>();

    @Override
    public List<Patient> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public Optional<Patient> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Patient save(Patient patient) {
        store.put(patient.id(), patient);
        return patient;
    }

    @Override
    public boolean deleteById(UUID id) {
        return store.remove(id) != null;
    }
}
