package com.emr.gds.features.history.application;

import com.emr.gds.features.history.domain.ConditionCategory;
import com.emr.gds.features.history.domain.HistoryRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FamilyHistoryServiceTest {

    @Test
    void initializeDefaultsSeedsEmptyRepository() {
        InMemoryHistoryRepository repository = new InMemoryHistoryRepository();
        new FamilyHistoryService(repository);

        assertContainsAll(repository.getConditionsByCategory(ConditionCategory.ENDOCRINE), List.of(
                "Type 1 Diabetes", "Type 2 Diabetes", "Hypothyroidism", "Hyperthyroidism", "Thyroid Cancer"
        ));
        assertContainsAll(repository.getConditionsByCategory(ConditionCategory.CANCER), List.of(
                "Breast Cancer", "Lung Cancer", "Prostate Cancer", "Colon Cancer", "Skin Cancer"
        ));
        assertContainsAll(repository.getConditionsByCategory(ConditionCategory.CARDIOVASCULAR), List.of(
                "Coronary Artery Disease", "Hypertension", "Heart Attack", "Stroke", "Arrhythmia"
        ));
        assertContainsAll(repository.getConditionsByCategory(ConditionCategory.GENETIC), List.of(
                "Cystic Fibrosis", "Huntington's Disease", "Down Syndrome", "Sickle Cell Anemia", "Hemophilia"
        ));
    }

    @Test
    void initializeDefaultsDoesNotOverrideExistingData() {
        InMemoryHistoryRepository repository = new InMemoryHistoryRepository();
        repository.addCondition(ConditionCategory.CANCER, "Existing Cancer");

        new FamilyHistoryService(repository);

        List<String> cancer = repository.getConditionsByCategory(ConditionCategory.CANCER);
        assertEquals(1, cancer.size());
        assertTrue(cancer.contains("Existing Cancer"));
    }

    private void assertContainsAll(List<String> actual, List<String> expected) {
        assertEquals(expected.size(), actual.size());
        assertTrue(actual.containsAll(expected));
    }

    private static class InMemoryHistoryRepository implements HistoryRepository {
        private final Map<ConditionCategory, List<String>> data = new EnumMap<>(ConditionCategory.class);

        @Override
        public List<String> getConditionsByCategory(ConditionCategory category) {
            return new ArrayList<>(data.getOrDefault(category, List.of()));
        }

        @Override
        public void addCondition(ConditionCategory category, String conditionName) {
            data.computeIfAbsent(category, key -> new ArrayList<>()).add(conditionName);
        }

        @Override
        public void updateCondition(ConditionCategory category, String oldName, String newName) {
            List<String> values = data.getOrDefault(category, new ArrayList<>());
            int index = values.indexOf(oldName);
            if (index >= 0) {
                values.set(index, newName);
            }
            data.putIfAbsent(category, values);
        }

        @Override
        public void deleteCondition(ConditionCategory category, String conditionName) {
            List<String> values = data.get(category);
            if (values != null) {
                values.remove(conditionName);
            }
        }
    }
}
