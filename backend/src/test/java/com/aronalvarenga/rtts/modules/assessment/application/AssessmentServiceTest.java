package com.aronalvarenga.rtts.modules.assessment.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.aronalvarenga.rtts.identity.domain.UserAccount;
import com.aronalvarenga.rtts.identity.domain.UserAccountRepository;
import com.aronalvarenga.rtts.identity.domain.UserRole;
import com.aronalvarenga.rtts.modules.assessment.domain.AssessmentResult;
import com.aronalvarenga.rtts.modules.assessment.domain.AssessmentResultRepository;
import com.aronalvarenga.rtts.modules.assessment.domain.AssessmentResultStatus;
import com.aronalvarenga.rtts.modules.assessment.web.AssessmentRequestDto;
import com.aronalvarenga.rtts.modules.enrollments.domain.Enrollment;
import com.aronalvarenga.rtts.modules.enrollments.domain.EnrollmentRepository;
import com.aronalvarenga.rtts.modules.enrollments.domain.EnrollmentStatus;
import com.aronalvarenga.rtts.modules.institutes.domain.TrainingInstitute;
import com.aronalvarenga.rtts.modules.institutes.domain.TrainingInstituteRepository;
import com.aronalvarenga.rtts.modules.representatives.application.RepresentativeService;
import com.aronalvarenga.rtts.modules.trainings.domain.Training;
import com.aronalvarenga.rtts.modules.trainings.domain.TrainingRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

class AssessmentServiceTest {

    @Test
    void submit_passedScore_marksEnrollmentCompleted() {
        AssessmentResultRepository assessmentResultRepository = mock(AssessmentResultRepository.class);
        EnrollmentRepository enrollmentRepository = mock(EnrollmentRepository.class);
        TrainingRepository trainingRepository = mock(TrainingRepository.class);
        UserAccountRepository userAccountRepository = mock(UserAccountRepository.class);
        RepresentativeService representativeService = mock(RepresentativeService.class);
        TrainingInstituteRepository trainingInstituteRepository = mock(TrainingInstituteRepository.class);

        UUID instituteUserId = UUID.randomUUID();
        UUID instituteId = UUID.randomUUID();
        UUID trainingId = UUID.randomUUID();
        UUID enrollmentId = UUID.randomUUID();
        UUID representativeId = UUID.randomUUID();

        TrainingInstitute institute = new TrainingInstitute("Test Institute", "test@institute.com");
        institute.setId(instituteId);
        institute.setUserId(instituteUserId);

        Training training = new Training(instituteId, "Test Training", "Description", LocalDate.now(), LocalDate.now().plusDays(5), 10);
        training.setId(trainingId);
        training.setPassingScore(new BigDecimal("70.00"));

        Enrollment enrollment = new Enrollment(representativeId, trainingId);
        enrollment.setId(enrollmentId);
        enrollment.setStatus(EnrollmentStatus.ONGOING);

        UserAccount instituteUser = new UserAccount("institute", "hashed", UserRole.TRAINING_INSTITUTE, "Institute");
        instituteUser.setId(instituteUserId);

        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn("institute");
        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(enrollment));
        when(trainingRepository.findById(trainingId)).thenReturn(Optional.of(training));
        when(userAccountRepository.findByUsername("institute")).thenReturn(Optional.of(instituteUser));
        when(trainingInstituteRepository.findByUserId(instituteUserId)).thenReturn(Optional.of(institute));
        when(assessmentResultRepository.findByTrainingEnrollmentIdAndPassedFalseOrderByAssessmentDateDesc(enrollmentId)).thenReturn(java.util.List.of());
        when(assessmentResultRepository.save(any(AssessmentResult.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AssessmentService service = new AssessmentService(
            assessmentResultRepository,
            enrollmentRepository,
            trainingRepository,
            null, // representativeRepository
            userAccountRepository,
            representativeService,
            trainingInstituteRepository
        );

        AssessmentRequestDto request = new AssessmentRequestDto(
            enrollmentId,
            new BigDecimal("85.00"),
            "Great performance",
            Instant.now()
        );

        AssessmentResult result = service.submit(request, jwt);

        assertEquals(AssessmentResultStatus.PASSED, result.getResultStatus());
        assertEquals(EnrollmentStatus.COMPLETED, enrollment.getStatus());
    }

    @Test
    void submit_failedScoreWithRetakes_marksRetakeRequired() {
        AssessmentResultRepository assessmentResultRepository = mock(AssessmentResultRepository.class);
        EnrollmentRepository enrollmentRepository = mock(EnrollmentRepository.class);
        TrainingRepository trainingRepository = mock(TrainingRepository.class);
        UserAccountRepository userAccountRepository = mock(UserAccountRepository.class);
        RepresentativeService representativeService = mock(RepresentativeService.class);
        TrainingInstituteRepository trainingInstituteRepository = mock(TrainingInstituteRepository.class);

        UUID instituteUserId = UUID.randomUUID();
        UUID instituteId = UUID.randomUUID();
        UUID trainingId = UUID.randomUUID();
        UUID enrollmentId = UUID.randomUUID();
        UUID representativeId = UUID.randomUUID();

        TrainingInstitute institute = new TrainingInstitute("Test Institute", "test@institute.com");
        institute.setId(instituteId);
        institute.setUserId(instituteUserId);

        Training training = new Training(instituteId, "Test Training", "Description", LocalDate.now(), LocalDate.now().plusDays(5), 10);
        training.setId(trainingId);
        training.setPassingScore(new BigDecimal("70.00"));
        training.setAllowedRetakeAttempts(2);

        Enrollment enrollment = new Enrollment(representativeId, trainingId);
        enrollment.setId(enrollmentId);
        enrollment.setStatus(EnrollmentStatus.ONGOING);

        UserAccount instituteUser = new UserAccount("institute", "hashed", UserRole.TRAINING_INSTITUTE, "Institute");
        instituteUser.setId(instituteUserId);

        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn("institute");
        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(enrollment));
        when(trainingRepository.findById(trainingId)).thenReturn(Optional.of(training));
        when(userAccountRepository.findByUsername("institute")).thenReturn(Optional.of(instituteUser));
        when(trainingInstituteRepository.findByUserId(instituteUserId)).thenReturn(Optional.of(institute));
        when(assessmentResultRepository.findByTrainingEnrollmentIdAndPassedFalseOrderByAssessmentDateDesc(enrollmentId)).thenReturn(java.util.List.of());
        when(assessmentResultRepository.save(any(AssessmentResult.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AssessmentService service = new AssessmentService(
            assessmentResultRepository,
            enrollmentRepository,
            trainingRepository,
            null, // representativeRepository
            userAccountRepository,
            representativeService,
            trainingInstituteRepository
        );

        AssessmentRequestDto request = new AssessmentRequestDto(
            enrollmentId,
            new BigDecimal("65.00"), // Below passing score
            "Needs improvement",
            Instant.now()
        );

        AssessmentResult result = service.submit(request, jwt);

        assertEquals(AssessmentResultStatus.FAILED, result.getResultStatus());
        assertEquals(EnrollmentStatus.RETAKE_REQUIRED, enrollment.getStatus());
    }
}
