package com.aronalvarenga.rtts.modules.representatives.web;

import com.aronalvarenga.rtts.modules.representatives.domain.Representative;

public final class RepresentativeMapper {

    private RepresentativeMapper() {
    }

    public static RepresentativeResponseDto toDto(Representative representative) {
        return new RepresentativeResponseDto(representative.getId(), representative.getFullName(), representative.getEmail(), representative.getStatus());
    }
}