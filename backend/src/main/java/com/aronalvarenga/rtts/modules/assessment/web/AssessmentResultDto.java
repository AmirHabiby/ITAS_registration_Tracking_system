package com.aronalvarenga.rtts.modules.assessment.web;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AssessmentResultDto(
    UUID attemptId,
    int attemptNumber,
    BigDecimal awardedMarks,
    BigDecimal maximumMarks,
    BigDecimal percentage,
    boolean passed,
    Instant evaluatedAt,
    Instant releasedAt,
    List<QuestionFeedbackDto> writtenFeedback
) {
    public record QuestionFeedbackDto(
        UUID questionId,
        String prompt,
        BigDecimal awardedMarks,
        BigDecimal maximumMarks,
        String feedback
    ) {}
}
