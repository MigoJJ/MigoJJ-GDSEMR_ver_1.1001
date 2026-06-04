package com.emr.gds.service;

import com.emr.gds.domain.PlanHistoryEntry;
import com.emr.gds.repository.PlanHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlanHistoryServiceTest {

    @Mock
    private PlanHistoryRepository repository;

    private PlanHistoryService service;

    @BeforeEach
    void setUp() {
        service = new PlanHistoryService(repository);
    }

    // ── initialize ────────────────────────────────────────────────────────

    @Test
    void initialize_delegatesToRepositoryInit() throws SQLException {
        service.initialize();
        verify(repository).init();
    }

    // ── save ─────────────────────────────────────────────────────────────

    @Test
    void save_delegatesToRepository() throws SQLException {
        service.save("P>", "Continue current medications", "P001", "2026-06-04");
        verify(repository).save(any(PlanHistoryEntry.class));
    }

    @Test
    void save_buildsEntryWithCorrectSection() throws SQLException {
        ArgumentCaptor<PlanHistoryEntry> captor = ArgumentCaptor.forClass(PlanHistoryEntry.class);

        service.save("P>", "Continue current medications", "P001", "2026-06-04");

        verify(repository).save(captor.capture());
        assertThat(captor.getValue().section()).isEqualTo("P>");
    }

    @Test
    void save_buildsEntryWithCorrectContent() throws SQLException {
        ArgumentCaptor<PlanHistoryEntry> captor = ArgumentCaptor.forClass(PlanHistoryEntry.class);

        service.save("P>", "Increase metformin to 1000mg", "P001", "2026-06-04");

        verify(repository).save(captor.capture());
        assertThat(captor.getValue().content()).isEqualTo("Increase metformin to 1000mg");
    }

    @Test
    void save_setsCreatedAtTimestamp() throws SQLException {
        ArgumentCaptor<PlanHistoryEntry> captor = ArgumentCaptor.forClass(PlanHistoryEntry.class);
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);

        service.save("P>", "Continue current medications", "P001", "2026-06-04");

        verify(repository).save(captor.capture());
        assertThat(captor.getValue().createdAt()).isAfter(before);
    }

    @Test
    void save_repositoryThrows_propagatesException() throws SQLException {
        doThrow(new SQLException("save failed")).when(repository).save(any());

        assertThatThrownBy(() -> service.save("P>", "some plan", null, null))
                .isInstanceOf(SQLException.class);
    }

    // ── 생성자 검증 ───────────────────────────────────────────────────────

    @Test
    void constructor_nullRepository_throwsNullPointerException() {
        assertThatThrownBy(() -> new PlanHistoryService(null))
                .isInstanceOf(NullPointerException.class);
    }
}
