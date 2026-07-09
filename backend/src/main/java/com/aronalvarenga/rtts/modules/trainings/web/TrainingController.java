package com.aronalvarenga.rtts.modules.trainings.web;

import com.aronalvarenga.rtts.modules.trainings.application.TrainingService;
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
@RequestMapping("/api/trainings")
public class TrainingController {

    private final TrainingService trainingService;

    public TrainingController(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    @GetMapping("/available")
    public List<TrainingResponseDto> available() {
        return trainingService.listAvailable().stream().map(TrainingMapper::toDto).toList();
    }

    @GetMapping("/{id}")
    public TrainingResponseDto get(@PathVariable UUID id) {
        return TrainingMapper.toDto(trainingService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','TRAINING_INSTITUTE')")
    public TrainingResponseDto create(@Valid @RequestBody TrainingRequestDto request) {
        return TrainingMapper.toDto(trainingService.create(request));
    }
}
