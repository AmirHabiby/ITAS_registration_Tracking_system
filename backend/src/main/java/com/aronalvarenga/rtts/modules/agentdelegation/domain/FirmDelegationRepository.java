package com.aronalvarenga.rtts.modules.agentdelegation.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FirmDelegationRepository extends JpaRepository<FirmDelegation, UUID> {
    List<FirmDelegation> findAllByOrderByDelegatedAtDesc();
    List<FirmDelegation> findByDelegatorProfileIdOrderByDelegatedAtDesc(UUID delegatorProfileId);
    Optional<FirmDelegation> findFirstByFirmIdAndRevokedAtIsNullOrderByDelegatedAtDesc(UUID firmId);
    List<FirmDelegation> findByFirmIdOrderByDelegatedAtDesc(UUID firmId);
}