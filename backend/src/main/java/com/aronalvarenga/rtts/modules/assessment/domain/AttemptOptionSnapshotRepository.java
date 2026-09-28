package com.aronalvarenga.rtts.modules.assessment.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttemptOptionSnapshotRepository extends JpaRepository<AttemptOptionSnapshot, UUID> {
    List<AttemptOptionSnapshot> findByAttempt_IdAndQuestion_IdOrderByDisplayOrderAsc(
        UUID attemptId, UUID questionId);
}
