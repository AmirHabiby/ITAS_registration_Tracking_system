package com.aronalvarenga.rtts.modules.assessment.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssessmentAttemptAnswerRepository extends JpaRepository<AssessmentAttemptAnswer, UUID> {
    List<AssessmentAttemptAnswer> findByAttempt_IdOrderByQuestion_DisplayOrderAsc(UUID attemptId);
    Optional<AssessmentAttemptAnswer> findByAttempt_IdAndQuestion_Id(UUID attemptId, UUID questionId);
}
