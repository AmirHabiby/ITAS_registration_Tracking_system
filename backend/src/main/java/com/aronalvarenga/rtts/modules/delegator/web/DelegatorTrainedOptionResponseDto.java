package com.aronalvarenga.rtts.modules.delegator.web;

import java.util.UUID;

public record DelegatorTrainedOptionResponseDto(
    UUID id,
    String name,
    String email,
    String type,
    String status,
    UUID firmId,
    long trainedStaffCount,
    boolean delegated
) {
}