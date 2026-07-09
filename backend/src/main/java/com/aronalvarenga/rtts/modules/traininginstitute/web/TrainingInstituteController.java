package com.aronalvarenga.rtts.modules.traininginstitute.web;

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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/institute")
@PreAuthorize("hasAnyRole('SYSTEM_ADMIN','TRAINING_INSTITUTE')")
public class TrainingInstituteController {

    private final TrainingService trainingService;
    private final EnrollmentService enrollmentService;

    public TrainingInstituteController(TrainingService trainingService, EnrollmentService enrollmentService) {
        this.trainingService = trainingService;
        this.enrollmentService = enrollmentService;
    }

    @PostMapping("/trainings")
    public TrainingResponseDto createTraining(@Valid @RequestBody TrainingRequestDto request) {
        return TrainingMapper.toDto(trainingService.create(request));
    }

    @GetMapping("/trainings")
    public List<TrainingResponseDto> listOwnTrainings() {
        return trainingService.listAvailable().stream().map(TrainingMapper::toDto).toList();
    }

    @PutMapping("/trainings/{id}")
    public TrainingResponseDto updateTraining(@PathVariable UUID id, @Valid @RequestBody TrainingRequestDto request) {
        return TrainingMapper.toDto(trainingService.get(id));
    }

    @DeleteMapping("/trainings/{id}")
    public void deleteTraining(@PathVariable UUID id) {
    }

    @GetMapping("/enrollments")
    public List<EnrollmentResponseDto> listEnrollments() {
        return enrollmentService.listAll().stream().map(EnrollmentMapper::toDto).toList();
    }

}