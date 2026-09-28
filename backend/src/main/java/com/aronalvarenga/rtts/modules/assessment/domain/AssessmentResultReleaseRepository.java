package com.aronalvarenga.rtts.modules.assessment.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssessmentResultReleaseRepository extends JpaRepository<AssessmentResultRelease, UUID> {
    List<AssessmentResultRelease> findByAttempt_IdOrderByReleaseNumberDesc(UUID attemptId);
    Optional<AssessmentResultRelease> findFirstByAttempt_IdOrderByReleaseNumberDesc(UUID attemptId);
    Optional<AssessmentResultRelease> findByAttempt_IdAndResult_Id(UUID attemptId, UUID resultId);
    long countByAttempt_Id(UUID attemptId);
}
