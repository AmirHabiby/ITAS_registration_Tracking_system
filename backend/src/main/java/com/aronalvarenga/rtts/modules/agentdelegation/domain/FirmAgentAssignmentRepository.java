package com.aronalvarenga.rtts.modules.agentdelegation.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FirmAgentAssignmentRepository extends JpaRepository<FirmAgentAssignment, UUID> {
    List<FirmAgentAssignment> findByFirmDelegationIdOrderByAssignedAtDesc(UUID firmDelegationId);
    List<FirmAgentAssignment> findByFirmDelegationIdAndRevokedAtIsNull(UUID firmDelegationId);
    Optional<FirmAgentAssignment> findByRepresentativeProfileIdAndRevokedAtIsNull(UUID representativeProfileId);
}