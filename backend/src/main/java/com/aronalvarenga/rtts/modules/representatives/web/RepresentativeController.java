package com.aronalvarenga.rtts.modules.representatives.web;

import com.aronalvarenga.rtts.modules.representatives.application.RepresentativeService;
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
}
