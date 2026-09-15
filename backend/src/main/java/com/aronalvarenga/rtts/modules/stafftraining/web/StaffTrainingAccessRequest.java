package com.aronalvarenga.rtts.modules.stafftraining.web;

import jakarta.validation.constraints.NotBlank;

public record StaffTrainingAccessRequest(
    @NotBlank String name,
    @NotBlank String department,
    @NotBlank String trainingPassword
) {
}
