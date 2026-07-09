package com.aronalvarenga.rtts.modules.user.web;

import com.aronalvarenga.rtts.identity.domain.UserRole;
import java.util.UUID;

public record UserResponseDto(
    UUID id,
    String username,
    UserRole role,
    boolean enabled,
    String displayName
) {
}