package com.aronalvarenga.rtts.modules.publictraining.web;

public record PublicTrainingSummaryDto(
    long totalTrainings,
    long enrolledTrainings,
    long remainingTrainings
) {
}
