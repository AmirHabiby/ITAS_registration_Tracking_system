package com.aronalvarenga.rtts.modules.firm.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FirmAdminProfileRepository extends JpaRepository<FirmAdminProfile, UUID> {
    Optional<FirmAdminProfile> findByUserId(UUID userId);
    Optional<FirmAdminProfile> findByFirmId(UUID firmId);
}