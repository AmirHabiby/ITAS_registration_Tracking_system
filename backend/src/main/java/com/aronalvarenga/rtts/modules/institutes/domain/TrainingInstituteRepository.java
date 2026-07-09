package com.aronalvarenga.rtts.modules.institutes.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainingInstituteRepository extends JpaRepository<TrainingInstitute, UUID> {
	Optional<TrainingInstitute> findByUserId(UUID userId);
}
