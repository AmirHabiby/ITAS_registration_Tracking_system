package com.aronalvarenga.rtts.modules.enrollments.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {
    Optional<Enrollment> findByRepresentativeIdAndTrainingId(UUID representativeId, UUID trainingId);
    Optional<Enrollment> findByTrainingRequestId(UUID trainingRequestId);
    List<Enrollment> findByRepresentativeIdOrderByAssessedAtDesc(UUID representativeId);
    List<Enrollment> findByTrainingIdInOrderByAssessedAtDesc(List<UUID> trainingIds);
    long countByRepresentativeId(UUID representativeId);
    long countByRepresentativeIdAndStatus(UUID representativeId, EnrollmentStatus status);
    long countByTrainingIdIn(List<UUID> trainingIds);
    long countByTrainingIdInAndStatus(List<UUID> trainingIds, EnrollmentStatus status);
    long countByStatus(EnrollmentStatus status);
    long countByTrainingId(UUID trainingId);
}
