package com.aronalvarenga.rtts.modules.agentdelegation.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentDelegationRepository extends JpaRepository<AgentDelegation, UUID> {
    List<AgentDelegation> findAllByOrderByDelegatedAtDesc();
    List<AgentDelegation> findByDelegatorProfileIdOrderByDelegatedAtDesc(UUID delegatorProfileId);
    List<AgentDelegation> findByRepresentativeProfileIdOrderByDelegatedAtDesc(UUID representativeProfileId);
    boolean existsByRepresentativeProfileIdAndStatus(UUID representativeProfileId, AgentDelegationStatus status);
    long countByStatus(AgentDelegationStatus status);
}