package com.aronalvarenga.rtts.modules.institutes.web;

import com.aronalvarenga.rtts.modules.institutes.domain.TrainingInstitute;

public final class InstituteMapper {

    private InstituteMapper() {
    }

    public static InstituteResponseDto toDto(TrainingInstitute institute) {
        return new InstituteResponseDto(institute.getId(), institute.getName(), institute.getContactEmail(), institute.isActive());
    }
}