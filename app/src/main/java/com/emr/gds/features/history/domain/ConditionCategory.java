package com.emr.gds.features.history.domain;

public enum ConditionCategory {
    ENDOCRINE("Endocrine"),
    CANCER("Cancer"),
    CARDIOVASCULAR("Cardiovascular"),
    GENETIC("Genetic");

    private final String label;

    ConditionCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
