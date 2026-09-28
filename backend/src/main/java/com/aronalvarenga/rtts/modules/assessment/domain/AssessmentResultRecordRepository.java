package com.aronalvarenga.rtts.modules.assessment.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssessmentResultRecordRepository extends JpaRepository<AssessmentResultRecord, UUID> {
    List<AssessmentResultRecord> findByAttempt_IdOrderByEvaluatedAtDesc(UUID attemptId);
    Optional<AssessmentResultRecord> findFirstByAttempt_IdAndFinalResultTrueOrderByGradingRevisionDesc(
        UUID attemptId);
    Optional<AssessmentResultRecord> findFirstByAttempt_IdOrderByEvaluatedAtDesc(UUID attemptId);
}
