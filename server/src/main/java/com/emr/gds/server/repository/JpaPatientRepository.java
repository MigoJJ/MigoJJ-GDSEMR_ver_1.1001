package com.emr.gds.server.repository;

import com.emr.gds.server.entity.PatientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JpaPatientRepository extends JpaRepository<PatientEntity, UUID> {
}
