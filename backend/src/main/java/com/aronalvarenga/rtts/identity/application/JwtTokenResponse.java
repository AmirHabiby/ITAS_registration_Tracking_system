package com.aronalvarenga.rtts.identity.application;

public record JwtTokenResponse(
    String token,
    long expiresInSeconds,
    String username,
    String role,
    String displayName
) {
}
