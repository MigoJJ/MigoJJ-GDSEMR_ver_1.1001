package com.emr.gds.repository.sqlite;

import com.emr.gds.core.db.AppDatabaseManager;
import com.emr.gds.repository.ProblemRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLite-backed repository for the persistent problem list.
 * Uses the shared connection from AppDatabaseManager (prolist.db).
 */
public class SqliteProblemRepository implements ProblemRepository {

    @Override
    public void init() throws SQLException {
        try (Statement stmt = AppDatabaseManager.getInstance().getProblemConnection().createStatement()) {
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS problems " +
                "(id INTEGER PRIMARY KEY AUTOINCREMENT, problem_text TEXT NOT NULL UNIQUE)"
            );
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS count FROM problems")) {
                if (rs.next() && rs.getInt("count") == 0) {
                    stmt.execute("INSERT INTO problems (problem_text) VALUES ('Hypercholesterolemia [F/U]')");
                    stmt.execute("INSERT INTO problems (problem_text) VALUES ('Prediabetes (FBS 108 mg/dL)')");
                    stmt.execute("INSERT INTO problems (problem_text) VALUES ('Thyroid nodule (small)')");
                }
            }
        }
    }

    @Override
    public List<String> findAll() throws SQLException {
        List<String> results = new ArrayList<>();
        String sql = "SELECT problem_text FROM problems ORDER BY problem_text COLLATE NOCASE";
        try (Statement stmt = AppDatabaseManager.getInstance().getProblemConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                results.add(rs.getString("problem_text"));
            }
        }
        return results;
    }

    @Override
    public void insert(String problemText) throws SQLException {
        String sql = "INSERT INTO problems(problem_text) VALUES(?)";
        try (PreparedStatement pstmt = AppDatabaseManager.getInstance().getProblemConnection().prepareStatement(sql)) {
            pstmt.setString(1, problemText);
            pstmt.executeUpdate();
        }
    }

    @Override
    public boolean delete(String problemText) throws SQLException {
        String sql = "DELETE FROM problems WHERE problem_text = ?";
        try (PreparedStatement pstmt = AppDatabaseManager.getInstance().getProblemConnection().prepareStatement(sql)) {
            pstmt.setString(1, problemText);
            return pstmt.executeUpdate() > 0;
        }
    }
}
