package com.aronalvarenga.rtts.modules.representative.web;

import java.util.UUID;

public record RepresentativeTrainingMaterialResponseDto(
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
