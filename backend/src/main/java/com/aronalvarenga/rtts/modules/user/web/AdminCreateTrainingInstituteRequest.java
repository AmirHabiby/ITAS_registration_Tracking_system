package com.aronalvarenga.rtts.modules.user.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminCreateTrainingInstituteRequest(
    @NotBlank String username,
    @NotBlank String password,
    @NotBlank String displayName,
    @NotBlank String name,
    @Email @NotBlank String contactEmail,
    @NotNull Boolean enabled
) {
}