package com.emr.gds.service;

import com.emr.gds.domain.AbbreviationEntry;
import com.emr.gds.repository.AbbreviationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AbbreviationServiceTest {

    @Mock
    private AbbreviationRepository repository;

    private Map<String, String> cache;
    private AbbreviationService service;

    @BeforeEach
    void setUp() {
        cache = new HashMap<>();
        service = new AbbreviationService(repository, cache);
    }

    // ── loadAll ───────────────────────────────────────────────────────────

    @Test
    void loadAll_replacesCache_withRepositoryData() throws SQLException {
        cache.put("old", "old_full");
        when(repository.findAll()).thenReturn(Map.of("htn", "Hypertension", "dm", "Diabetes Mellitus"));

        service.loadAll();

        assertThat(cache).containsKey("htn").containsKey("dm").doesNotContainKey("old");
    }

    @Test
    void loadAll_returnsLoadedMap() throws SQLException {
        when(repository.findAll()).thenReturn(Map.of("sob", "Shortness of breath"));

        Map<String, String> result = service.loadAll();

        assertThat(result).containsEntry("sob", "Shortness of breath");
    }

    // ── add ───────────────────────────────────────────────────────────────

    @Test
    void add_insertsIntoRepositoryAndCache() throws SQLException {
        var entry = new AbbreviationEntry("sob", "Shortness of breath");

        service.add(entry);

        verify(repository).insert("sob", "Shortness of breath");
        assertThat(cache).containsEntry("sob", "Shortness of breath");
    }

    @Test
    void add_duplicateEntry_callsRepositoryAgain() throws SQLException {
        cache.put("htn", "Old value");
        var entry = new AbbreviationEntry("htn", "Hypertension");

        service.add(entry);

        verify(repository).insert("htn", "Hypertension");
        assertThat(cache).containsEntry("htn", "Hypertension");
    }

    // ── update ────────────────────────────────────────────────────────────

    @Test
    void update_removesOldKeyAndAddsNewKey() throws SQLException {
        cache.put("htn", "Old full");
        var updated = new AbbreviationEntry("hypertension", "Hypertension");

        service.update("htn", updated);

        verify(repository).update("htn", "hypertension", "Hypertension");
        assertThat(cache).doesNotContainKey("htn");
        assertThat(cache).containsEntry("hypertension", "Hypertension");
    }

    @Test
    void update_sameKey_replacesValue() throws SQLException {
        cache.put("htn", "Old");
        var updated = new AbbreviationEntry("htn", "Hypertension updated");

        service.update("htn", updated);

        assertThat(cache).containsEntry("htn", "Hypertension updated");
    }

    // ── delete ────────────────────────────────────────────────────────────

    @Test
    void delete_existingKey_removesFromCacheAndReturnsTrue() throws SQLException {
        cache.put("htn", "Hypertension");
        when(repository.delete("htn")).thenReturn(true);

        boolean result = service.delete("htn");

        assertThat(result).isTrue();
        assertThat(cache).doesNotContainKey("htn");
    }

    @Test
    void delete_nonExistentKey_returnsFalse_cacheUnchanged() throws SQLException {
        when(repository.delete("unknown")).thenReturn(false);

        boolean result = service.delete("unknown");

        assertThat(result).isFalse();
        verify(repository).delete("unknown");
    }

    @Test
    void delete_repositoryReturnsFalse_cacheNotModified() throws SQLException {
        cache.put("htn", "Hypertension");
        when(repository.delete("htn")).thenReturn(false);

        service.delete("htn");

        assertThat(cache).containsKey("htn");
    }

    // ── getAbbreviations ─────────────────────────────────────────────────

    @Test
    void getAbbreviations_returnsSameCacheInstance() {
        cache.put("htn", "Hypertension");
        assertThat(service.getAbbreviations()).isSameAs(cache);
    }
}
