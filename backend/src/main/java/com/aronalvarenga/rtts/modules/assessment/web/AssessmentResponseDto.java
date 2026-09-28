package com.aronalvarenga.rtts.modules.assessment.web;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AssessmentResponseDto(
    UUID id,
    UUID trainingEnrollmentId,
    String representativeName,
    UUID submittedByUserId,
    BigDecimal score,
    boolean passed,
    String remarks,
    Instant assessmentDate
) {
}