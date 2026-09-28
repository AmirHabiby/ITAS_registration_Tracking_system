package com.aronalvarenga.rtts.modules.trainings.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainingMaterialRepository extends JpaRepository<TrainingMaterial, UUID> {
    List<TrainingMaterial> findByTrainingIdOrderByWeekNumberAscCreatedAtAsc(UUID trainingId);
}
