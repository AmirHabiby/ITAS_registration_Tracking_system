package com.aronalvarenga.rtts.modules.firm.web;

import java.util.UUID;

public record FirmResponseDto(UUID id, String name, String email, String description, boolean active) {}