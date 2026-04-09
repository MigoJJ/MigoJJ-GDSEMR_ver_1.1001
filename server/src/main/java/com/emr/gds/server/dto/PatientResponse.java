package com.emr.gds.server.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PatientResponse(
        UUID id,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String phone,
        List<VisitResponse> visits
) {
    public PatientResponse {
        visits = visits == null ? List.of() : List.copyOf(visits);
    }
}
