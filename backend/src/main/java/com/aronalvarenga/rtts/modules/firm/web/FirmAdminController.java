package com.aronalvarenga.rtts.modules.firm.web;

import com.aronalvarenga.rtts.modules.agentdelegation.domain.FirmAgentAssignment;
import com.aronalvarenga.rtts.modules.firm.application.FirmAdminService;
import com.aronalvarenga.rtts.modules.user.web.UserMapper;
import com.aronalvarenga.rtts.modules.user.web.UserResponseDto;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/firm-admin")
@PreAuthorize("hasRole('FIRM_ADMIN')")
public class FirmAdminController {
    private final FirmAdminService service;

    public FirmAdminController(FirmAdminService service) { this.service = service; }

    @GetMapping("/staff")
    public List<FirmStaffResponseDto> staff(@AuthenticationPrincipal Jwt jwt) {
        return service.staff(jwt).stream().map(rep -> new FirmStaffResponseDto(rep.getId(), rep.getFullName(), rep.getEmail(), rep.getStatus(), rep.getFirmId())).toList();
    }

    @PostMapping("/staff")
    public UserResponseDto createStaff(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody FirmCreateStaffRequest request) {
        return UserMapper.toDto(service.createStaff(jwt, request));
    }

    @GetMapping("/delegation")
    public FirmDelegationResponseDto delegation(@AuthenticationPrincipal Jwt jwt) {
        var delegation = service.activeDelegation(jwt);
        return delegation == null ? null : new FirmDelegationResponseDto(delegation.getId(), delegation.getFirmId(), delegation.getDelegatorProfileId(), delegation.getDelegatedAt(), delegation.getRevokedAt(), delegation.getReason());
    }

    @PostMapping("/staff/{id}/assign-agent")
    public FirmAgentAssignment assign(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @RequestBody(required = false) FirmAssignAgentRequest request) {
        return service.assign(jwt, id, request == null ? null : request.reason());
    }

    @PatchMapping("/assignments/{id}/revoke")
    public FirmAgentAssignment revoke(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return service.revokeAssignment(jwt, id);
    }
}