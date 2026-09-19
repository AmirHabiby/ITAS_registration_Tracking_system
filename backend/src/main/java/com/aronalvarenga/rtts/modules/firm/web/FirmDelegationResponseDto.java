package com.aronalvarenga.rtts.modules.firm.web;

import java.time.Instant;
import java.util.UUID;

public record FirmDelegationResponseDto(UUID id, UUID firmId, UUID delegatorProfileId, Instant delegatedAt, Instant revokedAt, String reason) {}