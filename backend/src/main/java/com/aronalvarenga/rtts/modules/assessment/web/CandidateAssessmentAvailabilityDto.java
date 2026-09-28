package com.aronalvarenga.rtts.modules.assessment.web;

import java.time.Instant;
import java.util.UUID;

public record CandidateAssessmentAvailabilityDto(
    UUID id,
    UUID trainingId,
    String title,
    String instructions,
    int version,
    int durationMinutes,
    int attemptLimit,
    long attemptsUsed,
    UUID activeAttemptId,
    Instant availableFrom,
    Instant availableUntil
) {}
