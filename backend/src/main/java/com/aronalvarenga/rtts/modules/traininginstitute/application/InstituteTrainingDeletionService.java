package com.aronalvarenga.rtts.modules.traininginstitute.application;

import com.aronalvarenga.rtts.media.CloudinaryService;
import com.aronalvarenga.rtts.modules.trainings.application.TrainingService;
import com.aronalvarenga.rtts.modules.trainings.domain.Training;
import com.aronalvarenga.rtts.modules.trainings.domain.TrainingMaterial;
import com.aronalvarenga.rtts.modules.trainings.domain.TrainingMaterialRepository;
import com.aronalvarenga.rtts.modules.trainings.domain.TrainingRepository;
import jakarta.persistence.EntityManager;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class InstituteTrainingDeletionService {

    private final TrainingService trainingService;
    private final TrainingRepository trainingRepository;
    private final TrainingMaterialRepository trainingMaterialRepository;
    private final CloudinaryService cloudinaryService;
    private final EntityManager entityManager;

    public InstituteTrainingDeletionService(
        TrainingService trainingService,
        TrainingRepository trainingRepository,
        TrainingMaterialRepository trainingMaterialRepository,
        CloudinaryService cloudinaryService,
        EntityManager entityManager
    ) {
        this.trainingService = trainingService;
        this.trainingRepository = trainingRepository;
        this.trainingMaterialRepository = trainingMaterialRepository;
        this.cloudinaryService = cloudinaryService;
        this.entityManager = entityManager;
    }

    @Transactional
    public void deleteForInstitute(UUID userId, UUID trainingId) {
        Training training = trainingService.getForInstitute(userId, trainingId);
        List<TrainingMaterial> materials =
            trainingMaterialRepository.findByTrainingIdOrderByWeekNumberAscCreatedAtAsc(trainingId);

        if (materials.stream().anyMatch(material ->
            material.getCloudinaryPublicId() == null || material.getCloudinaryPublicId().isBlank())) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "This training has uploaded materials without Cloudinary identifiers and cannot be safely deleted");
        }

        entityManager.createNativeQuery(
            "select set_config('app.training_deletion', 'true', true)")
            .getSingleResult();
        trainingRepository.delete(training);
        trainingRepository.flush();

        for (TrainingMaterial material : materials) {
            try {
                cloudinaryService.delete(material.getCloudinaryPublicId(), material.getMaterialType());
            } catch (IOException | RuntimeException exception) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Unable to delete all uploaded training materials from storage",
                    exception);
            }
        }
    }
}
