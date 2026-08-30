package com.aronalvarenga.rtts.modules.representative.web;

import com.aronalvarenga.rtts.identity.domain.UserAccountRepository;
import com.aronalvarenga.rtts.modules.dashboard.application.DashboardService;
import com.aronalvarenga.rtts.modules.representatives.domain.RepresentativeRepository;
import com.aronalvarenga.rtts.modules.requests.application.TrainingRequestService;
import com.aronalvarenga.rtts.modules.requests.domain.TrainingRequestRepository;
import com.aronalvarenga.rtts.modules.requests.web.TrainingRequestMapper;
import com.aronalvarenga.rtts.modules.requests.web.TrainingRequestResponseDto;
import com.aronalvarenga.rtts.modules.trainings.application.TrainingService;
import com.aronalvarenga.rtts.modules.trainings.web.TrainingMapper;
import com.aronalvarenga.rtts.modules.trainings.web.TrainingResponseDto;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/representatives/me")
@PreAuthorize("hasAnyRole('SYSTEM_ADMIN','REPRESENTATIVE')")
public class RepresentativePortalController {

    private final TrainingService trainingService;
    private final TrainingRequestService trainingRequestService;
    private final DashboardService dashboardService;
    private final UserAccountRepository userAccountRepository;
    private final RepresentativeRepository representativeRepository;
    private final TrainingRequestRepository trainingRequestRepository;
    private final com.aronalvarenga.rtts.modules.enrollments.domain.EnrollmentRepository enrollmentRepository;

    public RepresentativePortalController(
        TrainingService trainingService,
        TrainingRequestService trainingRequestService,
        DashboardService dashboardService,
        UserAccountRepository userAccountRepository,
        RepresentativeRepository representativeRepository,
        TrainingRequestRepository trainingRequestRepository,
        com.aronalvarenga.rtts.modules.enrollments.domain.EnrollmentRepository enrollmentRepository
    ) {
        this.trainingService = trainingService;
        this.trainingRequestService = trainingRequestService;
        this.dashboardService = dashboardService;
        this.userAccountRepository = userAccountRepository;
        this.representativeRepository = representativeRepository;
        this.trainingRequestRepository = trainingRequestRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @GetMapping("/trainings")
    public List<TrainingResponseDto> availableTrainings() {
        return trainingService.listAvailable().stream().map(TrainingMapper::toDto).toList();
    }

    @PostMapping("/training-requests")
    public TrainingRequestResponseDto requestTraining(@Valid @RequestBody com.aronalvarenga.rtts.modules.requests.web.TrainingRequestDto request, @AuthenticationPrincipal Jwt jwt) {
        java.util.UUID userId = userAccountRepository.findByUsername(jwt.getSubject())
            .map(com.aronalvarenga.rtts.identity.domain.UserAccount::getId)
            .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "User not found"));
        java.util.UUID representativeId = representativeRepository.findByUserId(userId)
            .map(com.aronalvarenga.rtts.modules.representatives.domain.Representative::getId)
            .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Representative profile not found"));
        // Pass the authenticated representative ID, not frontend-provided one
        return TrainingRequestMapper.toDto(trainingRequestService.requestTraining(representativeId, request));
    }

    @GetMapping("/training-requests")
    public List<TrainingRequestResponseDto> myRequests(@AuthenticationPrincipal Jwt jwt) {
        java.util.UUID userId = userAccountRepository.findByUsername(jwt.getSubject())
            .map(com.aronalvarenga.rtts.identity.domain.UserAccount::getId)
            .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "User not found"));
        java.util.UUID representativeId = representativeRepository.findByUserId(userId)
            .map(com.aronalvarenga.rtts.modules.representatives.domain.Representative::getId)
            .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Representative profile not found"));
        return trainingRequestRepository.findByRepresentativeIdOrderByRequestedAtDesc(representativeId).stream()
            .map(TrainingRequestMapper::toDto)
            .toList();
    }

    @GetMapping("/results")
    public List<RepresentativeResultResponseDto> results(@AuthenticationPrincipal Jwt jwt) {
        java.util.UUID userId = userAccountRepository.findByUsername(jwt.getSubject())
            .map(com.aronalvarenga.rtts.identity.domain.UserAccount::getId)
            .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "User not found"));
        java.util.UUID representativeId = representativeRepository.findByUserId(userId)
            .map(com.aronalvarenga.rtts.modules.representatives.domain.Representative::getId)
            .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Representative profile not found"));
        return enrollmentRepository.findByRepresentativeIdOrderByAssessedAtDesc(representativeId).stream().map(RepresentativeResultMapper::toDto).toList();
    }

    @GetMapping("/dashboard")
    public com.aronalvarenga.rtts.modules.dashboard.web.RepresentativeDashboardResponseDto dashboard(@AuthenticationPrincipal Jwt jwt) {
        java.util.UUID userId = userAccountRepository.findByUsername(jwt.getSubject())
            .map(com.aronalvarenga.rtts.identity.domain.UserAccount::getId)
            .orElseThrow();
        return dashboardService.representativeDashboard(userId);
    }
}