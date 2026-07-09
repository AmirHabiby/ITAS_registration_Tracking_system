package com.aronalvarenga.rtts.identity.application;

import java.util.UUID;

public record CurrentUserResponseDto(
    UUID id,
    String username,
    String role,
    String displayName,
    boolean enabled
) {
}