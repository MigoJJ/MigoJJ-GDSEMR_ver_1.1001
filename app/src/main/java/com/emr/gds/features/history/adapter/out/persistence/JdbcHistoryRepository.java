package com.emr.gds.features.history.adapter.out.persistence;

import com.emr.gds.core.db.AppDatabaseManager;
import com.emr.gds.features.history.domain.ConditionCategory;
import com.emr.gds.features.history.domain.HistoryPersistenceException;
import com.emr.gds.features.history.domain.HistoryRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class JdbcHistoryRepository implements HistoryRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcHistoryRepository.class);

    // 테스트에서 인메모리 DB를 주입하기 위한 seam
    private final Connection testConnection;

    public JdbcHistoryRepository() {
        this.testConnection = null;
        initializeDatabase();
    }

    /** 테스트 전용 생성자 — 프로덕션 코드에서 호출 금지. */
    JdbcHistoryRepository(Connection testConnection) {
        this.testConnection = testConnection;
        initializeDatabase();
    }

    private Connection conn() throws SQLException {
        return testConnection != null
                ? testConnection
                : AppDatabaseManager.getInstance().getHistoryConnection();
    }

    private void initializeDatabase() {
        String sql = "CREATE TABLE IF NOT EXISTS conditions (" +
                     "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                     "category TEXT NOT NULL, " +
                     "name TEXT NOT NULL, " +
                     "UNIQUE(category, name))";
        try (Statement stmt = conn().createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            LOGGER.error("히스토리 DB 초기화 실패", e);
            throw new HistoryPersistenceException("Failed to initialize history database.", e);
        }
    }

    @Override
    public List<String> getConditionsByCategory(ConditionCategory category) {
        List<String> results = new ArrayList<>();
        String sql = "SELECT name FROM conditions WHERE category = ? ORDER BY name";
        
        try (PreparedStatement pstmt = conn().prepareStatement(sql)) {

            pstmt.setString(1, category.name());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(rs.getString("name"));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("조건 목록 로드 실패: {}", category, e);
            throw new HistoryPersistenceException("Failed to load conditions for " + category + ".", e);
        }
        return results;
    }

    @Override
    public void addCondition(ConditionCategory category, String conditionName) {
        String sql = "INSERT OR IGNORE INTO conditions (category, name) VALUES (?, ?)";
        
        try (PreparedStatement pstmt = conn().prepareStatement(sql)) {

            pstmt.setString(1, category.name());
            pstmt.setString(2, conditionName);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            LOGGER.error("조건 추가 실패: {} in {}", conditionName, category, e);
            throw new HistoryPersistenceException("Failed to add condition '" + conditionName + "' for " + category + ".", e);
        }
    }

    @Override
    public void updateCondition(ConditionCategory category, String oldName, String newName) {
        String sql = "UPDATE conditions SET name = ? WHERE category = ? AND name = ?";
        try (PreparedStatement pstmt = conn().prepareStatement(sql)) {

            pstmt.setString(1, newName);
            pstmt.setString(2, category.name());
            pstmt.setString(3, oldName);
            int rows = pstmt.executeUpdate();
            
            if (rows == 0) {
                throw new HistoryPersistenceException("Condition '" + oldName + "' not found in category " + category);
            }
            
        } catch (SQLException e) {
            LOGGER.error("조건 수정 실패: {} → {}", oldName, newName, e);
            throw new HistoryPersistenceException("Failed to update condition.", e);
        }
    }

    @Override
    public void deleteCondition(ConditionCategory category, String conditionName) {
        String sql = "DELETE FROM conditions WHERE category = ? AND name = ?";
        try (PreparedStatement pstmt = conn().prepareStatement(sql)) {

            pstmt.setString(1, category.name());
            pstmt.setString(2, conditionName);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            LOGGER.error("조건 삭제 실패: {}", conditionName, e);
            throw new HistoryPersistenceException("Failed to delete condition.", e);
        }
    }
}
