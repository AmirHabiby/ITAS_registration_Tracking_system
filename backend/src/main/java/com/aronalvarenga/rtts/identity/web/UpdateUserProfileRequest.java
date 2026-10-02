package com.aronalvarenga.rtts.identity.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserProfileRequest(
    @NotBlank @Size(max = 160) String fullName
) {
}
