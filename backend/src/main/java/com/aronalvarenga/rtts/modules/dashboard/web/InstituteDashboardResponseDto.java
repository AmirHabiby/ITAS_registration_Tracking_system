package com.aronalvarenga.rtts.modules.dashboard.web;

public record InstituteDashboardResponseDto(
    long totalTrainings,
    long enrolledRepresentatives,
    long completedTrainings,
    long failedTrainees,
    long retakeRequiredTrainees
) {
}