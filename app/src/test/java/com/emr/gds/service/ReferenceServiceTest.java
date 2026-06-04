package com.emr.gds.service;

import com.emr.gds.features.ReferenceFile.ReferenceItem;
import com.emr.gds.repository.ReferenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReferenceServiceTest {

    @Mock
    private ReferenceRepository repository;

    private ReferenceService service;

    @BeforeEach
    void setUp() {
        service = new ReferenceService(repository);
    }

    private static ReferenceItem item(int id, String category, String contents) {
        return new ReferenceItem(id, category, contents, "");
    }

    // ── findAllReferences ─────────────────────────────────────────────────

    @Test
    void findAllReferences_returnsAllFromRepository() {
        when(repository.findAll()).thenReturn(List.of(
                item(1, "Drug Info", "Metformin"),
                item(2, "Guidelines", "HTN 2023")
        ));

        var result = service.findAllReferences();

        assertThat(result).hasSize(2);
    }

    @Test
    void findAllReferences_emptyRepository_returnsEmptyObservableList() {
        when(repository.findAll()).thenReturn(List.of());
        assertThat(service.findAllReferences()).isEmpty();
    }

    @Test
    void findAllReferences_resultIsObservableList() {
        when(repository.findAll()).thenReturn(List.of(item(1, "Cat", "Content")));
        // ObservableList는 javafx.collections.ObservableList를 구현해야 함
        assertThat(service.findAllReferences())
                .isInstanceOf(javafx.collections.ObservableList.class);
    }

    // ── saveReference ─────────────────────────────────────────────────────

    @Test
    void saveReference_delegatesToRepository() {
        ReferenceItem input = item(0, "Drug Info", "Metformin");
        ReferenceItem saved = item(1, "Drug Info", "Metformin");
        when(repository.save(input)).thenReturn(saved);

        ReferenceItem result = service.saveReference(input);

        verify(repository).save(input);
        assertThat(result.getId()).isEqualTo(1);
    }

    @Test
    void saveReference_returnsRepositoryResult() {
        ReferenceItem input = item(0, "Cat", "Content");
        ReferenceItem expected = item(42, "Cat", "Content");
        when(repository.save(input)).thenReturn(expected);

        assertThat(service.saveReference(input)).isSameAs(expected);
    }

    // ── deleteReference ───────────────────────────────────────────────────

    @Test
    void deleteReference_delegatesToRepository() {
        ReferenceItem toDelete = item(5, "Drug Info", "Metformin");

        service.deleteReference(toDelete);

        verify(repository).delete(toDelete);
    }
}
