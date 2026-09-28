package com.aronalvarenga.rtts.modules.publictraining.web;

import com.aronalvarenga.rtts.modules.publictraining.application.PublicTrainingService;
import com.aronalvarenga.rtts.modules.publictraining.domain.PublicTrainingEnrollment;
import com.aronalvarenga.rtts.modules.trainings.web.TrainingMapper;
import com.aronalvarenga.rtts.modules.trainings.web.TrainingResponseDto;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/trainings")
public class PublicTrainingController {

    private static final String TRAINEE_HEADER = "X-Public-Trainee-Id";
    private final PublicTrainingService publicTrainingService;

    public PublicTrainingController(PublicTrainingService publicTrainingService) {
        this.publicTrainingService = publicTrainingService;
    }

    @GetMapping
    public List<TrainingResponseDto> list() {
        return publicTrainingService.listPublicTrainings().stream()
            .map(TrainingMapper::toDto)
            .toList();
    }

    @GetMapping("/summary")
    public PublicTrainingSummaryDto summary(
        @RequestHeader(value = TRAINEE_HEADER, required = false) UUID publicTraineeId
    ) {
        long total = publicTrainingService.listPublicTrainings().size();
        long enrolled = publicTraineeId == null ? 0 : publicTrainingService.countEnrolled(publicTraineeId);
        return new PublicTrainingSummaryDto(total, enrolled, Math.max(0, total - enrolled));
    }

    @PostMapping("/{trainingId}/enroll")
    public PublicEnrollmentResponseDto enroll(
        @PathVariable UUID trainingId,
        @RequestHeader(TRAINEE_HEADER) UUID publicTraineeId
    ) {
        PublicTrainingEnrollment enrollment = publicTrainingService.enroll(publicTraineeId, trainingId);
        return new PublicEnrollmentResponseDto(
            enrollment.getId(),
            enrollment.getTrainingId(),
            enrollment.getEnrolledAt());
    }

    @GetMapping("/enrolled")
    public List<PublicEnrollmentResponseDto> enrolled(
        @RequestHeader(TRAINEE_HEADER) UUID publicTraineeId
    ) {
        return publicTrainingService.listEnrollments(publicTraineeId).stream()
            .map(enrollment -> new PublicEnrollmentResponseDto(
                enrollment.getId(),
                enrollment.getTrainingId(),
                enrollment.getEnrolledAt()))
            .toList();
    }

    @GetMapping("/{trainingId}/materials")
    public List<PublicTrainingMaterialResponseDto> materials(
        @PathVariable UUID trainingId,
        @RequestHeader(TRAINEE_HEADER) UUID publicTraineeId
    ) {
        return publicTrainingService.listMaterials(publicTraineeId, trainingId).stream()
            .map(material -> new PublicTrainingMaterialResponseDto(
                material.getId(),
                material.getTrainingId(),
                material.getTitle(),
                material.getDescription(),
                material.getMaterialType(),
                material.getFileUrl(),
                material.getFileSizeBytes(),
                material.getWeekNumber()))
            .toList();
    }
}
