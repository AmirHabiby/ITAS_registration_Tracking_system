package com.aronalvarenga.rtts.modules.publictraining.web;

import java.util.UUID;

public record PublicTrainingMaterialResponseDto(
    UUID id,
    UUID trainingId,
    String title,
    String description,
    String materialType,
    String fileUrl,
    Long fileSizeBytes,
    int weekNumber
) {
}
