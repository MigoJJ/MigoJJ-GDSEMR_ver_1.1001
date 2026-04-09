package com.emr.gds.server.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record Visit(
        UUID id,
        LocalDateTime occurredAt,
        String reason,
        String notes
) {
}
