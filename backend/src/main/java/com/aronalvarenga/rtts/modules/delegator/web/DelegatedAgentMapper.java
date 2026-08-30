package com.aronalvarenga.rtts.modules.delegator.web;

import com.aronalvarenga.rtts.modules.agentdelegation.domain.AgentDelegation;

public final class DelegatedAgentMapper {

    private DelegatedAgentMapper() {
    }

    public static DelegatedAgentResponseDto toDto(AgentDelegation delegation) {
        return new DelegatedAgentResponseDto(
            delegation.getId(),
            delegation.getRepresentativeProfileId(),
            delegation.getDelegatorProfileId(),
            delegation.getStatus(),
            delegation.getDelegatedAt(),
            delegation.getRevokedAt(),
            delegation.getReason());
    }
}