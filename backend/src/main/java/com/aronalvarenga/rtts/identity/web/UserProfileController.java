package com.aronalvarenga.rtts.identity.web;

import com.aronalvarenga.rtts.identity.application.UserProfileService;
import com.aronalvarenga.rtts.identity.application.UserProfileResponse;
import com.aronalvarenga.rtts.modules.user.web.UserMapper;
import com.aronalvarenga.rtts.modules.user.web.UserResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/profile")
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/me")
    public UserProfileResponse getOwnProfile(@AuthenticationPrincipal Jwt jwt) {
        return userProfileService.current(jwt.getSubject());
    }

    @PatchMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserResponseDto updateOwnProfile(
            @Valid @RequestPart("profile") UpdateUserProfileRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return UserMapper.toDto(userProfileService.update(jwt.getSubject(), request, image));
    }
}
