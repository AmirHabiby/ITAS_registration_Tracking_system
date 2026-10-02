package com.aronalvarenga.rtts.modules.representatives.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateRepresentativeProfileRequest(
    @NotBlank @Size(max = 160) String fullName
) {
}
