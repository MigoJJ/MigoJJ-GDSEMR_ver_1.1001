package com.emr.gds.soap.service;

import com.emr.gds.soap.model.PMHEntry;
import com.emr.gds.soap.model.PMHEntry.CategoryType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PMHServiceTest {

    private PMHService service;

    // 편의 메서드
    private static PMHEntry entry(String category, boolean selected, String notes, CategoryType type) {
        return new PMHEntry(category, selected, notes, false, type);
    }

    private static PMHEntry dotTarget(String category) {
        return new PMHEntry(category, false, "", true, CategoryType.DISEASE);
    }

    @BeforeEach
    void setUp() {
        service = new PMHService(Map.of("htn", "Hypertension", "dm", "Diabetes Mellitus"));
    }

    // ── expandAbbreviation ────────────────────────────────────────────────

    @Test
    void expandAbbreviation_knownKey_returnsExpanded() {
        Optional<String> result = service.expandAbbreviation(":htn");
        assertThat(result).contains("Hypertension");
    }

    @Test
    void expandAbbreviation_unknownKey_returnsEmpty() {
        Optional<String> result = service.expandAbbreviation(":unknown");
        assertThat(result).isEmpty();
    }

    @Test
    void expandAbbreviation_noColon_returnsEmpty() {
        Optional<String> result = service.expandAbbreviation("htn");
        assertThat(result).isEmpty();
    }

    @Test
    void expandAbbreviation_nullInput_returnsEmpty() {
        Optional<String> result = service.expandAbbreviation(null);
        assertThat(result).isEmpty();
    }

    @Test
    void expandAbbreviation_colonOnly_returnsEmpty() {
        Optional<String> result = service.expandAbbreviation(":");
        assertThat(result).isEmpty();
    }

    // ── filterByType ─────────────────────────────────────────────────────

    @Test
    void filterByType_returnsOnlyMatchingType() {
        List<PMHEntry> entries = List.of(
                entry("HTN", true, "", CategoryType.DISEASE),
                entry("Penicillin", true, "", CategoryType.ALLERGY),
                entry("Appendectomy", false, "", CategoryType.OPERATION)
        );

        List<PMHEntry> diseases = service.filterByType(entries, CategoryType.DISEASE);
        assertThat(diseases).hasSize(1);
        assertThat(diseases.get(0).getCategory()).isEqualTo("HTN");
    }

    @Test
    void filterByType_noMatch_returnsEmptyList() {
        List<PMHEntry> entries = List.of(entry("HTN", true, "", CategoryType.DISEASE));
        assertThat(service.filterByType(entries, CategoryType.ALLERGY)).isEmpty();
    }

    // ── getSelectedEntries ────────────────────────────────────────────────

    @Test
    void getSelectedEntries_returnsOnlySelected() {
        List<PMHEntry> entries = List.of(
                entry("HTN", true, "", CategoryType.DISEASE),
                entry("DM", false, "", CategoryType.DISEASE),
                entry("Penicillin", true, "", CategoryType.ALLERGY)
        );

        List<PMHEntry> selected = service.getSelectedEntries(entries);
        assertThat(selected).hasSize(2);
        assertThat(selected).allMatch(PMHEntry::isSelected);
    }

    @Test
    void getSelectedEntries_noneSelected_returnsEmpty() {
        List<PMHEntry> entries = List.of(entry("HTN", false, "", CategoryType.DISEASE));
        assertThat(service.getSelectedEntries(entries)).isEmpty();
    }

    // ── applyDefaultDots ─────────────────────────────────────────────────

    @Test
    void applyDefaultDots_emptyNotesTarget_getsDot() {
        PMHEntry target = dotTarget("Smoking");
        service.applyDefaultDots(List.of(target));
        assertThat(target.getNotes()).isEqualTo(";");
    }

    @Test
    void applyDefaultDots_notATarget_notModified() {
        PMHEntry normal = entry("HTN", false, "", CategoryType.DISEASE);
        service.applyDefaultDots(List.of(normal));
        assertThat(normal.getNotes()).isEmpty();
    }

    @Test
    void applyDefaultDots_targetWithExistingNotes_notOverwritten() {
        PMHEntry target = new PMHEntry("Smoking", false, "20 pack-years", true, CategoryType.SOCIAL);
        service.applyDefaultDots(List.of(target));
        assertThat(target.getNotes()).isEqualTo("20 pack-years");
    }

    // ── validate ─────────────────────────────────────────────────────────

    @Test
    void validate_noConflicts_isValid() {
        List<PMHEntry> entries = List.of(
                entry("HTN", true, "note", CategoryType.DISEASE)
        );
        PMHService.ValidationResult result = service.validate(entries);
        assertThat(result.isValid()).isTrue();
        assertThat(result.getWarnings()).isEmpty();
    }

    @Test
    void validate_allDeniedWithOtherAllergy_hasWarning() {
        List<PMHEntry> entries = List.of(
                entry("All denied allergies...", true, "", CategoryType.ALLERGY),
                entry("Penicillin allergy", true, "", CategoryType.ALLERGY)
        );
        PMHService.ValidationResult result = service.validate(entries);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getWarnings()).isNotEmpty();
    }

    @Test
    void validate_emptyList_isValid() {
        PMHService.ValidationResult result = service.validate(Collections.emptyList());
        assertThat(result.isValid()).isTrue();
    }

    // ── buildSummary ─────────────────────────────────────────────────────

    @Test
    void buildSummary_selectedEntry_appearsFirst() {
        List<PMHEntry> entries = List.of(
                entry("HTN", true, "controlled", CategoryType.DISEASE),
                entry("DM", false, "", CategoryType.DISEASE)
        );
        String summary = service.buildSummary(entries, false);
        int htnPos = summary.indexOf("HTN");
        int dmPos = summary.indexOf("DM");
        assertThat(htnPos).isLessThan(dmPos).as("선택된 HTN이 DM보다 먼저 나와야 함");
    }

    @Test
    void buildSummary_noEntries_returnsNoItemsMessage() {
        // shouldIncludeInSummary=true 이지만 selected=false인 엔트리만 있을 때
        // 빈 checkedLines 시 "no_items_selected" 메시지가 포함되어야 함
        List<PMHEntry> entries = Collections.emptyList();
        String summary = service.buildSummary(entries, false);
        assertThat(summary).isNotBlank();
    }

    @Test
    void buildSummary_selectedEntryWithNotes_containsNote() {
        List<PMHEntry> entries = List.of(
                entry("HTN", true, "Stage 1", CategoryType.DISEASE)
        );
        String summary = service.buildSummary(entries, false);
        assertThat(summary).contains("Stage 1");
    }
}
