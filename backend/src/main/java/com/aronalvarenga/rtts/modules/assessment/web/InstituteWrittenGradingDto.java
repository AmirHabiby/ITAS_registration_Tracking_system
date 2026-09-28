package com.aronalvarenga.rtts.modules.assessment.web;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record InstituteWrittenGradingDto(
    UUID attemptId,
    String attemptStatus,
    List<WrittenAnswerDto> answers
) {
    public record WrittenAnswerDto(
        UUID questionId,
        String prompt,
        String responseText,
        String gradingRubric,
        BigDecimal maximumMarks,
        BigDecimal awardedMarks,
        String feedback,
        UUID graderUserId,
        Instant gradedAt,
        List<GradeHistoryDto> history
    ) {}

    public record GradeHistoryDto(
        BigDecimal awardedMarks,
        String feedback,
        UUID graderUserId,
        Instant gradedAt
    ) {}
}
