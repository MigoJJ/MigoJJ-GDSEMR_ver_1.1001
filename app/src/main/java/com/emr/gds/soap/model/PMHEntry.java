package com.emr.gds.soap.model;

public class PMHEntry {
    private String category;
    private boolean selected;
    private String notes;
    private boolean isDefaultDotTarget;
    private CategoryType type;

    public PMHEntry(String category, boolean selected, String notes, boolean isDefaultDotTarget, CategoryType type) {
        this.category = category;
        this.selected = selected;
        this.notes = notes;
        this.isDefaultDotTarget = isDefaultDotTarget;
        this.type = type;
    }

    // Getters
    public String getCategory() {
        return category;
    }

    public boolean isSelected() {
        return selected;
    }

    public String getNotes() {
        return notes;
    }

    public CategoryType getType() {
        return type;
    }

    // Setters
    public void setCategory(String category) {
        this.category = category;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setType(CategoryType type) {
        this.type = type;
    }

    // Methods expected by PMHService
    public boolean isDefaultDotTarget() {
        return isDefaultDotTarget;
    }

    public void applyDefaultDot() {
        if (isDefaultDotTarget && notes.isEmpty()) {
            this.notes = ";";
        }
    }

    public boolean shouldIncludeInSummary() {
        // Assuming all entries are included by default unless specified otherwise
        // This might need refinement based on actual UI logic later.
        return true;
    }

    public String formatForDisplay() {
        String prefix = selected ? "▣ " : "□ ";
        return prefix + category + (notes != null && !notes.isEmpty() ? ": " + notes : "");
    }


    public enum CategoryType {
        ALLERGY,
        DISEASE,
        OPERATION,
        MEDICATION,
        SOCIAL,
        FAMILY,
        ETC
    }
}