package com.aronalvarenga.rtts.modules.publictraining.web;

import java.time.Instant;
import java.util.UUID;

public record PublicEnrollmentResponseDto(
    UUID id,
    UUID trainingId,
    Instant enrolledAt
) {
}
