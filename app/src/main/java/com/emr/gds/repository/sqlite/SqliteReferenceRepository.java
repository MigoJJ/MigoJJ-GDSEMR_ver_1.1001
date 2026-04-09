package com.emr.gds.repository.sqlite;

import com.emr.gds.core.db.AppDatabaseManager;
import com.emr.gds.features.ReferenceFile.ReferenceItem;
import com.emr.gds.repository.ReferenceRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteReferenceRepository implements ReferenceRepository {

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
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Error creating references table: " + e.getMessage());
            e.printStackTrace();
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

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

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
            System.err.println("Error saving reference item: " + e.getMessage());
            e.printStackTrace();
        }
        return item;
    }

    @Override
    public void delete(ReferenceItem item) {
        if (item.getId() == 0) {
            System.err.println("Cannot delete reference item without an ID.");
            return;
        }
        String sql = "DELETE FROM \"references\" WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, item.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting reference item: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<ReferenceItem> findAll() {
        List<ReferenceItem> items = new ArrayList<>();
        String sql = "SELECT id, category, contents, directory_path FROM \"references\" ORDER BY category";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
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
            System.err.println("Error finding all reference items: " + e.getMessage());
            e.printStackTrace();
        }
        return items;
    }

    @Override
    public Optional<ReferenceItem> findById(int id) {
        String sql = "SELECT id, category, contents, directory_path FROM \"references\" WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
            System.err.println("Error finding reference item by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return Optional.empty();
    }
}
