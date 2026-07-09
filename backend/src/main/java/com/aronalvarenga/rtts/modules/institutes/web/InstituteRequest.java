package com.aronalvarenga.rtts.modules.institutes.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record InstituteRequest(
    @NotBlank String name,
    @Email @NotBlank String contactEmail
) {
}
