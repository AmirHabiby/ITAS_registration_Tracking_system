package com.aronalvarenga.rtts.modules.representative.application;

import com.aronalvarenga.rtts.modules.representative.domain.RepresentativeTrainingMaterialProgress;
import com.aronalvarenga.rtts.modules.representative.domain.RepresentativeTrainingMaterialProgressRepository;
import com.aronalvarenga.rtts.modules.assessment.domain.AssessmentResultRecordRepository;
import com.aronalvarenga.rtts.modules.assessment.domain.AssessmentStatus;
import com.aronalvarenga.rtts.modules.assessment.domain.OnlineAssessment;
import com.aronalvarenga.rtts.modules.assessment.domain.OnlineAssessmentRepository;
import com.aronalvarenga.rtts.modules.requests.domain.TrainingRequestRepository;
import com.aronalvarenga.rtts.modules.requests.domain.TrainingRequestStatus;
import com.aronalvarenga.rtts.modules.trainings.domain.TrainingMaterial;
import com.aronalvarenga.rtts.modules.trainings.domain.TrainingMaterialRepository;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
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
    private final OnlineAssessmentRepository assessmentRepository;
    private final AssessmentResultRecordRepository assessmentResultRepository;

    public RepresentativeTrainingMaterialService(
        TrainingRequestRepository trainingRequestRepository,
        TrainingMaterialRepository trainingMaterialRepository,
        RepresentativeTrainingMaterialProgressRepository progressRepository,
        OnlineAssessmentRepository assessmentRepository,
        AssessmentResultRecordRepository assessmentResultRepository
    ) {
        this.trainingRequestRepository = trainingRequestRepository;
        this.trainingMaterialRepository = trainingMaterialRepository;
        this.progressRepository = progressRepository;
        this.assessmentRepository = assessmentRepository;
        this.assessmentResultRepository = assessmentResultRepository;
    }

    @Transactional(readOnly = true)
    public List<TrainingMaterial> listForApprovedTraining(UUID representativeId, UUID trainingId) {
        requireApprovedRequest(representativeId, trainingId);
        Set<Integer> unlockedWeeks = buildProgress(representativeId, trainingId).weeks().stream()
            .filter(RepresentativeTrainingProgress.WeekProgress::unlocked)
            .map(RepresentativeTrainingProgress.WeekProgress::weekNumber)
            .collect(Collectors.toSet());
        return trainingMaterialRepository.findByTrainingIdOrderByWeekNumberAscCreatedAtAsc(trainingId).stream()
            .filter(material -> unlockedWeeks.contains(material.getWeekNumber()))
            .toList();
    }

    @Transactional(readOnly = true)
    public RepresentativeTrainingProgress getProgress(UUID representativeId, UUID trainingId) {
        requireApprovedRequest(representativeId, trainingId);
        return buildProgress(representativeId, trainingId);
    }

    @Transactional
    public void requireWeekUnlocked(UUID representativeId, UUID trainingId, int weekNumber) {
        requireApprovedRequest(representativeId, trainingId);
        RepresentativeTrainingProgress.WeekProgress week = buildProgress(representativeId, trainingId).weeks().stream()
            .filter(status -> status.weekNumber() == weekNumber)
            .findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Training week not found"));
        if (!week.unlocked()) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Complete the previous week's materials and pass its quiz before accessing this week");
        }
    }

    @Transactional
    public void requireQuizReady(UUID representativeId, UUID trainingId, int weekNumber) {
        requireWeekUnlocked(representativeId, trainingId, weekNumber);
        RepresentativeTrainingProgress.WeekProgress week = buildProgress(representativeId, trainingId).weeks().stream()
            .filter(status -> status.weekNumber() == weekNumber)
            .findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Training week not found"));
        if (!week.materialsCompleted()) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Complete this week's materials before taking its quiz");
        }
    }

    private RepresentativeTrainingProgress buildProgress(UUID representativeId, UUID trainingId) {
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
        Map<Integer, List<TrainingMaterial>> materialsByWeek = new LinkedHashMap<>();
        materials.stream().sorted(java.util.Comparator.comparingInt(TrainingMaterial::getWeekNumber))
            .forEach(material -> materialsByWeek.computeIfAbsent(material.getWeekNumber(), ignored -> new java.util.ArrayList<>())
                .add(material));
        Map<Integer, OnlineAssessment> quizzesByWeek = new LinkedHashMap<>();
        assessmentRepository.findByTraining_IdAndStatusOrderByCreatedAtDesc(trainingId, AssessmentStatus.PUBLISHED)
            .stream()
            .filter(assessment -> assessment.getWeekNumber() != null)
            .forEach(assessment -> quizzesByWeek.putIfAbsent(assessment.getWeekNumber(), assessment));

        int lastWeek = Math.max(
            materialsByWeek.keySet().stream().mapToInt(Integer::intValue).max().orElse(0),
            quizzesByWeek.keySet().stream().mapToInt(Integer::intValue).max().orElse(0));
        boolean unlocked = true;
        List<RepresentativeTrainingProgress.WeekProgress> weeks = new java.util.ArrayList<>();
        for (int weekNumber = 1; weekNumber <= lastWeek; weekNumber++) {
            List<TrainingMaterial> weekMaterials = materialsByWeek.getOrDefault(weekNumber, List.of());
            boolean materialsCompleted = !weekMaterials.isEmpty() && weekMaterials.stream()
                .allMatch(material -> completedMaterialIdSet.contains(material.getId()));
            OnlineAssessment quiz = quizzesByWeek.get(weekNumber);
            boolean quizPassed = quiz != null
                && assessmentResultRepository.existsPassedFinalResult(representativeId, quiz.getId());
            weeks.add(new RepresentativeTrainingProgress.WeekProgress(
                weekNumber,
                quiz == null ? null : quiz.getId(),
                quiz == null ? null : quiz.getTitle(),
                quiz == null ? null : quiz.getPassingScore(),
                materialsCompleted,
                quizPassed,
                unlocked));
            unlocked = unlocked && materialsCompleted && quizPassed;
        }
        return new RepresentativeTrainingProgress(completedMaterialIds, completedCount, totalCount, percentage, weeks);
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

        requireWeekUnlocked(representativeId, trainingId, material.getWeekNumber());
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
