package com.aronalvarenga.rtts.modules.representatives.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RepresentativeRequest(
    @NotBlank String fullName,
    @Email @NotBlank String email
) {
}
