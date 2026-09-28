package com.aronalvarenga.rtts.modules.publictraining.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublicTrainingEnrollmentRepository extends JpaRepository<PublicTrainingEnrollment, UUID> {
    boolean existsByPublicTraineeIdAndTrainingId(UUID publicTraineeId, UUID trainingId);
    @Query("select enrollment from PublicTrainingEnrollment enrollment "
        + "where enrollment.publicTraineeId = :publicTraineeId "
        + "order by enrollment.enrolledAt desc")
    List<PublicTrainingEnrollment> findByPublicTraineeId(@Param("publicTraineeId") UUID publicTraineeId);
    long countByPublicTraineeId(UUID publicTraineeId);
    long countByTrainingId(UUID trainingId);
}
