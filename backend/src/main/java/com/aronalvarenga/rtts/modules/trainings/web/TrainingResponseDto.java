package com.aronalvarenga.rtts.modules.trainings.web;

import com.aronalvarenga.rtts.modules.trainings.domain.TrainingStatus;
import com.aronalvarenga.rtts.modules.trainings.domain.TrainingAccessType;
import java.time.LocalDate;
import java.util.UUID;

public record TrainingResponseDto(
    UUID id,
    UUID trainingInstituteProfileId,
    String title,
    String description,
    LocalDate startDate,
    LocalDate endDate,
    int capacity,
    java.math.BigDecimal passingScore,
    int allowedRetakeAttempts,
    TrainingAccessType accessType,
    TrainingStatus status,
    boolean active
) {
}