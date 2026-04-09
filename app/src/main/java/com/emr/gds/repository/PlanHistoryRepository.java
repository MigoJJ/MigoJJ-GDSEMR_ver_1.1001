package com.emr.gds.repository;

import com.emr.gds.domain.PlanHistoryEntry;
import java.sql.SQLException;

/**
 * Repository for storing plan history entries.
 */
public interface PlanHistoryRepository {
    void init() throws SQLException;

    void save(PlanHistoryEntry entry) throws SQLException;
}
