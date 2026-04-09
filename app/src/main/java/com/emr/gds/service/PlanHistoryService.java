package com.emr.gds.service;

import com.emr.gds.domain.PlanHistoryEntry;
import com.emr.gds.repository.PlanHistoryRepository;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Use-case service for storing plan history.
 */
public class PlanHistoryService {

    private final PlanHistoryRepository repository;

    public PlanHistoryService(PlanHistoryRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository");
    }

    public void initialize() throws SQLException {
        repository.init();
    }

    public void save(String section, String content, String patientId, String encounterDate) throws SQLException {
        PlanHistoryEntry entry = new PlanHistoryEntry(section, content, patientId, encounterDate, LocalDateTime.now());
        repository.save(entry);
    }
}
