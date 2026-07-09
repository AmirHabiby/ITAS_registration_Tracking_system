package com.aronalvarenga.rtts.modules.institutes.web;

import com.aronalvarenga.rtts.modules.institutes.application.InstituteService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/institutes")
public class InstituteController {

    private final InstituteService instituteService;

    public InstituteController(InstituteService instituteService) {
        this.instituteService = instituteService;
    }

    @GetMapping
    public List<InstituteResponseDto> list() {
        return instituteService.list().stream().map(InstituteMapper::toDto).toList();
    }

    @GetMapping("/{id}")
    public InstituteResponseDto get(@PathVariable UUID id) {
        return InstituteMapper.toDto(instituteService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public InstituteResponseDto create(@Valid @RequestBody InstituteRequest request) {
        return InstituteMapper.toDto(instituteService.create(request));
    }
}
