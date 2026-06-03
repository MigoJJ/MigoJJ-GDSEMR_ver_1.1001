package com.emr.gds.repository.sqlite;

import com.emr.gds.core.db.AppDatabaseManager;
import com.emr.gds.features.ReferenceFile.ReferenceItem;
import com.emr.gds.repository.ReferenceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteReferenceRepository implements ReferenceRepository {

    private static final Logger logger = LoggerFactory.getLogger(SqliteReferenceRepository.class);

    private final AppDatabaseManager dbManager;
    private static final String DB_FILE_NAME = "references.db";

    public SqliteReferenceRepository(AppDatabaseManager dbManager) {
        this.dbManager = dbManager;
        createTable();
    }

    private Connection getConnection() throws SQLException {
        return dbManager.getReferenceConnection();
    }

    private void createTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS "references" (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                category TEXT NOT NULL,
                contents TEXT NOT NULL,
                directory_path TEXT NOT NULL
            );
            """;
        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            logger.error("references 테이블 생성 실패", e);
        }
    }

    @Override
    public ReferenceItem save(ReferenceItem item) {
        String sql;
        if (item.getId() == 0) { // New item
            sql = "INSERT INTO \"references\" (category, contents, directory_path) VALUES (?, ?, ?)";
        } else { // Existing item
            sql = "UPDATE \"references\" SET category = ?, contents = ?, directory_path = ? WHERE id = ?";
        }

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, item.getCategory());
            pstmt.setString(2, item.getContents());
            pstmt.setString(3, item.getDirectoryPath());

            if (item.getId() != 0) {
                pstmt.setInt(4, item.getId());
            }

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0 && item.getId() == 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        item.setId(rs.getInt(1)); // Set the generated ID
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("참조 항목 저장 실패: id={}", item.getId(), e);
        }
        return item;
    }

    @Override
    public void delete(ReferenceItem item) {
        if (item.getId() == 0) {
            logger.warn("ID 없는 참조 항목 삭제 시도 - 무시됨");
            return;
        }
        String sql = "DELETE FROM \"references\" WHERE id = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, item.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("참조 항목 삭제 실패: id={}", item.getId(), e);
        }
    }

    @Override
    public List<ReferenceItem> findAll() {
        List<ReferenceItem> items = new ArrayList<>();
        String sql = "SELECT id, category, contents, directory_path FROM \"references\" ORDER BY category";
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                items.add(new ReferenceItem(
                    rs.getInt("id"),
                    rs.getString("category"),
                    rs.getString("contents"),
                    rs.getString("directory_path")
                ));
            }
        } catch (SQLException e) {
            logger.error("참조 항목 전체 조회 실패", e);
        }
        return items;
    }

    @Override
    public Optional<ReferenceItem> findById(int id) {
        String sql = "SELECT id, category, contents, directory_path FROM \"references\" WHERE id = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new ReferenceItem(
                        rs.getInt("id"),
                        rs.getString("category"),
                        rs.getString("contents"),
                        rs.getString("directory_path")
                    ));
                }
            }
        } catch (SQLException e) {
            logger.error("참조 항목 ID 조회 실패: id={}", id, e);
        }
        return Optional.empty();
    }
}
