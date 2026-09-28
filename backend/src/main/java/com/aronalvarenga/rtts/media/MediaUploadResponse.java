package com.aronalvarenga.rtts.media;

public record MediaUploadResponse(
        String publicId,
        String url,
        String secureUrl,
        String resourceType,
        String format,
        Long bytes,
        String originalFilename
) {
}