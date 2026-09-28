package com.aronalvarenga.rtts.modules.enrollments.web;

import com.aronalvarenga.rtts.modules.enrollments.domain.EnrollmentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record EnrollmentResponseDto(
    UUID id,
    UUID representativeId,
    String representativeName,
    UUID trainingId,
    String trainingTitle,
    BigDecimal assessmentScore,
    Boolean passed,
    String assessmentNote,
    Instant assessedAt,
    EnrollmentStatus status
) {
}