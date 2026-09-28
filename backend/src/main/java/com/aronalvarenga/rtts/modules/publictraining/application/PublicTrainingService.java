package com.aronalvarenga.rtts.modules.publictraining.application;

import com.aronalvarenga.rtts.modules.publictraining.domain.PublicTrainingEnrollment;
import com.aronalvarenga.rtts.modules.publictraining.domain.PublicTrainingEnrollmentRepository;
import com.aronalvarenga.rtts.modules.trainings.application.TrainingService;
import com.aronalvarenga.rtts.modules.trainings.domain.Training;
import com.aronalvarenga.rtts.modules.trainings.domain.TrainingAccessType;
import com.aronalvarenga.rtts.modules.trainings.domain.TrainingRepository;
import com.aronalvarenga.rtts.modules.trainings.domain.TrainingStatus;
import com.aronalvarenga.rtts.modules.trainings.domain.TrainingMaterial;
import com.aronalvarenga.rtts.modules.trainings.domain.TrainingMaterialRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PublicTrainingService {

    private final TrainingService trainingService;
    private final TrainingRepository trainingRepository;
    private final PublicTrainingEnrollmentRepository enrollmentRepository;
    private final TrainingMaterialRepository trainingMaterialRepository;

    public PublicTrainingService(
        TrainingService trainingService,
        TrainingRepository trainingRepository,
        PublicTrainingEnrollmentRepository enrollmentRepository,
        TrainingMaterialRepository trainingMaterialRepository
    ) {
        this.trainingService = trainingService;
        this.trainingRepository = trainingRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.trainingMaterialRepository = trainingMaterialRepository;
    }

    @Transactional(readOnly = true)
    public List<Training> listPublicTrainings() {
        return trainingService.listPublic();
    }

    @Transactional(readOnly = true)
    public long countEnrolled(UUID publicTraineeId) {
        return enrollmentRepository.countByPublicTraineeId(publicTraineeId);
    }

    @Transactional(readOnly = true)
    public List<PublicTrainingEnrollment> listEnrollments(UUID publicTraineeId) {
        return enrollmentRepository.findByPublicTraineeId(publicTraineeId);
    }

    @Transactional(readOnly = true)
    public List<TrainingMaterial> listMaterials(UUID publicTraineeId, UUID trainingId) {
        if (!enrollmentRepository.existsByPublicTraineeIdAndTrainingId(publicTraineeId, trainingId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You must be enrolled in this training");
        }
        return trainingMaterialRepository.findByTrainingIdOrderByWeekNumberAscCreatedAtAsc(trainingId);
    }

    @Transactional
    public PublicTrainingEnrollment enroll(UUID publicTraineeId, UUID trainingId) {
        Training training = trainingRepository.findById(trainingId)
            .filter(candidate -> candidate.isActive()
                && candidate.getAccessType() == TrainingAccessType.PUBLIC
                && candidate.getStatus() == TrainingStatus.PUBLISHED)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Public training not found"));

        if (enrollmentRepository.existsByPublicTraineeIdAndTrainingId(publicTraineeId, trainingId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already enrolled in this training");
        }
        if (enrollmentRepository.countByTrainingId(trainingId) >= training.getCapacity()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Training capacity is full");
        }
        return enrollmentRepository.save(new PublicTrainingEnrollment(publicTraineeId, trainingId));
    }
}
