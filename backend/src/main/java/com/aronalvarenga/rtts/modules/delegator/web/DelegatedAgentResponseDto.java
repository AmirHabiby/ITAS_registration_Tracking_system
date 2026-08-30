package com.aronalvarenga.rtts.modules.delegator.web;

import com.aronalvarenga.rtts.modules.agentdelegation.domain.AgentDelegationStatus;
import java.time.Instant;
import java.util.UUID;

public record DelegatedAgentResponseDto(
    UUID delegationId,
    UUID representativeId,
    UUID delegatorId,
    AgentDelegationStatus status,
    Instant delegatedAt,
    Instant revokedAt,
    String reason
) {
}