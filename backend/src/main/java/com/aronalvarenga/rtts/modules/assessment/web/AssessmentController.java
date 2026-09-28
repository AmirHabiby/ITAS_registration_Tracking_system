package com.aronalvarenga.rtts.modules.assessment.web;

import com.aronalvarenga.rtts.modules.assessment.application.AssessmentService;
import com.aronalvarenga.rtts.modules.enrollments.domain.EnrollmentRepository;
import com.aronalvarenga.rtts.modules.representatives.domain.RepresentativeRepository;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assessments")
@PreAuthorize("hasAnyRole('SYSTEM_ADMIN','TRAINING_INSTITUTE')")
public class AssessmentController {

    private final AssessmentService assessmentService;
    private final com.aronalvarenga.rtts.identity.domain.UserAccountRepository userAccountRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final RepresentativeRepository representativeRepository;

    public AssessmentController(
        AssessmentService assessmentService,
        com.aronalvarenga.rtts.identity.domain.UserAccountRepository userAccountRepository,
        EnrollmentRepository enrollmentRepository,
        RepresentativeRepository representativeRepository
    ) {
        this.assessmentService = assessmentService;
        this.userAccountRepository = userAccountRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.representativeRepository = representativeRepository;
    }

    @PostMapping
    public AssessmentResponseDto submit(@Valid @RequestBody AssessmentRequestDto request, @AuthenticationPrincipal Jwt jwt) {
        return AssessmentMapper.toDto(assessmentService.submit(request, jwt));
    }

    @GetMapping
    public List<AssessmentResponseDto> list(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = userAccountRepository.findByUsername(jwt.getSubject())
            .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "User not found"))
            .getId();
        return assessmentService.listForInstitute(userId).stream().map(this::toDto).toList();
    }

    @GetMapping("/{id}")
    public AssessmentResponseDto get(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        UUID userId = userAccountRepository.findByUsername(jwt.getSubject())
            .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "User not found"))
            .getId();
        return toDto(assessmentService.getForInstitute(userId, id));
    }

    private AssessmentResponseDto toDto(com.aronalvarenga.rtts.modules.assessment.domain.AssessmentResult assessment) {
        String representativeName = enrollmentRepository.findById(assessment.getTrainingEnrollmentId())
            .flatMap(enrollment -> representativeRepository.findById(enrollment.getRepresentativeId()))
            .map(representative -> representative.getFullName())
            .orElse("Unknown representative");
        return AssessmentMapper.toDto(assessment, representativeName);
    }
}