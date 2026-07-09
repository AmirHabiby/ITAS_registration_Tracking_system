package com.aronalvarenga.rtts.modules.representative.web;

import com.aronalvarenga.rtts.modules.enrollments.domain.EnrollmentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RepresentativeResultResponseDto(
    UUID enrollmentId,
    UUID trainingId,
    BigDecimal assessmentScore,
    Boolean passed,
    String assessmentNote,
    Instant assessedAt,
    EnrollmentStatus status
) {
}