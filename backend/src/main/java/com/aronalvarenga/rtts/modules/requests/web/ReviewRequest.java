package com.aronalvarenga.rtts.modules.requests.web;

import jakarta.validation.constraints.NotBlank;

public record ReviewRequest(
    @NotBlank String note
) {
}
