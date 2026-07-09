package com.aronalvarenga.rtts.modules.assessment.web;

import com.aronalvarenga.rtts.modules.assessment.domain.AssessmentResult;

public final class AssessmentMapper {

    private AssessmentMapper() {
    }

    public static AssessmentResponseDto toDto(AssessmentResult assessmentResult) {
        return new AssessmentResponseDto(
            assessmentResult.getId(),
            assessmentResult.getTrainingEnrollmentId(),
            assessmentResult.getSubmittedByUserId(),
            assessmentResult.getScore(),
            assessmentResult.isPassed(),
            assessmentResult.getRemarks(),
            assessmentResult.getAssessmentDate());
    }
}