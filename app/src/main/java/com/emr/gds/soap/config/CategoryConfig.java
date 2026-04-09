package com.emr.gds.soap.config;

import com.emr.gds.soap.model.PMHEntry;

public class CategoryConfig {
    private String name;
    private boolean defaultDotTarget;
    private String tooltip;
    private PMHEntry.CategoryType type;

    // Constructors
    public CategoryConfig() {}

    public CategoryConfig(String name, boolean defaultDotTarget, String tooltip, PMHEntry.CategoryType type) {
        this.name = name;
        this.defaultDotTarget = defaultDotTarget;
        this.tooltip = tooltip;
        this.type = type;
    }

    // Getters
    public String getName() {
        return name;
    }

    public boolean isDefaultDotTarget() {
        return defaultDotTarget;
    }

    public String getTooltip() {
        return tooltip;
    }

    public PMHEntry.CategoryType getType() {
        return type;
    }

    // Setters (required for JSON deserialization)
    public void setName(String name) {
        this.name = name;
    }

    public void setDefaultDotTarget(boolean defaultDotTarget) {
        this.defaultDotTarget = defaultDotTarget;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    public void setType(PMHEntry.CategoryType type) {
        this.type = type;
    }
}