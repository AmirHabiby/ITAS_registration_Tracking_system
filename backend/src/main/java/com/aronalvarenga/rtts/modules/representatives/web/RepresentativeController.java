package com.aronalvarenga.rtts.modules.representatives.web;

import com.aronalvarenga.rtts.modules.representatives.application.RepresentativeService;
import com.aronalvarenga.rtts.modules.representatives.domain.Representative;
import com.aronalvarenga.rtts.modules.user.web.UserMapper;
import com.aronalvarenga.rtts.modules.user.web.UserResponseDto;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/representatives")
public class RepresentativeController {

    private final RepresentativeService representativeService;

    public RepresentativeController(RepresentativeService representativeService) {
        this.representativeService = representativeService;
    }

    @GetMapping
    public List<RepresentativeResponseDto> list() {
        return representativeService.list().stream().map(RepresentativeMapper::toDto).toList();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','DELEGATOR')")
    public RepresentativeResponseDto create(@Valid @RequestBody RepresentativeRequest request) {
        return RepresentativeMapper.toDto(representativeService.create(request));
    }

    @PatchMapping("/{id}/trained")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','DELEGATOR')")
    public RepresentativeResponseDto markTrained(@PathVariable UUID id) {
        return RepresentativeMapper.toDto(representativeService.markTrained(id));
    }

    @PatchMapping("/{id}/agent")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','DELEGATOR')")
    public RepresentativeResponseDto markAgent(@PathVariable UUID id) {
        return RepresentativeMapper.toDto(representativeService.markAgent(id));
    }

    @GetMapping("/me/profile")
    @PreAuthorize("hasRole('REPRESENTATIVE')")
    public RepresentativeProfileResponseDto ownProfile(@AuthenticationPrincipal Jwt jwt) {
        Representative representative = representativeService.getOwnProfile(jwt.getSubject());
        return new RepresentativeProfileResponseDto(representative.getFullName());
    }

    @PatchMapping(value = "/me/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('REPRESENTATIVE')")
    public UserResponseDto updateOwnProfile(
            @Valid @RequestPart("profile") UpdateRepresentativeProfileRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return UserMapper.toDto(representativeService.updateOwnProfile(jwt.getSubject(), request, image));
    }
}
