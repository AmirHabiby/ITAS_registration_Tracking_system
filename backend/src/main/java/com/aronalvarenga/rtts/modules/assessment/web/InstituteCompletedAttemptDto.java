package com.aronalvarenga.rtts.modules.assessment.web;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record InstituteCompletedAttemptDto(
    UUID attemptId,
    UUID assessmentId,
    String assessmentTitle,
    String representativeName,
    int attemptNumber,
    String attemptStatus,
    Instant submittedAt,
    Instant expiredAt,
    int writtenQuestionCount,
    int gradedWrittenQuestionCount,
    boolean finalized,
    Instant releasedAt,
    BigDecimal awardedMarks,
    BigDecimal maximumMarks,
    BigDecimal percentage,
    Boolean passed
) {}
