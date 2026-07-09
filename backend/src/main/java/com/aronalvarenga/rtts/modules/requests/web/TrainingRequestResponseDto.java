package com.aronalvarenga.rtts.modules.requests.web;

import com.aronalvarenga.rtts.modules.requests.domain.TrainingRequestStatus;
import java.time.Instant;
import java.util.UUID;

public record TrainingRequestResponseDto(
    UUID id,
    UUID representativeId,
    UUID trainingId,
    TrainingRequestStatus status,
    String reviewerUsername,
    String reviewerNote,
    Instant requestedAt,
    Instant reviewedAt
) {
}