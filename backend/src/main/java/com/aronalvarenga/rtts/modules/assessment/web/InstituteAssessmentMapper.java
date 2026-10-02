package com.aronalvarenga.rtts.modules.assessment.web;

import com.aronalvarenga.rtts.modules.assessment.domain.OnlineAssessment;

public final class InstituteAssessmentMapper {

    private InstituteAssessmentMapper() {
    }

    public static InstituteAssessmentDto toDto(OnlineAssessment assessment) {
        return new InstituteAssessmentDto(
            assessment.getId(),
            assessment.getAssessmentSeriesId(),
            assessment.getVersionNumber(),
            assessment.getTraining().getId(),
            assessment.getTitle(),
            assessment.getInstructions(),
            assessment.getPassingScore(),
            assessment.getWeekNumber(),
            assessment.getDurationMinutes(),
            assessment.getAttemptLimit(),
            assessment.getAvailableFrom(),
            assessment.getAvailableUntil(),
            assessment.isRandomizeQuestions(),
            assessment.isRandomizeOptions(),
            assessment.getStatus().name(),
            assessment.getQuestions().stream()
                .map(question -> new InstituteAssessmentDto.QuestionDto(
                    question.getId(),
                    question.getPrompt(),
                    question.getQuestionType().name(),
                    question.getDisplayOrder(),
                    question.getPoints(),
                    question.getGradingRubric(),
                    question.getOptions().stream()
                        .map(option -> new InstituteAssessmentDto.OptionDto(
                            option.getId(), option.getText(), option.getDisplayOrder(), option.isCorrect()))
                        .toList()))
                .toList());
    }
}
