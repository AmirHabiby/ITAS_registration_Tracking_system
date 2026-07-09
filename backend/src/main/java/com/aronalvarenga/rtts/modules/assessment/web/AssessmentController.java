package com.aronalvarenga.rtts.modules.assessment.web;

import com.aronalvarenga.rtts.modules.assessment.application.AssessmentService;
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
@RequestMapping("/api/institute/assessments")
@PreAuthorize("hasAnyRole('SYSTEM_ADMIN','TRAINING_INSTITUTE')")
public class AssessmentController {

    private final AssessmentService assessmentService;

    public AssessmentController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @PostMapping
    public AssessmentResponseDto submit(@Valid @RequestBody AssessmentRequestDto request, @AuthenticationPrincipal Jwt jwt) {
        return AssessmentMapper.toDto(assessmentService.submit(request, jwt));
    }

    @GetMapping
    public List<AssessmentResponseDto> list() {
        return assessmentService.listAll().stream().map(AssessmentMapper::toDto).toList();
    }

    @GetMapping("/{id}")
    public AssessmentResponseDto get(@PathVariable UUID id) {
        return AssessmentMapper.toDto(assessmentService.get(id));
    }
}