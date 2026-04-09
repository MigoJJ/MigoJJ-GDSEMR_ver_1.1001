package com.emr.gds.features.history.application;

import com.emr.gds.features.history.domain.ConditionCategory;
import com.emr.gds.features.history.domain.HistoryRepository;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FamilyHistoryService {

    private static final Map<ConditionCategory, List<String>> DEFAULTS = new EnumMap<>(ConditionCategory.class);

    static {
        DEFAULTS.put(ConditionCategory.ENDOCRINE, List.of(
            "Type 1 Diabetes", "Type 2 Diabetes", "Hypothyroidism", "Hyperthyroidism", "Thyroid Cancer"
        ));
        DEFAULTS.put(ConditionCategory.CANCER, List.of(
            "Breast Cancer", "Lung Cancer", "Prostate Cancer", "Colon Cancer", "Skin Cancer"
        ));
        DEFAULTS.put(ConditionCategory.CARDIOVASCULAR, List.of(
            "Coronary Artery Disease", "Hypertension", "Heart Attack", "Stroke", "Arrhythmia"
        ));
        DEFAULTS.put(ConditionCategory.GENETIC, List.of(
            "Cystic Fibrosis", "Huntington's Disease", "Down Syndrome", "Sickle Cell Anemia", "Hemophilia"
        ));
    }

    private final HistoryRepository repository;

    public FamilyHistoryService(HistoryRepository repository) {
        this.repository = repository;
        initializeDefaults();
    }

    public List<String> getConditions(ConditionCategory category) {
        return repository.getConditionsByCategory(category);
    }

    public void addCondition(ConditionCategory category, String name) {
        String normalized = normalizeName(name);
        if (normalized.isEmpty()) {
            return;
        }
        List<String> existing = repository.getConditionsByCategory(category);
        if (containsIgnoreCase(existing, normalized)) {
            return;
        }
        repository.addCondition(category, normalized);
    }

    public void updateCondition(ConditionCategory category, String oldName, String newName) {
        String normalizedNew = normalizeName(newName);
        if (normalizedNew.isEmpty()) {
            throw new IllegalArgumentException("New name cannot be empty.");
        }
        // Check if new name already exists (and isn't the same as old name)
        if (!oldName.equalsIgnoreCase(normalizedNew)) {
             List<String> existing = repository.getConditionsByCategory(category);
             if (containsIgnoreCase(existing, normalizedNew)) {
                 throw new IllegalArgumentException("Condition '" + normalizedNew + "' already exists.");
             }
        }
        repository.updateCondition(category, oldName, normalizedNew);
    }

    public void deleteCondition(ConditionCategory category, String name) {
        if (name == null || name.trim().isEmpty()) return;
        repository.deleteCondition(category, name);
    }

    private void initializeDefaults() {
        for (Map.Entry<ConditionCategory, List<String>> entry : DEFAULTS.entrySet()) {
            checkAndPopulate(entry.getKey(), entry.getValue());
        }
    }

    private void checkAndPopulate(ConditionCategory category, List<String> defaults) {
        List<String> current = repository.getConditionsByCategory(category);
        if (current.isEmpty()) {
            for (String item : defaults) {
                repository.addCondition(category, item);
            }
        }
    }

    private String normalizeName(String name) {
        if (name == null) {
            return "";
        }
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        return trimmed.replaceAll("\\s+", " ");
    }

    private boolean containsIgnoreCase(List<String> values, String candidate) {
        String lowered = candidate.toLowerCase(Locale.ROOT);
        for (String value : values) {
            if (value.toLowerCase(Locale.ROOT).equals(lowered)) {
                return true;
            }
        }
        return false;
    }
}
