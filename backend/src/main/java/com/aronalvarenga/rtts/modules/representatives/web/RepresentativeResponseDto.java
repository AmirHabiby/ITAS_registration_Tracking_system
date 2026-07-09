package com.aronalvarenga.rtts.modules.representatives.web;

import com.aronalvarenga.rtts.modules.representatives.domain.RepresentativeStatus;
import java.util.UUID;

public record RepresentativeResponseDto(
    UUID id,
    String fullName,
    String email,
    RepresentativeStatus status
) {
}