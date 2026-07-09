package com.aronalvarenga.rtts.modules.enrollments.web;

import com.aronalvarenga.rtts.modules.enrollments.domain.Enrollment;

public final class EnrollmentMapper {

    private EnrollmentMapper() {
    }

    public static EnrollmentResponseDto toDto(Enrollment enrollment) {
        return new EnrollmentResponseDto(
            enrollment.getId(),
            enrollment.getRepresentativeId(),
            enrollment.getTrainingId(),
            enrollment.getAssessmentScore(),
            enrollment.getPassed(),
            enrollment.getAssessmentNote(),
            enrollment.getAssessedAt(),
            enrollment.getStatus());
    }
}