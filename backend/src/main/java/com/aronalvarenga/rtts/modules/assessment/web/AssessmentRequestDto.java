package com.aronalvarenga.rtts.modules.assessment.web;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AssessmentRequestDto(
    @NotNull UUID enrollmentId,
    @NotNull @DecimalMin("0.0") BigDecimal score,
    String remarks,
    @NotNull Instant assessmentDate
) {
}