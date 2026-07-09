package com.aronalvarenga.rtts.modules.user.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminCreateDelegatorRequest(
    @NotBlank String username,
    @NotBlank String password,
    @NotBlank String displayName,
    @NotBlank String fullName,
    @Email @NotBlank String email,
    @NotNull Boolean enabled
) {
}