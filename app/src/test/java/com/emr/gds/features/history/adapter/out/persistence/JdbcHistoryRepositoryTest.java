package com.emr.gds.features.history.adapter.out.persistence;

import com.emr.gds.features.history.domain.ConditionCategory;
import com.emr.gds.features.history.domain.HistoryPersistenceException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JdbcHistoryRepositoryTest {

    private Connection conn;
    private JdbcHistoryRepository repo;

    @BeforeEach
    void setUp() throws Exception {
        conn = DriverManager.getConnection("jdbc:sqlite::memory:");
        repo = new JdbcHistoryRepository(conn);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }

    // ── getConditionsByCategory ───────────────────────────────────────────

    @Test
    void getConditionsByCategory_emptyDb_returnsEmptyList() {
        List<String> result = repo.getConditionsByCategory(ConditionCategory.ENDOCRINE);
        assertThat(result).isEmpty();
    }

    @Test
    void getConditionsByCategory_returnsOnlyMatchingCategory() {
        repo.addCondition(ConditionCategory.ENDOCRINE, "Type 2 DM");
        repo.addCondition(ConditionCategory.CARDIOVASCULAR, "Essential HTN");

        List<String> diabetesConditions = repo.getConditionsByCategory(ConditionCategory.ENDOCRINE);

        assertThat(diabetesConditions).containsExactly("Type 2 DM");
        assertThat(diabetesConditions).doesNotContain("Essential HTN");
    }

    @Test
    void getConditionsByCategory_resultsReturnedAlphabetically() {
        repo.addCondition(ConditionCategory.ENDOCRINE, "Type 2 DM");
        repo.addCondition(ConditionCategory.ENDOCRINE, "Type 1 DM");
        repo.addCondition(ConditionCategory.ENDOCRINE, "Gestational DM");

        List<String> result = repo.getConditionsByCategory(ConditionCategory.ENDOCRINE);

        assertThat(result).isSortedAccordingTo(String::compareTo);
    }

    // ── addCondition ─────────────────────────────────────────────────────

    @Test
    void addCondition_persistsAndRetrievable() {
        repo.addCondition(ConditionCategory.CARDIOVASCULAR, "Essential HTN");

        List<String> result = repo.getConditionsByCategory(ConditionCategory.CARDIOVASCULAR);
        assertThat(result).containsExactly("Essential HTN");
    }

    @Test
    void addCondition_duplicateIgnored_noDuplicateInList() {
        repo.addCondition(ConditionCategory.ENDOCRINE, "Type 2 DM");
        repo.addCondition(ConditionCategory.ENDOCRINE, "Type 2 DM"); // 중복

        List<String> result = repo.getConditionsByCategory(ConditionCategory.ENDOCRINE);
        assertThat(result).hasSize(1);
    }

    @Test
    void addCondition_multipleCategories_storedIndependently() {
        repo.addCondition(ConditionCategory.ENDOCRINE, "DM");
        repo.addCondition(ConditionCategory.CARDIOVASCULAR, "HTN");
        repo.addCondition(ConditionCategory.CANCER, "Hypothyroidism");

        assertThat(repo.getConditionsByCategory(ConditionCategory.ENDOCRINE)).containsExactly("DM");
        assertThat(repo.getConditionsByCategory(ConditionCategory.CARDIOVASCULAR)).containsExactly("HTN");
        assertThat(repo.getConditionsByCategory(ConditionCategory.CANCER)).containsExactly("Hypothyroidism");
    }

    // ── updateCondition ───────────────────────────────────────────────────

    @Test
    void updateCondition_changesName() {
        repo.addCondition(ConditionCategory.ENDOCRINE, "Old Name");

        repo.updateCondition(ConditionCategory.ENDOCRINE, "Old Name", "New Name");

        List<String> result = repo.getConditionsByCategory(ConditionCategory.ENDOCRINE);
        assertThat(result).containsExactly("New Name");
        assertThat(result).doesNotContain("Old Name");
    }

    @Test
    void updateCondition_nonExistentCondition_throwsException() {
        assertThatThrownBy(() ->
                repo.updateCondition(ConditionCategory.ENDOCRINE, "Ghost Entry", "New Name")
        ).isInstanceOf(HistoryPersistenceException.class);
    }

    // ── deleteCondition ───────────────────────────────────────────────────

    @Test
    void deleteCondition_removesEntry() {
        repo.addCondition(ConditionCategory.CARDIOVASCULAR, "Stage 1 HTN");

        repo.deleteCondition(ConditionCategory.CARDIOVASCULAR, "Stage 1 HTN");

        assertThat(repo.getConditionsByCategory(ConditionCategory.CARDIOVASCULAR)).isEmpty();
    }

    @Test
    void deleteCondition_onlyDeletesSpecifiedEntry() {
        repo.addCondition(ConditionCategory.CARDIOVASCULAR, "Stage 1 HTN");
        repo.addCondition(ConditionCategory.CARDIOVASCULAR, "Stage 2 HTN");

        repo.deleteCondition(ConditionCategory.CARDIOVASCULAR, "Stage 1 HTN");

        assertThat(repo.getConditionsByCategory(ConditionCategory.CARDIOVASCULAR))
                .containsExactly("Stage 2 HTN");
    }

    @Test
    void deleteCondition_nonExistentEntry_noExceptionThrown() {
        // 없는 항목 삭제는 예외 없이 무시되어야 함
        repo.deleteCondition(ConditionCategory.ENDOCRINE, "Ghost Entry");
    }

    // ── 복합 시나리오 ─────────────────────────────────────────────────────

    @Test
    void fullCrudCycle_addUpdateDeleteVerify() {
        repo.addCondition(ConditionCategory.CANCER, "Hypothyroidism");
        assertThat(repo.getConditionsByCategory(ConditionCategory.CANCER)).contains("Hypothyroidism");

        repo.updateCondition(ConditionCategory.CANCER, "Hypothyroidism", "Primary Hypothyroidism");
        assertThat(repo.getConditionsByCategory(ConditionCategory.CANCER)).contains("Primary Hypothyroidism");

        repo.deleteCondition(ConditionCategory.CANCER, "Primary Hypothyroidism");
        assertThat(repo.getConditionsByCategory(ConditionCategory.CANCER)).isEmpty();
    }
}
