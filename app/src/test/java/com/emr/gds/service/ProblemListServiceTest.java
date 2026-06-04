package com.emr.gds.service;

import com.emr.gds.repository.ProblemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProblemListServiceTest {

    @Mock
    private ProblemRepository repository;

    private ProblemListService service;

    @BeforeEach
    void setUp() {
        service = new ProblemListService(repository);
    }

    // ── initialize ───────────────────────────────────────────────────────

    @Test
    void initialize_delegatesToRepositoryInit() throws SQLException {
        service.initialize();
        verify(repository).init();
    }

    @Test
    void initialize_repositoryThrows_propagatesException() throws SQLException {
        doThrow(new SQLException("DB error")).when(repository).init();
        assertThatThrownBy(() -> service.initialize())
                .isInstanceOf(SQLException.class)
                .hasMessageContaining("DB error");
    }

    // ── loadAll ───────────────────────────────────────────────────────────

    @Test
    void loadAll_returnsRepositoryList() throws SQLException {
        when(repository.findAll()).thenReturn(List.of("HTN", "DM"));

        List<String> result = service.loadAll();

        assertThat(result).containsExactly("HTN", "DM");
    }

    @Test
    void loadAll_emptyRepository_returnsEmptyList() throws SQLException {
        when(repository.findAll()).thenReturn(List.of());
        assertThat(service.loadAll()).isEmpty();
    }

    // ── add ───────────────────────────────────────────────────────────────

    @Test
    void add_delegatesToRepositoryInsert() throws SQLException {
        service.add("Hypercholesterolemia");
        verify(repository).insert("Hypercholesterolemia");
    }

    // ── delete ────────────────────────────────────────────────────────────

    @Test
    void delete_existingProblem_returnsTrue() throws SQLException {
        when(repository.delete("HTN")).thenReturn(true);
        assertThat(service.delete("HTN")).isTrue();
    }

    @Test
    void delete_nonExistentProblem_returnsFalse() throws SQLException {
        when(repository.delete("Unknown")).thenReturn(false);
        assertThat(service.delete("Unknown")).isFalse();
    }

    // ── 생성자 검증 ───────────────────────────────────────────────────────

    @Test
    void constructor_nullRepository_throwsNullPointerException() {
        assertThatThrownBy(() -> new ProblemListService(null))
                .isInstanceOf(NullPointerException.class);
    }
}
