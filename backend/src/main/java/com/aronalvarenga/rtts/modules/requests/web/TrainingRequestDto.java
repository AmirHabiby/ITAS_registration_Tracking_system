package com.aronalvarenga.rtts.modules.requests.web;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record TrainingRequestDto(
    @NotNull UUID representativeId,
    @NotNull UUID trainingId
) {
}
