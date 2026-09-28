package com.aronalvarenga.rtts.modules.assessment.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OnlineAssessmentRepository extends JpaRepository<OnlineAssessment, UUID> {
    List<OnlineAssessment> findByTraining_IdAndStatusOrderByCreatedAtDesc(
        UUID trainingId,
        AssessmentStatus status
    );

    Optional<OnlineAssessment> findTopByAssessmentSeriesIdOrderByVersionNumberDesc(UUID assessmentSeriesId);

    Optional<OnlineAssessment> findTopByAssessmentSeriesIdAndStatusOrderByVersionNumberDesc(
        UUID assessmentSeriesId,
        AssessmentStatus status
    );

    boolean existsByAssessmentSeriesIdAndStatus(UUID assessmentSeriesId, AssessmentStatus status);

    List<OnlineAssessment> findByTraining_IdOrderByCreatedAtDesc(UUID trainingId);
    List<OnlineAssessment> findByAssessmentSeriesIdOrderByVersionNumberDesc(UUID assessmentSeriesId);

    List<OnlineAssessment> findByTraining_IdInAndStatusOrderByCreatedAtDesc(
        List<UUID> trainingIds, AssessmentStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select assessment from OnlineAssessment assessment where assessment.id = :id")
    Optional<OnlineAssessment> findByIdForUpdate(@Param("id") UUID id);
}
