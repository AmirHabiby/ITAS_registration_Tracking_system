package com.aronalvarenga.rtts.modules.requests.web;

import com.aronalvarenga.rtts.modules.requests.domain.TrainingRequestEntity;
import com.aronalvarenga.rtts.modules.representatives.domain.RepresentativeRepository;
import com.aronalvarenga.rtts.modules.trainings.domain.TrainingRepository;

public final class TrainingRequestMapper {

    private TrainingRequestMapper() {
    }

    public static TrainingRequestResponseDto toDto(TrainingRequestEntity request) {
        return new TrainingRequestResponseDto(
            request.getId(),
            request.getRepresentativeId(),
            null,
            request.getTrainingId(),
            null,
            request.getStatus(),
            request.getReviewerUsername(),
            request.getReviewerNote(),
            request.getRequestedAt(),
            request.getReviewedAt());
    }

    public static TrainingRequestResponseDto toDto(
        TrainingRequestEntity request,
        RepresentativeRepository representativeRepository,
        TrainingRepository trainingRepository
    ) {
        return new TrainingRequestResponseDto(
            request.getId(),
            request.getRepresentativeId(),
            representativeRepository.findById(request.getRepresentativeId())
                .map(representative -> representative.getFullName())
                .orElse("Unknown representative"),
            request.getTrainingId(),
            trainingRepository.findById(request.getTrainingId())
                .map(training -> training.getTitle())
                .orElse("Unknown training"),
            request.getStatus(),
            request.getReviewerUsername(),
            request.getReviewerNote(),
            request.getRequestedAt(),
            request.getReviewedAt());
    }
}