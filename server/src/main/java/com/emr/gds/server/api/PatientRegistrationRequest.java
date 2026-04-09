package com.emr.gds.server.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PatientRegistrationRequest(
        @NotBlank(message = "Patient name is required")
        @Size(max = 80, message = "Name must be 80 characters or fewer")
        String name,
        @Past(message = "Birth date must be in the past")
        LocalDate birthDate,
        @Size(max = 255, message = "Notes must be 255 characters or fewer")
        String notes
) {
}
