package com.aronalvarenga.rtts.modules.trainings.web;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.aronalvarenga.rtts.modules.trainings.domain.TrainingAccessType;
import java.time.LocalDate;
import java.util.UUID;

public record TrainingRequestDto(
    @NotNull UUID instituteId,
    @NotBlank String title,
    @NotBlank String description,
    @NotNull LocalDate startDate,
    @NotNull LocalDate endDate,
    @Min(1) int capacity,
    @NotNull TrainingAccessType accessType,
    String staffAccessPassword
) {
}
