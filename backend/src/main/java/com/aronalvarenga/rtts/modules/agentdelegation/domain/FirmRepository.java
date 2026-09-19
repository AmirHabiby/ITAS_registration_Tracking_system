package com.aronalvarenga.rtts.modules.agentdelegation.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FirmRepository extends JpaRepository<Firm, UUID> {
    Optional<Firm> findByName(String name);
    List<Firm> findByActiveTrueOrderByNameAsc();
}