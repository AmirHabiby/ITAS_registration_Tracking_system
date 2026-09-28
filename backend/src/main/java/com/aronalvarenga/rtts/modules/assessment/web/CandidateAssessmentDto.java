package com.aronalvarenga.rtts.modules.assessment.web;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CandidateAssessmentDto(
    UUID id,
    int version,
    String title,
    String instructions,
    BigDecimal passingScore,
    List<CandidateQuestionDto> questions
) {
    public record CandidateQuestionDto(
        UUID id,
        String prompt,
        String questionType,
        int displayOrder,
        BigDecimal points,
        List<CandidateOptionDto> options
    ) {}

    public record CandidateOptionDto(UUID id, String text, int displayOrder) {}
}
