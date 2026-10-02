package com.aronalvarenga.rtts.modules.representative.application;

import java.util.List;
import java.math.BigDecimal;
import java.util.UUID;

public record RepresentativeTrainingProgress(
    List<UUID> completedMaterialIds,
    int completedCount,
    int totalCount,
    int percentage,
    List<WeekProgress> weeks
) {
    public record WeekProgress(
        int weekNumber,
        UUID assessmentId,
        String assessmentTitle,
        BigDecimal passingScore,
        boolean materialsCompleted,
        boolean quizPassed,
        boolean unlocked
    ) {}
}
