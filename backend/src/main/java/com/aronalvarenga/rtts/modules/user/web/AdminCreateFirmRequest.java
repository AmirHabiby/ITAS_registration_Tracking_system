package com.aronalvarenga.rtts.modules.user.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminCreateFirmRequest(
    @NotBlank String name,
    @NotBlank @Email String email,
    String description,
    @NotBlank String adminUsername,
    @NotBlank String adminPassword,
    @NotBlank String adminDisplayName,
    @NotBlank String adminFullName,
    @NotBlank @Email String adminEmail,
    @NotNull Boolean enabled
) {}