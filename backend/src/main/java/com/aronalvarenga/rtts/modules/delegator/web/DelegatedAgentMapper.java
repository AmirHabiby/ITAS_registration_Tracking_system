package com.aronalvarenga.rtts.modules.delegator.web;

import com.aronalvarenga.rtts.modules.agentdelegation.domain.AgentDelegation;
import com.aronalvarenga.rtts.modules.representatives.domain.RepresentativeRepository;

public final class DelegatedAgentMapper {

    private DelegatedAgentMapper() {
    }

    public static DelegatedAgentResponseDto toDto(AgentDelegation delegation) {
        return new DelegatedAgentResponseDto(
            delegation.getId(),
            delegation.getRepresentativeProfileId(),
            null,
            delegation.getDelegatorProfileId(),
            delegation.getStatus(),
            delegation.getDelegatedAt(),
            delegation.getRevokedAt(),
            delegation.getReason());
    }

    public static DelegatedAgentResponseDto toDto(AgentDelegation delegation, RepresentativeRepository representativeRepository) {
        return new DelegatedAgentResponseDto(
            delegation.getId(),
            delegation.getRepresentativeProfileId(),
            representativeRepository.findById(delegation.getRepresentativeProfileId())
                .map(representative -> representative.getFullName())
                .orElse("Unknown representative"),
            delegation.getDelegatorProfileId(),
            delegation.getStatus(),
            delegation.getDelegatedAt(),
            delegation.getRevokedAt(),
            delegation.getReason());
    }
}