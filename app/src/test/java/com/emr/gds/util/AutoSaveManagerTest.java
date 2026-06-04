package com.emr.gds.util;

import com.emr.gds.soap.model.PMHEntry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AutoSaveManagerTest {

    @TempDir
    Path tempDir;

    private AutoSaveManager manager;

    @BeforeEach
    void setUp() throws Exception {
        manager = new AutoSaveManager("test-pmh.json");
        // 저장 경로를 임시 디렉토리로 교체 (프로덕션 홈 디렉토리에 기록하지 않음)
        Field field = AutoSaveManager.class.getDeclaredField("savePath");
        field.setAccessible(true);
        field.set(manager, tempDir.resolve("test-pmh.json"));
    }

    private static PMHEntry entry(String category, boolean selected) {
        return new PMHEntry(category, selected, "", false, PMHEntry.CategoryType.DISEASE);
    }

    // ── autoSave ─────────────────────────────────────────────────────────

    @Test
    void autoSave_createsFile() throws Exception {
        manager.autoSave(List.of(entry("HTN", true)));

        Path saved = tempDir.resolve("test-pmh.json");
        assertThat(Files.exists(saved)).isTrue();
    }

    @Test
    void autoSave_writesValidJson() throws Exception {
        manager.autoSave(List.of(entry("HTN", true), entry("DM", false)));

        String content = Files.readString(tempDir.resolve("test-pmh.json"));
        assertThat(content).contains("HTN").contains("DM");
    }

    @Test
    void autoSave_emptyList_writesEmptyJsonArray() throws Exception {
        manager.autoSave(List.of());

        String content = Files.readString(tempDir.resolve("test-pmh.json")).replaceAll("\\s", "");
        assertThat(content).contains("[]");
    }

    // ── restore ───────────────────────────────────────────────────────────

    @Test
    void restore_noFileExists_returnsEmpty() {
        Optional<List<PMHEntry>> result = manager.restore();
        assertThat(result).isEmpty();
    }

    @Test
    void restore_afterSave_returnsEntries() throws Exception {
        List<PMHEntry> original = List.of(entry("HTN", true), entry("DM", false));
        manager.autoSave(original);

        Optional<List<PMHEntry>> restored = manager.restore();

        assertThat(restored).isPresent();
        assertThat(restored.get()).hasSize(2);
    }

    @Test
    void restore_afterSave_preservesCategory() throws Exception {
        manager.autoSave(List.of(entry("Hypercholesterolemia", true)));

        Optional<List<PMHEntry>> restored = manager.restore();

        assertThat(restored).isPresent();
        assertThat(restored.get().get(0).getCategory()).isEqualTo("Hypercholesterolemia");
    }

    @Test
    void restore_afterSave_preservesSelectedState() throws Exception {
        manager.autoSave(List.of(entry("HTN", true), entry("DM", false)));

        List<PMHEntry> result = manager.restore().orElseThrow();

        assertThat(result.get(0).isSelected()).isTrue();
        assertThat(result.get(1).isSelected()).isFalse();
    }

    @Test
    void restore_corruptedFile_returnsEmpty() throws Exception {
        Files.writeString(tempDir.resolve("test-pmh.json"), "{ invalid json !!!}");

        Optional<List<PMHEntry>> result = manager.restore();
        assertThat(result).isEmpty();
    }

    // ── 왕복 직렬화 ───────────────────────────────────────────────────────

    @Test
    void roundTrip_multipleEntries_allPreserved() throws Exception {
        List<PMHEntry> original = List.of(
                entry("HTN", true),
                entry("DM type 2", false),
                entry("Hyperlipidemia", true)
        );

        manager.autoSave(original);
        List<PMHEntry> restored = manager.restore().orElseThrow();

        assertThat(restored).hasSize(3);
        assertThat(restored.get(2).getCategory()).isEqualTo("Hyperlipidemia");
    }
}
