package com.aronalvarenga.rtts.modules.firm.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FirmCreateStaffRequest(
    @NotBlank String username,
    @NotBlank String password,
    @NotBlank String displayName,
    @NotBlank String fullName,
    @NotBlank @Email String email,
    @NotNull Boolean enabled
) {}