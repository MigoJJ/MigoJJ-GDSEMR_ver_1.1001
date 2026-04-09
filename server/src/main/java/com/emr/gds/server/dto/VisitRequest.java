package com.emr.gds.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record VisitRequest(
        @NotNull(message = "Visit time is required")
        @PastOrPresent(message = "Visit time cannot be in the future")
        LocalDateTime occurredAt,
        @NotBlank(message = "Visit reason is required")
        @Size(max = 120, message = "Reason must be 120 characters or fewer")
        String reason,
        @Size(max = 500, message = "Notes must be 500 characters or fewer")
        String notes
) {
}
