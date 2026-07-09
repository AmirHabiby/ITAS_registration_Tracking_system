package com.aronalvarenga.rtts.modules.representative.web;

import com.aronalvarenga.rtts.modules.enrollments.domain.Enrollment;

public final class RepresentativeResultMapper {

    private RepresentativeResultMapper() {
    }

    public static RepresentativeResultResponseDto toDto(Enrollment enrollment) {
        return new RepresentativeResultResponseDto(
            enrollment.getId(),
            enrollment.getTrainingId(),
            enrollment.getAssessmentScore(),
            enrollment.getPassed(),
            enrollment.getAssessmentNote(),
            enrollment.getAssessedAt(),
            enrollment.getStatus());
    }
}