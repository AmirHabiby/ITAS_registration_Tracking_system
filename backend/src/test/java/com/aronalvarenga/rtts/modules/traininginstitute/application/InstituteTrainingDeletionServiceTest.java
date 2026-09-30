package com.aronalvarenga.rtts.modules.traininginstitute.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aronalvarenga.rtts.media.CloudinaryService;
import com.aronalvarenga.rtts.modules.trainings.application.TrainingService;
import com.aronalvarenga.rtts.modules.trainings.domain.Training;
import com.aronalvarenga.rtts.modules.trainings.domain.TrainingMaterial;
import com.aronalvarenga.rtts.modules.trainings.domain.TrainingMaterialRepository;
import com.aronalvarenga.rtts.modules.trainings.domain.TrainingRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class InstituteTrainingDeletionServiceTest {

    @Mock
    private TrainingService trainingService;
    @Mock
    private TrainingRepository trainingRepository;
    @Mock
    private TrainingMaterialRepository trainingMaterialRepository;
    @Mock
    private CloudinaryService cloudinaryService;
    @Mock
    private EntityManager entityManager;
    @Mock
    private Query query;
    @InjectMocks
    private InstituteTrainingDeletionService service;

    @Test
    void deletesTrainingAndItsUploadedMaterials() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID trainingId = UUID.randomUUID();
        Training training = org.mockito.Mockito.mock(Training.class);
        TrainingMaterial material = org.mockito.Mockito.mock(TrainingMaterial.class);
        when(trainingService.getForInstitute(userId, trainingId)).thenReturn(training);
        when(trainingMaterialRepository.findByTrainingIdOrderByWeekNumberAscCreatedAtAsc(trainingId))
            .thenReturn(List.of(material));
        when(material.getCloudinaryPublicId()).thenReturn("training/material");
        when(material.getMaterialType()).thenReturn("video");
        when(entityManager.createNativeQuery("select set_config('app.training_deletion', 'true', true)"))
            .thenReturn(query);

        service.deleteForInstitute(userId, trainingId);

        verify(trainingRepository).delete(training);
        verify(trainingRepository).flush();
        verify(cloudinaryService).delete("training/material", "video");
    }

    @Test
    void refusesDeletionWhenAnUploadedMaterialHasNoCloudinaryIdentifier() {
        UUID userId = UUID.randomUUID();
        UUID trainingId = UUID.randomUUID();
        when(trainingService.getForInstitute(userId, trainingId)).thenReturn(
            org.mockito.Mockito.mock(Training.class));
        when(trainingMaterialRepository.findByTrainingIdOrderByWeekNumberAscCreatedAtAsc(trainingId))
            .thenReturn(List.of(org.mockito.Mockito.mock(TrainingMaterial.class)));

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> service.deleteForInstitute(userId, trainingId));

        assertEquals(409, exception.getStatusCode().value());
        org.mockito.Mockito.verifyNoInteractions(cloudinaryService, trainingRepository);
    }
}
