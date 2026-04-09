package com.emr.gds.domain;

import java.time.LocalDateTime;

/**
 * Core domain model for plan history storage.
 */
public record PlanHistoryEntry(
        String section,
        String content,
        String patientId,
        String encounterDate,
        LocalDateTime createdAt
) {
    public PlanHistoryEntry {
        if (section == null || section.isBlank()) {
            throw new IllegalArgumentException("Section is required.");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Content is required.");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("Created timestamp is required.");
        }
    }
}
