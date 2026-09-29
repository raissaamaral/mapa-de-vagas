package io.github.raissaamaral.mapadevagas.application;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ApplicationRequest(
        @NotBlank @Size(max = 150) String company,
        @NotBlank @Size(max = 150) String jobTitle,
        @Size(max = 2048) @Pattern(regexp = "^https?://\\S+$") String jobUrl,
        JobSource source,
        WorkModel workModel,
        Seniority seniority,
        @Size(max = 150) String location,
        @PositiveOrZero BigDecimal salary,
        @Size(max = 1000) String techStack,
        @Size(max = 20000) String jobDescription,
        LocalDate applicationDeadline,
        LocalDate appliedOn,
        @Positive Integer totalStages,
        @Size(max = 10000) String notes) {
}
