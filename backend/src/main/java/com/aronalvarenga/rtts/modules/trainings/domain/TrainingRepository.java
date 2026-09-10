package com.aronalvarenga.rtts.modules.trainings.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainingRepository extends JpaRepository<Training, UUID> {
    List<Training> findByActiveTrueAndAccessTypeNotOrderByStartDateAsc(TrainingAccessType accessType);
    List<Training> findByTrainingInstituteProfileIdOrderByStartDateAsc(UUID trainingInstituteProfileId);
    long countByTrainingInstituteProfileId(UUID trainingInstituteProfileId);
    long countByTrainingInstituteProfileIdAndStatus(UUID trainingInstituteProfileId, TrainingStatus status);
    long countByActiveTrue();
}
