package com.aronalvarenga.rtts.modules.firm.web;

import com.aronalvarenga.rtts.modules.representatives.domain.RepresentativeStatus;
import java.util.UUID;

public record FirmStaffResponseDto(UUID id, String fullName, String email, RepresentativeStatus status, UUID firmId) {}