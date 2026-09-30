package com.aronalvarenga.rtts.modules.dashboard.web;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateSystemAnnouncementRequest(
    @NotNull
    @Size(max = 2000)
    String text
) {
}
