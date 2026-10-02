package com.aronalvarenga.rtts.modules.assessment.web;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;

public record AssessmentDraftRequest(
    @NotBlank @Size(max = 180) String title,
    @Size(max = 2000) String instructions,
    @NotNull @DecimalMin("0.00") @DecimalMax("100.00") @Digits(integer = 3, fraction = 2)
        BigDecimal passingScore,
    @Min(1) @Max(1440) int durationMinutes,
    @Min(1) @Max(100) int attemptLimit,
    Instant availableFrom,
    Instant availableUntil,
    boolean randomizeQuestions,
    boolean randomizeOptions,
    @Min(1) Integer weekNumber
) {}
