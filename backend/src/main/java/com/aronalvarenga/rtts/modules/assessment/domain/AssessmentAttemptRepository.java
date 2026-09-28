package com.aronalvarenga.rtts.modules.assessment.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.Instant;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AssessmentAttemptRepository extends JpaRepository<AssessmentAttempt, UUID> {
    List<AssessmentAttempt> findByEnrollment_IdOrderByStartedAtDesc(UUID enrollmentId);
    List<AssessmentAttempt> findByStatusInOrderByStartedAtDesc(List<AssessmentAttemptStatus> statuses);
    Optional<AssessmentAttempt> findByIdAndEnrollment_Id(UUID id, UUID enrollmentId);
    long countByAssessment_IdAndEnrollment_Id(UUID assessmentId, UUID enrollmentId);
    long countByAssessment_Id(UUID assessmentId);
    Optional<AssessmentAttempt> findFirstByAssessment_IdAndEnrollment_IdAndStatus(
        UUID assessmentId, UUID enrollmentId, AssessmentAttemptStatus status);
    List<AssessmentAttempt> findByStatusAndDeadlineAtLessThanEqual(
        AssessmentAttemptStatus status, Instant deadlineAt);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select attempt from AssessmentAttempt attempt where attempt.id = :id")
    Optional<AssessmentAttempt> findByIdForUpdate(@Param("id") UUID id);
}
