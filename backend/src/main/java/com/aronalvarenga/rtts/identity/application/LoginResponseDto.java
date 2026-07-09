package com.aronalvarenga.rtts.identity.application;

public record LoginResponseDto(
    String token,
    long expiresInSeconds,
    String username,
    String role,
    String displayName
) {
}