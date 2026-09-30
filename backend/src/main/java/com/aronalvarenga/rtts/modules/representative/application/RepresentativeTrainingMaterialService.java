package com.aronalvarenga.rtts.modules.representative.application;

import com.aronalvarenga.rtts.modules.representative.domain.RepresentativeTrainingMaterialProgress;
import com.aronalvarenga.rtts.modules.representative.domain.RepresentativeTrainingMaterialProgressRepository;
import com.aronalvarenga.rtts.modules.requests.domain.TrainingRequestRepository;
import com.aronalvarenga.rtts.modules.requests.domain.TrainingRequestStatus;
import com.aronalvarenga.rtts.modules.trainings.domain.TrainingMaterial;
import com.aronalvarenga.rtts.modules.trainings.domain.TrainingMaterialRepository;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RepresentativeTrainingMaterialService {

    private final TrainingRequestRepository trainingRequestRepository;
    private final TrainingMaterialRepository trainingMaterialRepository;
    private final RepresentativeTrainingMaterialProgressRepository progressRepository;

    public RepresentativeTrainingMaterialService(
        TrainingRequestRepository trainingRequestRepository,
        TrainingMaterialRepository trainingMaterialRepository,
        RepresentativeTrainingMaterialProgressRepository progressRepository
    ) {
        this.trainingRequestRepository = trainingRequestRepository;
        this.trainingMaterialRepository = trainingMaterialRepository;
        this.progressRepository = progressRepository;
    }

    @Transactional(readOnly = true)
    public List<TrainingMaterial> listForApprovedTraining(UUID representativeId, UUID trainingId) {
        requireApprovedRequest(representativeId, trainingId);
        return trainingMaterialRepository.findByTrainingIdOrderByWeekNumberAscCreatedAtAsc(trainingId);
    }

    @Transactional(readOnly = true)
    public RepresentativeTrainingProgress getProgress(UUID representativeId, UUID trainingId) {
        requireApprovedRequest(representativeId, trainingId);
        List<TrainingMaterial> materials =
            trainingMaterialRepository.findByTrainingIdOrderByWeekNumberAscCreatedAtAsc(trainingId);
        List<UUID> completedMaterialIds = progressRepository
            .findByRepresentativeIdAndTrainingId(representativeId, trainingId)
            .stream()
            .map(RepresentativeTrainingMaterialProgress::getMaterialId)
            .toList();
        Set<UUID> completedMaterialIdSet = completedMaterialIds.stream().collect(Collectors.toSet());
        int totalCount = materials.size();
        int completedCount = (int) materials.stream()
            .filter(material -> completedMaterialIdSet.contains(material.getId()))
            .count();
        int percentage = totalCount == 0 ? 0 : Math.round(completedCount * 100f / totalCount);
        return new RepresentativeTrainingProgress(completedMaterialIds, completedCount, totalCount, percentage);
    }

    @Transactional
    public RepresentativeTrainingProgress completeMaterial(UUID representativeId, UUID trainingId, UUID materialId) {
        requireApprovedRequest(representativeId, trainingId);
        TrainingMaterial material = trainingMaterialRepository.findById(materialId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Training material not found"));
        if (!trainingId.equals(material.getTrainingId())) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Training material not found for this training");
        }

        if (!progressRepository.existsByRepresentativeIdAndMaterialId(representativeId, materialId)) {
            progressRepository.save(new RepresentativeTrainingMaterialProgress(
                representativeId,
                trainingId,
                materialId));
        }
        return getProgress(representativeId, trainingId);
    }

    private void requireApprovedRequest(UUID representativeId, UUID trainingId) {
        boolean approved = trainingRequestRepository.existsByRepresentativeIdAndTrainingIdAndStatus(
            representativeId,
            trainingId,
            TrainingRequestStatus.APPROVED);
        if (!approved) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You must have an approved request for this training to access its materials");
        }
    }
}
