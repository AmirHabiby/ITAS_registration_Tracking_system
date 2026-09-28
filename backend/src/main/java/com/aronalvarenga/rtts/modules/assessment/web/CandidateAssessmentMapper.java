package com.aronalvarenga.rtts.modules.assessment.web;

import com.aronalvarenga.rtts.modules.assessment.domain.OnlineAssessment;
import com.aronalvarenga.rtts.modules.assessment.domain.AssessmentStatus;

public final class CandidateAssessmentMapper {

    private CandidateAssessmentMapper() {
    }

    public static CandidateAssessmentDto toDto(OnlineAssessment assessment) {
        if (assessment.getStatus() != AssessmentStatus.PUBLISHED) {
            throw new IllegalArgumentException("Only published assessment versions are candidate-visible");
        }
        return new CandidateAssessmentDto(
            assessment.getId(),
            assessment.getVersionNumber(),
            assessment.getTitle(),
            assessment.getInstructions(),
            assessment.getPassingScore(),
            assessment.getQuestions().stream()
                .map(question -> new CandidateAssessmentDto.CandidateQuestionDto(
                    question.getId(),
                    question.getPrompt(),
                    question.getQuestionType().name(),
                    question.getDisplayOrder(),
                    question.getPoints(),
                    question.getOptions().stream()
                        .map(option -> new CandidateAssessmentDto.CandidateOptionDto(
                            option.getId(),
                            option.getText(),
                            option.getDisplayOrder()))
                        .toList()))
                .toList());
    }
}
