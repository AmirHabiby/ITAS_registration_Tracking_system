package com.aronalvarenga.rtts.modules.user.web;

import com.aronalvarenga.rtts.modules.user.application.AdminService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/representatives")
    public UserResponseDto createRepresentative(@Valid @RequestBody AdminCreateRepresentativeRequest request) {
        return UserMapper.toDto(adminService.createRepresentative(request));
    }

    @PostMapping("/delegators")
    public UserResponseDto createDelegator(@Valid @RequestBody AdminCreateDelegatorRequest request) {
        return UserMapper.toDto(adminService.createDelegator(request));
    }

    @PostMapping("/training-institutes")
    public UserResponseDto createTrainingInstitute(@Valid @RequestBody AdminCreateTrainingInstituteRequest request) {
        return UserMapper.toDto(adminService.createTrainingInstitute(request));
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