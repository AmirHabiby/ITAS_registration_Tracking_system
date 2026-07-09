package com.aronalvarenga.rtts.modules.trainings.web;

import com.aronalvarenga.rtts.modules.trainings.domain.Training;

public final class TrainingMapper {

    private TrainingMapper() {
    }

    public static TrainingResponseDto toDto(Training training) {
        return new TrainingResponseDto(
            training.getId(),
            training.getTrainingInstituteProfileId(),
            training.getTitle(),
            training.getDescription(),
            training.getStartDate(),
            training.getEndDate(),
            training.getCapacity(),
            training.getPassingScore(),
            training.getAllowedRetakeAttempts(),
            training.getStatus(),
            training.isActive());
    }
}