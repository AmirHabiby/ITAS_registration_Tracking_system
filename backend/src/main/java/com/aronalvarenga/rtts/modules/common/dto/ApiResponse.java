package com.aronalvarenga.rtts.modules.common.dto;

public record ApiResponse(
    boolean success,
    String message
) {
    public static ApiResponse success(String message) {
        return new ApiResponse(true, message);
    }
}