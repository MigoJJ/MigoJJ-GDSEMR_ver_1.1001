package com.emr.gds.repository.sqlite;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SqliteAbbreviationRepositoryTest {

    private Connection conn;
    private SqliteAbbreviationRepository repo;

    @BeforeEach
    void setUp() throws Exception {
        conn = DriverManager.getConnection("jdbc:sqlite::memory:");
        repo = new SqliteAbbreviationRepository(conn);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (conn != null && !conn.isClosed()) conn.close();
    }

    // ── findAll ───────────────────────────────────────────────────────────

    @Test
    void findAll_emptyDb_returnsEmptyMap() throws SQLException {
        assertThat(repo.findAll()).isEmpty();
    }

    @Test
    void findAll_afterInsert_returnsEntry() throws SQLException {
        repo.insert("htn", "Hypertension");

        Map<String, String> result = repo.findAll();

        assertThat(result).containsEntry("htn", "Hypertension");
    }

    @Test
    void findAll_multipleEntries_returnsAll() throws SQLException {
        repo.insert("htn", "Hypertension");
        repo.insert("dm", "Diabetes Mellitus");
        repo.insert("sob", "Shortness of breath");

        assertThat(repo.findAll()).hasSize(3);
    }

    // ── insert ───────────────────────────────────────────────────────────

    @Test
    void insert_newEntry_persistsSuccessfully() throws SQLException {
        repo.insert("htn", "Hypertension");

        assertThat(repo.findAll()).containsKey("htn");
    }

    @Test
    void insert_duplicatePrimaryKey_throwsSQLException() throws SQLException {
        repo.insert("htn", "Hypertension");

        assertThatThrownBy(() -> repo.insert("htn", "Another value"))
                .isInstanceOf(SQLException.class);
    }

    // ── update ────────────────────────────────────────────────────────────

    @Test
    void update_existingEntry_changesValues() throws SQLException {
        repo.insert("htn", "Old value");

        repo.update("htn", "hypertension", "Hypertension (updated)");

        Map<String, String> result = repo.findAll();
        assertThat(result).doesNotContainKey("htn");
        assertThat(result).containsEntry("hypertension", "Hypertension (updated)");
    }

    @Test
    void update_sameKey_updatesFullForm() throws SQLException {
        repo.insert("htn", "Old full");

        repo.update("htn", "htn", "Hypertension");

        assertThat(repo.findAll()).containsEntry("htn", "Hypertension");
    }

    @Test
    void update_nonExistentEntry_noExceptionButNoEffect() throws SQLException {
        repo.update("ghost", "ghost", "value"); // 없는 키 업데이트 — 예외 없이 무시
        assertThat(repo.findAll()).isEmpty();
    }

    // ── delete ────────────────────────────────────────────────────────────

    @Test
    void delete_existingEntry_returnsTrue() throws SQLException {
        repo.insert("htn", "Hypertension");

        assertThat(repo.delete("htn")).isTrue();
    }

    @Test
    void delete_existingEntry_removesFromDb() throws SQLException {
        repo.insert("htn", "Hypertension");

        repo.delete("htn");

        assertThat(repo.findAll()).doesNotContainKey("htn");
    }

    @Test
    void delete_nonExistentEntry_returnsFalse() throws SQLException {
        assertThat(repo.delete("ghost")).isFalse();
    }

    // ── 복합 시나리오 ─────────────────────────────────────────────────────

    @Test
    void fullCrudCycle() throws SQLException {
        // Create
        repo.insert("htn", "Hypertension");
        assertThat(repo.findAll()).containsEntry("htn", "Hypertension");

        // Update
        repo.update("htn", "htn", "Hypertension - Essential");
        assertThat(repo.findAll()).containsEntry("htn", "Hypertension - Essential");

        // Delete
        repo.delete("htn");
        assertThat(repo.findAll()).doesNotContainKey("htn");
    }

    @Test
    void insertMultiple_deleteOne_othersRemain() throws SQLException {
        repo.insert("htn", "Hypertension");
        repo.insert("dm", "Diabetes Mellitus");

        repo.delete("htn");

        Map<String, String> remaining = repo.findAll();
        assertThat(remaining).doesNotContainKey("htn");
        assertThat(remaining).containsKey("dm");
    }
}
