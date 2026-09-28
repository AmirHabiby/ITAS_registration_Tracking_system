package com.aronalvarenga.rtts.modules.assessment.web;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record InstituteGradingQueueItemDto(
    UUID attemptId,
    UUID assessmentId,
    String assessmentTitle,
    String representativeName,
    int attemptNumber,
    String attemptStatus,
    Instant submittedAt,
    Instant expiredAt,
    int writtenQuestionCount,
    int gradedWrittenQuestionCount
) {}
