package com.aronalvarenga.rtts.modules.assessment.web;

import java.time.Instant;
import java.util.UUID;

public record CandidateAttemptHistoryDto(
    UUID attemptId,
    UUID assessmentId,
    UUID trainingId,
    String trainingTitle,
    String assessmentTitle,
    int assessmentVersion,
    int attemptNumber,
    String status,
    Instant startedAt,
    Instant submittedAt,
    Instant expiredAt,
    AssessmentResultDto releasedResult
) {}
