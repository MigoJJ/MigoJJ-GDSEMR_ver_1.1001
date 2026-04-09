package com.emr.gds.server.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record VisitResponse(
        UUID id,
        LocalDateTime occurredAt,
        String reason,
        String notes
) {
}
