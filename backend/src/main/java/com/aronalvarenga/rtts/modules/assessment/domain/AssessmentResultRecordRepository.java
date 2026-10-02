package com.aronalvarenga.rtts.modules.assessment.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AssessmentResultRecordRepository extends JpaRepository<AssessmentResultRecord, UUID> {
    List<AssessmentResultRecord> findByAttempt_IdOrderByEvaluatedAtDesc(UUID attemptId);
    Optional<AssessmentResultRecord> findFirstByAttempt_IdAndFinalResultTrueOrderByGradingRevisionDesc(
        UUID attemptId);
    Optional<AssessmentResultRecord> findFirstByAttempt_IdOrderByEvaluatedAtDesc(UUID attemptId);

    @Query("""
        select (count(result) > 0)
        from AssessmentResultRecord result
        join result.attempt attempt
        join attempt.assessment assessment
        where attempt.enrollment.representativeId = :representativeId
          and assessment.id = :assessmentId
          and result.passed = true
          and result.finalResult = true
        """)
    boolean existsPassedFinalResult(
        @Param("representativeId") UUID representativeId,
        @Param("assessmentId") UUID assessmentId);
}
