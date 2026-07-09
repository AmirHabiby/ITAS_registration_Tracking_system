package com.aronalvarenga.rtts.modules.assessment.application;

import com.aronalvarenga.rtts.identity.domain.UserAccountRepository;
import com.aronalvarenga.rtts.modules.assessment.domain.AssessmentResult;
import com.aronalvarenga.rtts.modules.assessment.domain.AssessmentResultRepository;
import com.aronalvarenga.rtts.modules.assessment.web.AssessmentRequestDto;
import com.aronalvarenga.rtts.modules.delegator.domain.DelegatorProfileRepository;
import com.aronalvarenga.rtts.modules.enrollments.domain.Enrollment;
import com.aronalvarenga.rtts.modules.enrollments.domain.EnrollmentRepository;
import com.aronalvarenga.rtts.modules.enrollments.domain.EnrollmentStatus;
import com.aronalvarenga.rtts.modules.institutes.domain.TrainingInstituteRepository;
import com.aronalvarenga.rtts.modules.representatives.application.RepresentativeService;
import com.aronalvarenga.rtts.modules.representatives.domain.RepresentativeRepository;
import com.aronalvarenga.rtts.modules.representatives.domain.RepresentativeStatus;
import com.aronalvarenga.rtts.modules.trainings.domain.Training;
import com.aronalvarenga.rtts.modules.trainings.domain.TrainingRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AssessmentService {

    private final AssessmentResultRepository assessmentResultRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final TrainingRepository trainingRepository;
    private final RepresentativeRepository representativeRepository;
    private final UserAccountRepository userAccountRepository;
    private final RepresentativeService representativeService;
    private final TrainingInstituteRepository trainingInstituteRepository;

    public AssessmentService(
        AssessmentResultRepository assessmentResultRepository,
        EnrollmentRepository enrollmentRepository,
        TrainingRepository trainingRepository,
        RepresentativeRepository representativeRepository,
        UserAccountRepository userAccountRepository,
        RepresentativeService representativeService,
        TrainingInstituteRepository trainingInstituteRepository
    ) {
        this.assessmentResultRepository = assessmentResultRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.trainingRepository = trainingRepository;
        this.representativeRepository = representativeRepository;
        this.userAccountRepository = userAccountRepository;
        this.representativeService = representativeService;
        this.trainingInstituteRepository = trainingInstituteRepository;
    }

    @Transactional(readOnly = true)
    public List<AssessmentResult> listForInstitute(UUID userId) {
        UUID instituteId = trainingInstituteRepository.findByUserId(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Training institute not found"))
            .getId();
        List<UUID> trainingIds = trainingRepository.findByTrainingInstituteProfileIdOrderByStartDateAsc(instituteId)
            .stream()
            .map(training -> training.getId())
            .toList();
        List<UUID> enrollmentIds = enrollmentRepository.findByTrainingIdInOrderByAssessedAtDesc(trainingIds)
            .stream()
            .map(Enrollment::getId)
            .toList();
        return assessmentResultRepository.findByTrainingEnrollmentIdInOrderByAssessmentDateDesc(enrollmentIds);
    }

    @Transactional(readOnly = true)
    public AssessmentResult getForInstitute(UUID userId, UUID assessmentId) {
        AssessmentResult assessmentResult = get(assessmentId);
        UUID instituteId = trainingInstituteRepository.findByUserId(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Training institute not found"))
            .getId();
        boolean ownsAssessment = enrollmentRepository.findById(assessmentResult.getTrainingEnrollmentId())
            .flatMap(enrollment -> trainingRepository.findById(enrollment.getTrainingId()))
            .map(training -> instituteId.equals(training.getTrainingInstituteProfileId()))
            .orElse(false);
        if (!ownsAssessment) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only access assessments for your own trainings");
        }
        return assessmentResult;
    }

    @Transactional
    public AssessmentResult submit(AssessmentRequestDto request, Jwt jwt) {
        Enrollment enrollment = enrollmentRepository.findById(request.enrollmentId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Enrollment not found"));
        Training training = trainingRepository.findById(enrollment.getTrainingId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Training not found"));

        UUID instituteId = trainingInstituteRepository.findByUserId(userAccountRepository.findByUsername(jwt.getSubject())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"))
            .getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Training institute not found"))
            .getId();
        if (!instituteId.equals(training.getTrainingInstituteProfileId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only submit assessments for your own trainings");
        }

        UUID submittedByUserId = userAccountRepository.findByUsername(jwt.getSubject())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"))
            .getId();

        boolean passed = request.score().compareTo(training.getPassingScore()) >= 0;
        int failedAttempts = assessmentResultRepository.findByTrainingEnrollmentIdAndPassedFalseOrderByAssessmentDateDesc(enrollment.getId()).size();
        AssessmentResult assessmentResult = new AssessmentResult(enrollment.getId(), submittedByUserId, request.score(), passed, request.remarks());
        assessmentResult.setAssessmentDate(request.assessmentDate());
        AssessmentResult saved = assessmentResultRepository.save(assessmentResult);

        if (passed) {
            enrollment.setStatus(EnrollmentStatus.COMPLETED);
            enrollment.setAssessmentScore(request.score());
            enrollment.setPassed(true);
            enrollment.setAssessmentNote(request.remarks());
            enrollment.setAssessedAt(request.assessmentDate());
            enrollmentRepository.save(enrollment);
            representativeService.markTrained(enrollment.getRepresentativeId());
        } else {
            enrollment.setAssessmentScore(request.score());
            enrollment.setPassed(false);
            enrollment.setAssessmentNote(request.remarks());
            enrollment.setAssessedAt(request.assessmentDate());
            int allowedRetakes = training.getAllowedRetakeAttempts();
            enrollment.setStatus(failedAttempts < allowedRetakes ? EnrollmentStatus.RETAKE_REQUIRED : EnrollmentStatus.FAILED);
            enrollmentRepository.save(enrollment);
        }

        return saved;
    }

    @Transactional(readOnly = true)
    public List<AssessmentResult> listAll() {
        return assessmentResultRepository.findAll();
    }

    @Transactional(readOnly = true)
    public AssessmentResult get(UUID id) {
        return assessmentResultRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assessment not found"));
    }
}