package com.aronalvarenga.rtts.modules.delegator.web;

import java.time.Instant;
import java.util.UUID;

public record DelegatedAgentResponseDto(
    UUID delegationId,
    UUID representativeId,
    UUID delegatorId,
    Instant delegatedAt,
    String reason
) {
}