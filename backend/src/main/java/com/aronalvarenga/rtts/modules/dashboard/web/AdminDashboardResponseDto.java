package com.aronalvarenga.rtts.modules.dashboard.web;

public record AdminDashboardResponseDto(
    long totalRepresentatives,
    long totalDelegators,
    long totalTrainingInstitutes,
    long totalTrainings,
    long totalTrainedRepresentatives,
    long totalAgents,
    long pendingRequests,
    long approvedRequests,
    long rejectedRequests
) {
}