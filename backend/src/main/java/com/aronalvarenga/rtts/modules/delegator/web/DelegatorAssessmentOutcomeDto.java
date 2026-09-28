package com.aronalvarenga.rtts.modules.delegator.web;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record DelegatorAssessmentOutcomeDto(
    UUID attemptId,
    UUID representativeId,
    String representativeName,
    UUID trainingId,
    String trainingTitle,
    UUID assessmentId,
    String assessmentTitle,
    int attemptNumber,
    BigDecimal awardedMarks,
    BigDecimal maximumMarks,
    BigDecimal percentage,
    boolean passed,
    Instant releasedAt
) {}
