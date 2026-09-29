package io.github.raissaamaral.mapadevagas.application;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ApplicationResponse(
        Long id,
        String company,
        String jobTitle,
        String jobUrl,
        JobSource source,
        WorkModel workModel,
        Seniority seniority,
        String location,
        BigDecimal salary,
        String techStack,
        String jobDescription,
        ApplicationStatus status,
        LocalDate applicationDeadline,
        LocalDate appliedOn,
        Integer totalStages,
        String notes) {
}
