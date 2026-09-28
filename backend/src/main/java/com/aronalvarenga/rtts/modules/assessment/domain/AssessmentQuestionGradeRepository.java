package com.aronalvarenga.rtts.modules.assessment.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssessmentQuestionGradeRepository extends JpaRepository<AssessmentQuestionGrade, UUID> {
    List<AssessmentQuestionGrade> findByAttempt_IdAndQuestion_IdOrderByGradeRevisionDesc(
        UUID attemptId, UUID questionId);
    List<AssessmentQuestionGrade> findByAttempt_IdOrderByGradeRevisionDesc(UUID attemptId);
    Optional<AssessmentQuestionGrade> findFirstByAttempt_IdAndQuestion_IdOrderByGradeRevisionDesc(
        UUID attemptId, UUID questionId);

    long countByAttempt_Id(UUID attemptId);
}
