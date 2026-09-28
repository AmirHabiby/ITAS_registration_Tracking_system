package com.aronalvarenga.rtts.modules.assessment.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttemptQuestionSnapshotRepository extends JpaRepository<AttemptQuestionSnapshot, UUID> {
    List<AttemptQuestionSnapshot> findByAttempt_IdOrderByDisplayOrderAsc(UUID attemptId);
}
