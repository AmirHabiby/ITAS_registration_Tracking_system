package com.aronalvarenga.rtts.modules.requests.web;

import com.aronalvarenga.rtts.modules.requests.domain.TrainingRequestEntity;

public final class TrainingRequestMapper {

    private TrainingRequestMapper() {
    }

    public static TrainingRequestResponseDto toDto(TrainingRequestEntity request) {
        return new TrainingRequestResponseDto(
            request.getId(),
            request.getRepresentativeId(),
            request.getTrainingId(),
            request.getStatus(),
            request.getReviewerUsername(),
            request.getReviewerNote(),
            request.getRequestedAt(),
            request.getReviewedAt());
    }
}