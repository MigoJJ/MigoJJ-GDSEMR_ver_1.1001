package com.emr.gds.domain;

/**
 * Core domain model for a single abbreviation entry.
 */
public record AbbreviationEntry(String shortForm, String fullForm) {
    public AbbreviationEntry {
        if (shortForm == null || shortForm.isBlank()) {
            throw new IllegalArgumentException("Short form is required.");
        }
        if (fullForm == null || fullForm.isBlank()) {
            throw new IllegalArgumentException("Full form is required.");
        }
    }
}
