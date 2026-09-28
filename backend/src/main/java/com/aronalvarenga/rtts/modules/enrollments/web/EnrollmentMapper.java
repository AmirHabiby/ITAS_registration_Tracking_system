package com.aronalvarenga.rtts.modules.enrollments.web;

import com.aronalvarenga.rtts.modules.enrollments.domain.Enrollment;
import com.aronalvarenga.rtts.modules.representatives.domain.RepresentativeRepository;
import com.aronalvarenga.rtts.modules.trainings.domain.TrainingRepository;

public final class EnrollmentMapper {

    private EnrollmentMapper() {
    }

    public static EnrollmentResponseDto toDto(Enrollment enrollment) {
        return new EnrollmentResponseDto(
            enrollment.getId(),
            enrollment.getRepresentativeId(),
            null,
            enrollment.getTrainingId(),
            null,
            enrollment.getAssessmentScore(),
            enrollment.getPassed(),
            enrollment.getAssessmentNote(),
            enrollment.getAssessedAt(),
            enrollment.getStatus());
    }

    public static EnrollmentResponseDto toDto(
        Enrollment enrollment,
        RepresentativeRepository representativeRepository,
        TrainingRepository trainingRepository
    ) {
        return new EnrollmentResponseDto(
            enrollment.getId(),
            enrollment.getRepresentativeId(),
            representativeRepository.findById(enrollment.getRepresentativeId())
                .map(representative -> representative.getFullName())
                .orElse("Unknown representative"),
            enrollment.getTrainingId(),
            trainingRepository.findById(enrollment.getTrainingId())
                .map(training -> training.getTitle())
                .orElse("Unknown training"),
            enrollment.getAssessmentScore(),
            enrollment.getPassed(),
            enrollment.getAssessmentNote(),
            enrollment.getAssessedAt(),
            enrollment.getStatus());
    }
}