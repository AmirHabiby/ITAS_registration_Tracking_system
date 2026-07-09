package com.aronalvarenga.rtts.modules.enrollments.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record AssessmentRequest(
    @NotNull UUID trainingId,
    @NotNull UUID representativeId,
    @NotNull BigDecimal score,
    @NotBlank String note
) {
}
