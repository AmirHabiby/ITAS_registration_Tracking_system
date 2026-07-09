package com.aronalvarenga.rtts.modules.institutes.web;

import java.util.UUID;

public record InstituteResponseDto(
    UUID id,
    String name,
    String contactEmail,
    boolean active
) {
}