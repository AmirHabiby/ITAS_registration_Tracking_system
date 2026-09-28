package com.aronalvarenga.rtts.modules.assessment.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface AssessmentQuestionRepository extends JpaRepository<AssessmentQuestion, UUID> {
    List<AssessmentQuestion> findByAssessment_IdOrderByDisplayOrderAsc(UUID assessmentId);
    Optional<AssessmentQuestion> findByIdAndAssessment_Id(UUID id, UUID assessmentId);
    void deleteByIdAndAssessment_Id(UUID id, UUID assessmentId);

    @Modifying
    @Query("update AssessmentQuestion q set q.displayOrder = q.displayOrder + 10000 where q.assessment.id = :assessmentId")
    int moveOrdersOutOfRange(@Param("assessmentId") UUID assessmentId);
}
