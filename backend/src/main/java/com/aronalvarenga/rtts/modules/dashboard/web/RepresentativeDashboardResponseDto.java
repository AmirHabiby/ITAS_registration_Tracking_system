package com.aronalvarenga.rtts.modules.dashboard.web;

import com.aronalvarenga.rtts.modules.representatives.domain.RepresentativeStatus;

public record RepresentativeDashboardResponseDto(
    long availableTrainings,
    long myPendingRequests,
    long myApprovedTrainings,
    long myCompletedTrainings,
    RepresentativeStatus currentRepresentativeStatus
) {
}