package com.aronalvarenga.rtts.modules.assessment.web;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CandidateAttemptDto(
    UUID id,
    UUID assessmentId,
    String assessmentTitle,
    int assessmentVersion,
    String status,
    Instant startedAt,
    Instant deadlineAt,
    Instant submittedAt,
    Instant expiredAt,
    long remainingSeconds,
    List<QuestionDto> questions
) {
    public record QuestionDto(
        UUID id,
        String prompt,
        String questionType,
        BigDecimal points,
        int displayOrder,
        List<OptionDto> options,
        UUID selectedOptionId,
        String responseText
    ) {}

    public record OptionDto(UUID id, String text, int displayOrder) {}
}
