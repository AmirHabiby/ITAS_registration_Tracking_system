package com.aronalvarenga.rtts.modules.delegator.web;

import com.aronalvarenga.rtts.modules.delegator.application.DelegatorService;
import com.aronalvarenga.rtts.modules.requests.web.TrainingRequestMapper;
import com.aronalvarenga.rtts.modules.requests.web.TrainingRequestResponseDto;
import com.aronalvarenga.rtts.modules.representatives.web.RepresentativeMapper;
import com.aronalvarenga.rtts.modules.representatives.web.RepresentativeResponseDto;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/delegators/me")
@PreAuthorize("hasAnyRole('SYSTEM_ADMIN','DELEGATOR')")
public class DelegatorController {

    private final DelegatorService delegatorService;

    public DelegatorController(DelegatorService delegatorService) {
        this.delegatorService = delegatorService;
    }

    @GetMapping("/training-requests")
    public List<TrainingRequestResponseDto> listTrainingRequests() {
        return delegatorService.listTrainingRequests().stream().map(TrainingRequestMapper::toDto).toList();
    }

    @GetMapping("/training-requests/pending")
    public List<TrainingRequestResponseDto> listPendingTrainingRequests() {
        return delegatorService.listPendingTrainingRequests().stream().map(TrainingRequestMapper::toDto).toList();
    }

    @PatchMapping("/training-requests/{id}/approve")
    public TrainingRequestResponseDto approve(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt, @RequestBody(required = false) DelegatorDecisionRequest request) {
        DelegatorDecisionRequest decision = request == null ? new DelegatorDecisionRequest(null) : request;
        return TrainingRequestMapper.toDto(delegatorService.approve(id, jwt, decision));
    }

    @PatchMapping("/training-requests/{id}/reject")
    public TrainingRequestResponseDto reject(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody DelegatorDecisionRequest request) {
        return TrainingRequestMapper.toDto(delegatorService.reject(id, jwt, request));
    }

    @GetMapping("/trained-representatives")
    public List<RepresentativeResponseDto> trainedRepresentatives() {
        return delegatorService.trainedRepresentatives().stream().map(RepresentativeMapper::toDto).toList();
    }

    @PatchMapping("/representatives/{id}/mark-as-agent")
    public DelegatedAgentResponseDto markAsAgent(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt, @RequestBody(required = false) DelegatorDecisionRequest request) {
        DelegatorDecisionRequest decision = request == null ? new DelegatorDecisionRequest(null) : request;
        return DelegatedAgentMapper.toDto(delegatorService.markAsAgent(id, jwt, decision));
    }

    @GetMapping("/agents")
    public List<DelegatedAgentResponseDto> agents() {
        return delegatorService.agents().stream().map(DelegatedAgentMapper::toDto).toList();
    }
}