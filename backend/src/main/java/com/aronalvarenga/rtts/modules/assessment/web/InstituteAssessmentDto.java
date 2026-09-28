package com.aronalvarenga.rtts.modules.assessment.web;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record InstituteAssessmentDto(
    UUID id,
    UUID assessmentSeriesId,
    int version,
    UUID trainingId,
    String title,
    String instructions,
    BigDecimal passingScore,
    int durationMinutes,
    int attemptLimit,
    Instant availableFrom,
    Instant availableUntil,
    boolean randomizeQuestions,
    boolean randomizeOptions,
    String status,
    List<QuestionDto> questions
) {
    public record QuestionDto(
        UUID id,
        String prompt,
        String questionType,
        int displayOrder,
        BigDecimal points,
        String gradingRubric,
        List<OptionDto> options
    ) {}

    public record OptionDto(UUID id, String text, int displayOrder, boolean correct) {}
}
