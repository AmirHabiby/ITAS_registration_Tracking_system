package com.aronalvarenga.rtts.modules.requests.web;

import com.aronalvarenga.rtts.modules.requests.application.TrainingRequestService;
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
@RequestMapping("/api/training-requests")
public class TrainingRequestController {

    private final TrainingRequestService trainingRequestService;

    public TrainingRequestController(TrainingRequestService trainingRequestService) {
        this.trainingRequestService = trainingRequestService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','DELEGATOR')")
    public List<TrainingRequestResponseDto> listAll() {
        return trainingRequestService.listAll().stream().map(TrainingRequestMapper::toDto).toList();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','REPRESENTATIVE')")
    public TrainingRequestResponseDto requestTraining(@Valid @RequestBody TrainingRequestDto request) {
        return TrainingRequestMapper.toDto(trainingRequestService.requestTraining(request));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','DELEGATOR')")
    public TrainingRequestResponseDto approve(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ReviewRequest request) {
        return TrainingRequestMapper.toDto(trainingRequestService.approve(id, jwt, request));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','DELEGATOR')")
    public TrainingRequestResponseDto reject(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ReviewRequest request) {
        return TrainingRequestMapper.toDto(trainingRequestService.reject(id, jwt, request));
    }

    @GetMapping("/{id}")
    public TrainingRequestResponseDto get(@PathVariable UUID id) {
        return TrainingRequestMapper.toDto(trainingRequestService.get(id));
    }
}
