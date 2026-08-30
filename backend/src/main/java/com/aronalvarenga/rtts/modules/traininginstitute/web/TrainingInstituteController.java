package com.aronalvarenga.rtts.modules.traininginstitute.web;

import com.aronalvarenga.rtts.identity.domain.UserAccountRepository;
import com.aronalvarenga.rtts.modules.enrollments.application.EnrollmentService;
import com.aronalvarenga.rtts.modules.enrollments.web.EnrollmentMapper;
import com.aronalvarenga.rtts.modules.enrollments.web.EnrollmentResponseDto;
import com.aronalvarenga.rtts.modules.trainings.application.TrainingService;
import com.aronalvarenga.rtts.modules.trainings.web.TrainingMapper;
import com.aronalvarenga.rtts.modules.trainings.web.TrainingRequestDto;
import com.aronalvarenga.rtts.modules.trainings.web.TrainingResponseDto;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/institutes/me")
@PreAuthorize("hasAnyRole('SYSTEM_ADMIN','TRAINING_INSTITUTE')")
public class TrainingInstituteController {

    private final TrainingService trainingService;
    private final EnrollmentService enrollmentService;
    private final UserAccountRepository userAccountRepository;

    public TrainingInstituteController(TrainingService trainingService, EnrollmentService enrollmentService, UserAccountRepository userAccountRepository) {
        this.trainingService = trainingService;
        this.enrollmentService = enrollmentService;
        this.userAccountRepository = userAccountRepository;
    }

    @PostMapping("/trainings")
    public TrainingResponseDto createTraining(@Valid @RequestBody TrainingRequestDto request, @AuthenticationPrincipal Jwt jwt) {
        UUID userId = userAccountRepository.findByUsername(jwt.getSubject())
            .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "User not found"))
            .getId();
        return TrainingMapper.toDto(trainingService.createForInstitute(userId, request));
    }

    @GetMapping("/trainings")
    public List<TrainingResponseDto> listOwnTrainings(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = userAccountRepository.findByUsername(jwt.getSubject())
            .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "User not found"))
            .getId();
        return trainingService.listForInstitute(userId).stream().map(TrainingMapper::toDto).toList();
    }

    @PutMapping("/trainings/{id}")
    public TrainingResponseDto updateTraining(@PathVariable UUID id, @Valid @RequestBody TrainingRequestDto request, @AuthenticationPrincipal Jwt jwt) {
        UUID userId = userAccountRepository.findByUsername(jwt.getSubject())
            .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "User not found"))
            .getId();
        return TrainingMapper.toDto(trainingService.getForInstitute(userId, id));
    }

    @DeleteMapping("/trainings/{id}")
    public void deleteTraining(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
    }

    @GetMapping("/enrollments")
    public List<EnrollmentResponseDto> listEnrollments(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = userAccountRepository.findByUsername(jwt.getSubject())
            .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "User not found"))
            .getId();
        return enrollmentService.listForInstitute(userId).stream().map(EnrollmentMapper::toDto).toList();
    }

}