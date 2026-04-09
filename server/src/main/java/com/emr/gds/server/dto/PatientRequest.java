package com.emr.gds.server.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record PatientRequest(
        @NotBlank(message = "First name is required")
        @Size(max = 40, message = "First name must be 40 characters or fewer")
        String firstName,
        @NotBlank(message = "Last name is required")
        @Size(max = 40, message = "Last name must be 40 characters or fewer")
        String lastName,
        @Past(message = "Date of birth must be in the past")
        LocalDate dateOfBirth,
        @Pattern(regexp = "^[0-9+\\-()\\s]*$", message = "Phone may contain digits and + - ( ) characters")
        @Size(max = 20, message = "Phone must be 20 characters or fewer")
        String phone,
        @Valid
        @Size(max = 50, message = "Visits cannot exceed 50 entries")
        List<VisitRequest> visits
) {
}
