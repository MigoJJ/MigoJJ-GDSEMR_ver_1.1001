package com.emr.gds.server.model;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record Patient(
        UUID id,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String phone,
        List<Visit> visits
) {
    public Patient {
        // Normalize visits to an immutable list to avoid accidental external mutation.
        visits = visits == null ? List.of() : List.copyOf(visits);
    }
}
