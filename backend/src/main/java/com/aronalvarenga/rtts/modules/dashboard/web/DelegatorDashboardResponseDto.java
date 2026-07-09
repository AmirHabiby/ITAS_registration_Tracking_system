package com.aronalvarenga.rtts.modules.dashboard.web;

public record DelegatorDashboardResponseDto(
    long pendingTrainingRequests,
    long approvedTrainingRequests,
    long rejectedTrainingRequests,
    long trainedRepresentatives,
    long delegatedAgents
) {
}