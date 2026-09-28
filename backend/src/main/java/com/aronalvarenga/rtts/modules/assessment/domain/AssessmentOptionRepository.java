package com.aronalvarenga.rtts.modules.assessment.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AssessmentOptionRepository extends JpaRepository<AssessmentOption, UUID> {
    List<AssessmentOption> findByQuestion_IdOrderByDisplayOrderAsc(UUID questionId);
    void deleteByIdAndQuestion_Id(UUID id, UUID questionId);

    @Modifying
    @Query("update AssessmentOption option set option.displayOrder = option.displayOrder + 10000 where option.question.id = :questionId")
    int moveOrdersOutOfRange(@Param("questionId") UUID questionId);
}
