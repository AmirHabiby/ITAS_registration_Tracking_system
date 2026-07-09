package com.aronalvarenga.rtts.modules.user.web;

import com.aronalvarenga.rtts.identity.domain.UserAccount;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserResponseDto toDto(UserAccount userAccount) {
        return new UserResponseDto(userAccount.getId(), userAccount.getUsername(), userAccount.getRole(), userAccount.isEnabled(), userAccount.getDisplayName());
    }
}