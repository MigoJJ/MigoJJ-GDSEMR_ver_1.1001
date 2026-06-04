package com.emr.gds.features.allergy.service;

import com.emr.gds.features.allergy.model.AllergyCause;
import com.emr.gds.features.allergy.model.SymptomItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AllergyDataServiceTest {

    private AllergyDataService service;

    @BeforeEach
    void setUp() {
        service = new AllergyDataService();
    }

    // ── getSymptomItems ───────────────────────────────────────────────────

    @Test
    void getSymptomItems_returnsNonEmptyList() {
        assertThat(service.getSymptomItems()).isNotEmpty();
    }

    @Test
    void getSymptomItems_containsAllExpectedCategories() {
        List<String> categories = service.getSymptomItems().stream()
                .map(SymptomItem::getCategory)
                .distinct()
                .toList();

        assertThat(categories).contains(
                "Skin reactions", "Swelling", "Respiratory symptoms",
                "Gastrointestinal", "Other", "Anaphylaxis"
        );
    }

    @Test
    void getSymptomItems_anaphylaxisItemsAllFlaggedTrue() {
        List<SymptomItem> anaphylaxisItems = service.getSymptomItems().stream()
                .filter(i -> "Anaphylaxis".equals(i.getCategory()))
                .toList();

        assertThat(anaphylaxisItems).isNotEmpty();
        assertThat(anaphylaxisItems).allMatch(SymptomItem::isAnaphylaxis)
                .withFailMessage("Anaphylaxis 카테고리 항목은 모두 isAnaphylaxis=true 여야 함");
    }

    @Test
    void getSymptomItems_nonAnaphylaxisItemsNotFlagged() {
        List<SymptomItem> normalItems = service.getSymptomItems().stream()
                .filter(i -> !"Anaphylaxis".equals(i.getCategory()))
                .toList();

        assertThat(normalItems).isNotEmpty();
        assertThat(normalItems).noneMatch(SymptomItem::isAnaphylaxis)
                .withFailMessage("일반 카테고리 항목은 isAnaphylaxis=false 여야 함");
    }

    @Test
    void getSymptomItems_containsDropInBloodPressure() {
        boolean found = service.getSymptomItems().stream()
                .anyMatch(i -> "Anaphylaxis".equals(i.getCategory())
                        && i.getSymptom().toLowerCase().contains("blood pressure"));
        assertThat(found).as("아나필락시스에 혈압 저하 항목이 포함되어야 함").isTrue();
    }

    @Test
    void getSymptomItems_defaultSelectedIsFalse() {
        assertThat(service.getSymptomItems())
                .allSatisfy(i -> assertThat(i.isSelected()).isFalse());
    }

    // ── getAllergyCauses ───────────────────────────────────────────────────

    @Test
    void getAllergyCauses_returnsNonEmptyList() {
        assertThat(service.getAllergyCauses()).isNotEmpty();
    }

    @Test
    void getAllergyCauses_containsCommonDrugAllergens() {
        List<String> names = service.getAllergyCauses().stream()
                .map(AllergyCause::getName)
                .toList();

        assertThat(names).anySatisfy(n -> assertThat(n).containsIgnoringCase("Penicillin"));
        assertThat(names).anySatisfy(n -> assertThat(n).containsIgnoringCase("NSAIDs"));
        assertThat(names).anySatisfy(n -> assertThat(n).containsIgnoringCase("Sulfa"));
    }

    @Test
    void getAllergyCauses_containsCommonFoodAllergens() {
        List<String> names = service.getAllergyCauses().stream()
                .map(AllergyCause::getName)
                .toList();

        assertThat(names).anySatisfy(n -> assertThat(n).containsIgnoringCase("Peanut"));
        assertThat(names).anySatisfy(n -> assertThat(n).containsIgnoringCase("Shellfish"));
        assertThat(names).anySatisfy(n -> assertThat(n).containsIgnoringCase("Egg"));
    }

    @Test
    void getAllergyCauses_noCauseHasBlankName() {
        assertThat(service.getAllergyCauses())
                .allSatisfy(c -> assertThat(c.getName()).isNotBlank());
    }
}
