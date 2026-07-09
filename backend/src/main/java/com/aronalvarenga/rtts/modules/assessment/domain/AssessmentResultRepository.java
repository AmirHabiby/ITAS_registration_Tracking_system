package com.aronalvarenga.rtts.modules.assessment.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssessmentResultRepository extends JpaRepository<AssessmentResult, UUID> {
	List<AssessmentResult> findByTrainingEnrollmentIdOrderByAssessmentDateDesc(UUID trainingEnrollmentId);
	List<AssessmentResult> findByTrainingEnrollmentIdAndPassedFalseOrderByAssessmentDateDesc(UUID trainingEnrollmentId);
	List<AssessmentResult> findByTrainingEnrollmentIdInOrderByAssessmentDateDesc(List<UUID> trainingEnrollmentIds);
	long countByTrainingEnrollmentIdIn(List<UUID> trainingEnrollmentIds);
	long countByTrainingEnrollmentIdInAndPassedTrue(List<UUID> trainingEnrollmentIds);
	long countByTrainingEnrollmentIdInAndPassedFalse(List<UUID> trainingEnrollmentIds);
}