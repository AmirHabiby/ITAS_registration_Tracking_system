package com.aronalvarenga.rtts.modules.publictraining.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublicTrainingEnrollmentRepository extends JpaRepository<PublicTrainingEnrollment, UUID> {
    boolean existsByPublicTraineeIdAndTrainingId(UUID publicTraineeId, UUID trainingId);
    List<PublicTrainingEnrollment> findByPublicTraineeIdOrderByEnrolledAtDesc(UUID publicTraineeId);
    long countByPublicTraineeId(UUID publicTraineeId);
    long countByTrainingId(UUID trainingId);
}
