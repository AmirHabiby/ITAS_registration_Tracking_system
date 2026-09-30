package com.aronalvarenga.rtts.modules.representative.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aronalvarenga.rtts.modules.requests.domain.TrainingRequestRepository;
import com.aronalvarenga.rtts.modules.requests.domain.TrainingRequestStatus;
import com.aronalvarenga.rtts.modules.representative.domain.RepresentativeTrainingMaterialProgress;
import com.aronalvarenga.rtts.modules.representative.domain.RepresentativeTrainingMaterialProgressRepository;
import com.aronalvarenga.rtts.modules.trainings.domain.TrainingMaterial;
import com.aronalvarenga.rtts.modules.trainings.domain.TrainingMaterialRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class RepresentativeTrainingMaterialServiceTest {

    @Mock
    private TrainingRequestRepository trainingRequestRepository;

    @Mock
    private TrainingMaterialRepository trainingMaterialRepository;

    @Mock
    private RepresentativeTrainingMaterialProgressRepository progressRepository;

    @InjectMocks
    private RepresentativeTrainingMaterialService service;

    @Test
    void listsMaterialsWhenRepresentativeHasApprovedTrainingRequest() {
        UUID representativeId = UUID.randomUUID();
        UUID trainingId = UUID.randomUUID();
        List<TrainingMaterial> materials = List.of();
        when(trainingRequestRepository.existsByRepresentativeIdAndTrainingIdAndStatus(
            representativeId,
            trainingId,
            TrainingRequestStatus.APPROVED)).thenReturn(true);
        when(trainingMaterialRepository.findByTrainingIdOrderByWeekNumberAscCreatedAtAsc(trainingId))
            .thenReturn(materials);

        assertEquals(materials, service.listForApprovedTraining(representativeId, trainingId));
    }

    @Test
    void rejectsMaterialsWhenTrainingRequestIsNotApproved() {
        UUID representativeId = UUID.randomUUID();
        UUID trainingId = UUID.randomUUID();
        when(trainingRequestRepository.existsByRepresentativeIdAndTrainingIdAndStatus(
            representativeId,
            trainingId,
            TrainingRequestStatus.APPROVED)).thenReturn(false);

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> service.listForApprovedTraining(representativeId, trainingId));

        assertEquals(403, exception.getStatusCode().value());
        verify(trainingMaterialRepository, org.mockito.Mockito.never())
            .findByTrainingIdOrderByWeekNumberAscCreatedAtAsc(trainingId);
    }

    @Test
    void calculatesProgressFromCompletedMaterials() {
        UUID representativeId = UUID.randomUUID();
        UUID trainingId = UUID.randomUUID();
        UUID completedMaterialId = UUID.randomUUID();
        TrainingMaterial completedMaterial = org.mockito.Mockito.mock(TrainingMaterial.class);
        TrainingMaterial remainingMaterial = org.mockito.Mockito.mock(TrainingMaterial.class);
        RepresentativeTrainingMaterialProgress completion =
            org.mockito.Mockito.mock(RepresentativeTrainingMaterialProgress.class);

        when(trainingRequestRepository.existsByRepresentativeIdAndTrainingIdAndStatus(
            representativeId,
            trainingId,
            TrainingRequestStatus.APPROVED)).thenReturn(true);
        when(trainingMaterialRepository.findByTrainingIdOrderByWeekNumberAscCreatedAtAsc(trainingId))
            .thenReturn(List.of(completedMaterial, remainingMaterial));
        when(completedMaterial.getId()).thenReturn(completedMaterialId);
        when(completion.getMaterialId()).thenReturn(completedMaterialId);
        when(progressRepository.findByRepresentativeIdAndTrainingId(representativeId, trainingId))
            .thenReturn(List.of(completion));

        RepresentativeTrainingProgress progress = service.getProgress(representativeId, trainingId);

        assertEquals(List.of(completedMaterialId), progress.completedMaterialIds());
        assertEquals(1, progress.completedCount());
        assertEquals(2, progress.totalCount());
        assertEquals(50, progress.percentage());
    }

    @Test
    void completionRequiresAnApprovedTrainingRequest() {
        UUID representativeId = UUID.randomUUID();
        UUID trainingId = UUID.randomUUID();
        when(trainingRequestRepository.existsByRepresentativeIdAndTrainingIdAndStatus(
            representativeId,
            trainingId,
            TrainingRequestStatus.APPROVED)).thenReturn(false);

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> service.completeMaterial(representativeId, trainingId, UUID.randomUUID()));

        assertEquals(403, exception.getStatusCode().value());
        org.mockito.Mockito.verifyNoInteractions(progressRepository);
    }

    @Test
    void completesMaterialAndReturnsUpdatedProgress() {
        UUID representativeId = UUID.randomUUID();
        UUID trainingId = UUID.randomUUID();
        UUID materialId = UUID.randomUUID();
        TrainingMaterial material = org.mockito.Mockito.mock(TrainingMaterial.class);
        when(trainingRequestRepository.existsByRepresentativeIdAndTrainingIdAndStatus(
            representativeId,
            trainingId,
            TrainingRequestStatus.APPROVED)).thenReturn(true);
        when(trainingMaterialRepository.findById(materialId)).thenReturn(java.util.Optional.of(material));
        when(material.getTrainingId()).thenReturn(trainingId);
        when(material.getId()).thenReturn(materialId);
        when(progressRepository.existsByRepresentativeIdAndMaterialId(representativeId, materialId))
            .thenReturn(false);
        when(trainingMaterialRepository.findByTrainingIdOrderByWeekNumberAscCreatedAtAsc(trainingId))
            .thenReturn(List.of(material));
        when(progressRepository.findByRepresentativeIdAndTrainingId(representativeId, trainingId))
            .thenReturn(List.of(new RepresentativeTrainingMaterialProgress(
                representativeId,
                trainingId,
                materialId)));

        RepresentativeTrainingProgress progress =
            service.completeMaterial(representativeId, trainingId, materialId);

        verify(progressRepository).save(any(RepresentativeTrainingMaterialProgress.class));
        assertEquals(List.of(materialId), progress.completedMaterialIds());
        assertEquals(100, progress.percentage());
    }
}
