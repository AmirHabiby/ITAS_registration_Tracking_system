package com.aronalvarenga.rtts.modules.user.web;

import com.aronalvarenga.rtts.modules.user.application.AdminService;
import com.aronalvarenga.rtts.modules.firm.web.FirmResponseDto;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping(value = "/representatives", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserResponseDto createRepresentative(
            @Valid @RequestPart("request") AdminCreateRepresentativeRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        return UserMapper.toDto(adminService.createRepresentative(request, image));
    }

    @PostMapping("/firms")
    public UserResponseDto createFirm(@Valid @RequestBody AdminCreateFirmRequest request) {
        return UserMapper.toDto(adminService.createFirm(request, null));
    }

    @PostMapping(value = "/firms", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserResponseDto createFirm(
            @Valid @RequestPart("request") AdminCreateFirmRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        return UserMapper.toDto(adminService.createFirm(request, image));
    }

    @GetMapping("/firms")
    public List<FirmResponseDto> listFirms() {
        return adminService.listFirms().stream()
            .map(firm -> new FirmResponseDto(firm.getId(), firm.getName(), firm.getEmail(), firm.getDescription(), firm.isActive()))
            .toList();
    }

    @PostMapping("/delegators")
    public UserResponseDto createDelegator(@Valid @RequestBody AdminCreateDelegatorRequest request) {
        return UserMapper.toDto(adminService.createDelegator(request, null));
    }

    @PostMapping(value = "/delegators", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserResponseDto createDelegator(
            @Valid @RequestPart("request") AdminCreateDelegatorRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        return UserMapper.toDto(adminService.createDelegator(request, image));
    }

    @PostMapping("/training-institutes")
    public UserResponseDto createTrainingInstitute(@Valid @RequestBody AdminCreateTrainingInstituteRequest request) {
        return UserMapper.toDto(adminService.createTrainingInstitute(request, null));
    }

    @PostMapping(value = "/training-institutes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserResponseDto createTrainingInstitute(
            @Valid @RequestPart("request") AdminCreateTrainingInstituteRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        return UserMapper.toDto(adminService.createTrainingInstitute(request, image));
    }

    @GetMapping("/users")
    public List<UserResponseDto> listUsers() {
        return adminService.listUsers().stream().map(UserMapper::toDto).toList();
    }

    @PatchMapping("/users/{id}/activate")
    public UserResponseDto activate(@PathVariable UUID id) {
        return UserMapper.toDto(adminService.activateUser(id));
    }

    @PatchMapping("/users/{id}/deactivate")
    public UserResponseDto deactivate(@PathVariable UUID id) {
        return UserMapper.toDto(adminService.deactivateUser(id));
    }
}