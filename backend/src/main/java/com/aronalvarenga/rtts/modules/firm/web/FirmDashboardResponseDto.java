package com.aronalvarenga.rtts.modules.firm.web;

public record FirmDashboardResponseDto(
    long totalStaff,
    long trainedStaff,
    long staffInTraining,
    long assignedStaff,
    long availableTrainedStaff,
    boolean delegated
) {}