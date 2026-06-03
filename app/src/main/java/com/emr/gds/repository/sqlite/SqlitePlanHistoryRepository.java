package com.emr.gds.repository.sqlite;

import com.emr.gds.core.db.AppDatabaseManager;
import com.emr.gds.domain.PlanHistoryEntry;
import com.emr.gds.repository.PlanHistoryRepository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * SQLite-backed repository for plan history.
 * Uses the shared connection from AppDatabaseManager (plan_history.db).
 */
public class SqlitePlanHistoryRepository implements PlanHistoryRepository {

    @Override
    public void init() throws SQLException {
        try (Statement st = AppDatabaseManager.getInstance().getPlanHistoryConnection().createStatement()) {
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS plan_history (" +
                "id INTEGER PRIMARY KEY, created_at TEXT NOT NULL, " +
                "section TEXT, content TEXT, patient_id TEXT, encounter_date TEXT)"
            );
        }
    }

    @Override
    public void save(PlanHistoryEntry entry) throws SQLException {
        String sql = "INSERT INTO plan_history (created_at, section, content, patient_id, encounter_date) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = AppDatabaseManager.getInstance().getPlanHistoryConnection().prepareStatement(sql)) {
            ps.setString(1, entry.createdAt().toString());
            ps.setString(2, entry.section());
            ps.setString(3, entry.content());
            ps.setString(4, entry.patientId());
            ps.setString(5, entry.encounterDate());
            ps.executeUpdate();
        }
    }
}
