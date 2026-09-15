package com.aronalvarenga.rtts.modules.stafftraining.web;

import com.aronalvarenga.rtts.modules.stafftraining.application.StaffTrainingService;
import com.aronalvarenga.rtts.modules.trainings.web.TrainingMapper;
import com.aronalvarenga.rtts.modules.trainings.web.TrainingResponseDto;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/staff/trainings")
public class StaffTrainingController {

    private final StaffTrainingService staffTrainingService;

    public StaffTrainingController(StaffTrainingService staffTrainingService) {
        this.staffTrainingService = staffTrainingService;
    }

    @GetMapping
    public List<TrainingResponseDto> list() {
        return staffTrainingService.listStaffTrainings().stream()
            .map(TrainingMapper::toDto)
            .toList();
    }

    @PostMapping("/access")
    public TrainingResponseDto access(@Valid @RequestBody StaffTrainingAccessRequest request) {
        return TrainingMapper.toDto(staffTrainingService.access(request));
    }
}
