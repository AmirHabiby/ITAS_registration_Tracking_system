package com.aronalvarenga.rtts.modules.delegator.web;

import com.aronalvarenga.rtts.modules.delegator.application.DelegatorService;
import com.aronalvarenga.rtts.modules.delegator.application.DelegatorAssessmentOutcomeService;
import com.aronalvarenga.rtts.modules.delegator.web.DelegatorAssessmentOutcomeDto;
import com.aronalvarenga.rtts.modules.firm.web.FirmDelegationResponseDto;
import com.aronalvarenga.rtts.modules.firm.web.FirmResponseDto;
import com.aronalvarenga.rtts.modules.requests.web.TrainingRequestMapper;
import com.aronalvarenga.rtts.modules.requests.web.TrainingRequestResponseDto;
import com.aronalvarenga.rtts.modules.representatives.web.RepresentativeMapper;
import com.aronalvarenga.rtts.modules.representatives.web.RepresentativeResponseDto;
import com.aronalvarenga.rtts.modules.representatives.domain.RepresentativeRepository;
import com.aronalvarenga.rtts.modules.trainings.domain.TrainingRepository;
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
    private final RepresentativeRepository representativeRepository;
    private final TrainingRepository trainingRepository;
    private final DelegatorAssessmentOutcomeService assessmentOutcomeService;

    public DelegatorController(
        DelegatorService delegatorService,
        RepresentativeRepository representativeRepository,
        TrainingRepository trainingRepository,
        DelegatorAssessmentOutcomeService assessmentOutcomeService
    ) {
        this.delegatorService = delegatorService;
        this.representativeRepository = representativeRepository;
        this.trainingRepository = trainingRepository;
        this.assessmentOutcomeService = assessmentOutcomeService;
    }

    @GetMapping("/assessment-outcomes")
    public List<DelegatorAssessmentOutcomeDto> releasedAssessmentOutcomes(@AuthenticationPrincipal Jwt jwt) {
        return assessmentOutcomeService.listReleasedOutcomes(jwt.getSubject());
    }

    @GetMapping("/training-requests")
    public List<TrainingRequestResponseDto> listTrainingRequests() {
        return delegatorService.listTrainingRequests().stream()
            .map(request -> TrainingRequestMapper.toDto(request, representativeRepository, trainingRepository))
            .toList();
    }

    @GetMapping("/training-requests/pending")
    public List<TrainingRequestResponseDto> listPendingTrainingRequests() {
        return delegatorService.listPendingTrainingRequests().stream()
            .map(request -> TrainingRequestMapper.toDto(request, representativeRepository, trainingRepository))
            .toList();
    }

    @PatchMapping("/training-requests/{id}/approve")
    public TrainingRequestResponseDto approve(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt, @RequestBody(required = false) DelegatorDecisionRequest request) {
        DelegatorDecisionRequest decision = request == null ? new DelegatorDecisionRequest(null) : request;
        return TrainingRequestMapper.toDto(delegatorService.approve(id, jwt, decision), representativeRepository, trainingRepository);
    }

    @PatchMapping("/training-requests/{id}/reject")
    public TrainingRequestResponseDto reject(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody DelegatorDecisionRequest request) {
        return TrainingRequestMapper.toDto(delegatorService.reject(id, jwt, request), representativeRepository, trainingRepository);
    }

    @GetMapping("/trained-representatives")
    public List<DelegatorTrainedOptionResponseDto> trainedRepresentatives() {
        return delegatorService.trainedOptions();
    }

    @PatchMapping("/representatives/{id}/mark-as-agent")
    public DelegatedAgentResponseDto markAsAgent(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt, @RequestBody(required = false) DelegatorDecisionRequest request) {
        DelegatorDecisionRequest decision = request == null ? new DelegatorDecisionRequest(null) : request;
        return DelegatedAgentMapper.toDto(delegatorService.markAsAgent(id, jwt, decision), representativeRepository);
    }

    @GetMapping("/agents")
    public List<DelegatedAgentResponseDto> agents(@AuthenticationPrincipal Jwt jwt) {
        return delegatorService.agents(jwt).stream()
            .map(agent -> DelegatedAgentMapper.toDto(agent, representativeRepository))
            .toList();
    }

    @GetMapping("/firms")
    public List<FirmResponseDto> firms() {
        return delegatorService.firms().stream()
            .map(firm -> new FirmResponseDto(firm.getId(), firm.getName(), firm.getEmail(), firm.getDescription(), firm.isActive()))
            .toList();
    }

    @PatchMapping("/firms/{id}/delegate")
    public FirmDelegationResponseDto delegateFirm(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt, @RequestBody(required = false) DelegatorDecisionRequest request) {
        var delegation = delegatorService.delegateFirm(id, jwt, request);
        return new FirmDelegationResponseDto(delegation.getId(), delegation.getFirmId(), delegation.getDelegatorProfileId(), delegation.getDelegatedAt(), delegation.getRevokedAt(), delegation.getReason());
    }

    @PatchMapping("/firms/{id}/revoke")
    public FirmDelegationResponseDto revokeFirm(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        var delegation = delegatorService.revokeFirm(id, jwt);
        return new FirmDelegationResponseDto(delegation.getId(), delegation.getFirmId(), delegation.getDelegatorProfileId(), delegation.getDelegatedAt(), delegation.getRevokedAt(), delegation.getReason());
    }
}