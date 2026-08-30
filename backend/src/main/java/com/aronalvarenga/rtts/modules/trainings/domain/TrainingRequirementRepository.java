package com.aronalvarenga.rtts.modules.trainings.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainingRequirementRepository extends JpaRepository<TrainingRequirement, UUID> {
    List<TrainingRequirement> findByTrainingIdOrderByNameAsc(UUID trainingId);
    long countByTrainingIdAndRequired(UUID trainingId, boolean required);
}
