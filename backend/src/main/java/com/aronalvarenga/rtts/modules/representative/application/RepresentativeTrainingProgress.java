package com.aronalvarenga.rtts.modules.representative.application;

import java.util.List;
import java.util.UUID;

public record RepresentativeTrainingProgress(
    List<UUID> completedMaterialIds,
    int completedCount,
    int totalCount,
    int percentage
) {
}
