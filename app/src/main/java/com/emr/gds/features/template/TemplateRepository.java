package com.emr.gds.features.template;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TemplateRepository {

    private static final Logger logger = LoggerFactory.getLogger(TemplateRepository.class);
    private static final String DB_FILENAME = "emr_templates.db";

    private Connection conn;

    public TemplateRepository() {
        try {
            initConnection();
            createTableIfNotExists();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize template database", e);
        }
    }

    private void initConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        Path dbPath = getDbPath();
        try {
            if (!Files.exists(dbPath.getParent())) {
                Files.createDirectories(dbPath.getParent());
            }
        } catch (Exception e) {
            logger.warn("DB 디렉토리 생성 실패: {}", dbPath.getParent(), e);
        }

        String url = "jdbc:sqlite:" + dbPath.toAbsolutePath();
        this.conn = DriverManager.getConnection(url);
    }

    private Path getDbPath() {
        Path p = Paths.get("").toAbsolutePath();
        while (p != null && !Files.exists(p.resolve("gradlew")) && !Files.exists(p.resolve(".git"))) {
            p = p.getParent();
        }
        if (p == null) p = Paths.get("").toAbsolutePath();
        return p.resolve("app").resolve("db").resolve(DB_FILENAME);
    }

    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS templates (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, content TEXT);";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            logger.error("templates 테이블 생성 실패", e);
        }
    }

    public List<TemplateModel> getAllTemplates() {
        List<TemplateModel> list = new ArrayList<>();
        String sql = "SELECT id, name, content FROM templates ORDER BY name;";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new TemplateModel(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("content")
                ));
            }
        } catch (SQLException e) {
            logger.error("템플릿 목록 로드 실패", e);
        }
        return list;
    }

    public void createTemplate(String name, String content) {
        String sql = "INSERT INTO templates (name, content) VALUES (?, ?);";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, content);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("템플릿 생성 실패: name={}", name, e);
        }
    }

    public void updateTemplate(int id, String name, String content) {
        String sql = "UPDATE templates SET name = ?, content = ? WHERE id = ?;";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, content);
            ps.setInt(3, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("템플릿 수정 실패: id={}", id, e);
        }
    }

    public void deleteTemplate(int id) {
        String sql = "DELETE FROM templates WHERE id = ?;";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("템플릿 삭제 실패: id={}", id, e);
        }
    }

    public void close() {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                logger.warn("template DB 커넥션 종료 실패", e);
            }
        }
    }
}
