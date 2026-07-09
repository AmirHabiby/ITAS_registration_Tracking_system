package com.aronalvarenga.rtts.modules.enrollments.web;

import com.aronalvarenga.rtts.modules.enrollments.application.EnrollmentService;
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
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','TRAINING_INSTITUTE')")
    public List<EnrollmentResponseDto> listAll() {
        return enrollmentService.listAll().stream().map(EnrollmentMapper::toDto).toList();
    }

    @PostMapping("/assess")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','TRAINING_INSTITUTE')")
    public EnrollmentResponseDto assess(@Valid @RequestBody AssessmentRequest request) {
        return EnrollmentMapper.toDto(enrollmentService.recordAssessment(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','TRAINING_INSTITUTE')")
    public EnrollmentResponseDto get(@PathVariable UUID id) {
        return EnrollmentMapper.toDto(enrollmentService.get(id));
    }
}
