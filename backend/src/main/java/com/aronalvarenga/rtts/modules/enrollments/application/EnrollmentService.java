package com.aronalvarenga.rtts.modules.enrollments.application;

import com.aronalvarenga.rtts.modules.enrollments.domain.Enrollment;
import com.aronalvarenga.rtts.modules.enrollments.domain.EnrollmentRepository;
import com.aronalvarenga.rtts.modules.enrollments.domain.EnrollmentStatus;
import com.aronalvarenga.rtts.modules.enrollments.web.AssessmentRequest;
import com.aronalvarenga.rtts.modules.representatives.application.RepresentativeService;
import com.aronalvarenga.rtts.modules.institutes.domain.TrainingInstituteRepository;
import com.aronalvarenga.rtts.modules.trainings.domain.TrainingRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final RepresentativeService representativeService;
    private final TrainingRepository trainingRepository;
    private final TrainingInstituteRepository trainingInstituteRepository;

    public EnrollmentService(
        EnrollmentRepository enrollmentRepository,
        RepresentativeService representativeService,
        TrainingRepository trainingRepository,
        TrainingInstituteRepository trainingInstituteRepository
    ) {
        this.enrollmentRepository = enrollmentRepository;
        this.representativeService = representativeService;
        this.trainingRepository = trainingRepository;
        this.trainingInstituteRepository = trainingInstituteRepository;
    }

    @Transactional(readOnly = true)
    public List<Enrollment> listAll() {
        return enrollmentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Enrollment> listForInstitute(UUID userId) {
        UUID trainingInstituteProfileId = trainingInstituteRepository.findByUserId(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Training institute not found"))
            .getId();
        List<UUID> trainingIds = trainingRepository.findByTrainingInstituteProfileIdOrderByStartDateAsc(trainingInstituteProfileId)
            .stream()
            .map(Training -> Training.getId())
            .toList();
        return enrollmentRepository.findByTrainingIdInOrderByAssessedAtDesc(trainingIds);
    }

    @Transactional(readOnly = true)
    public List<Enrollment> listForRepresentative(UUID representativeId) {
        return enrollmentRepository.findByRepresentativeIdOrderByAssessedAtDesc(representativeId);
    }

    @Transactional
    public Enrollment recordAssessment(AssessmentRequest request) {
        Enrollment enrollment = enrollmentRepository.findByRepresentativeIdAndTrainingId(request.representativeId(), request.trainingId())
            .orElseGet(() -> new Enrollment(request.representativeId(), request.trainingId()));

        enrollment.setAssessmentScore(request.score());
        enrollment.setAssessmentNote(request.note());
        enrollment.setAssessedAt(Instant.now());
        enrollment.setPassed(isPassingScore(request.score()));
        enrollment.setStatus(Boolean.TRUE.equals(enrollment.getPassed()) ? EnrollmentStatus.COMPLETED : EnrollmentStatus.RETAKE_REQUIRED);

        Enrollment saved = enrollmentRepository.save(enrollment);
        if (Boolean.TRUE.equals(saved.getPassed())) {
            representativeService.markTrained(request.representativeId());
        }
        return saved;
    }

    @Transactional(readOnly = true)
    public Enrollment get(UUID enrollmentId) {
        return enrollmentRepository.findById(enrollmentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Enrollment not found"));
    }

    @Transactional(readOnly = true)
    public Enrollment getForInstitute(UUID userId, UUID enrollmentId) {
        Enrollment enrollment = get(enrollmentId);
        UUID trainingInstituteProfileId = trainingInstituteRepository.findByUserId(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Training institute not found"))
            .getId();
        UUID trainingId = enrollment.getTrainingId();
        boolean ownsTraining = trainingRepository.findById(trainingId)
            .map(training -> trainingInstituteProfileId.equals(training.getTrainingInstituteProfileId()))
            .orElse(false);
        if (!ownsTraining) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only access enrollments for your own trainings");
        }
        return enrollment;
    }

    private boolean isPassingScore(BigDecimal score) {
        return score.compareTo(new BigDecimal("70")) >= 0;
    }
}
