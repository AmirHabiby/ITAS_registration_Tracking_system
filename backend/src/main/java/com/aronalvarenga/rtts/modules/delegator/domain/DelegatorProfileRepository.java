package com.aronalvarenga.rtts.modules.delegator.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DelegatorProfileRepository extends JpaRepository<DelegatorProfile, UUID> {
	Optional<DelegatorProfile> findByUserId(UUID userId);
}