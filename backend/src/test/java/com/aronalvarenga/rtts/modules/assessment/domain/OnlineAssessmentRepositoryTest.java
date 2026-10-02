package com.aronalvarenga.rtts.modules.assessment.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.aronalvarenga.rtts.identity.domain.UserAccount;
import com.aronalvarenga.rtts.identity.domain.UserAccountRepository;
import com.aronalvarenga.rtts.identity.domain.UserRole;
import com.aronalvarenga.rtts.modules.assessment.application.AssessmentAttemptService;
import com.aronalvarenga.rtts.modules.assessment.application.AssessmentGradingService;
import com.aronalvarenga.rtts.modules.assessment.application.OnlineAssessmentService;
import com.aronalvarenga.rtts.modules.delegator.application.DelegatorAssessmentOutcomeService;
import com.aronalvarenga.rtts.modules.representative.application.RepresentativeTrainingMaterialService;
import com.aronalvarenga.rtts.modules.delegator.domain.DelegatorProfile;
import com.aronalvarenga.rtts.modules.delegator.domain.DelegatorProfileRepository;
import com.aronalvarenga.rtts.modules.agentdelegation.domain.AgentDelegation;
import com.aronalvarenga.rtts.modules.agentdelegation.domain.AgentDelegationRepository;
import com.aronalvarenga.rtts.modules.assessment.web.AssessmentAnswerRequest;
import com.aronalvarenga.rtts.modules.assessment.web.AssessmentResultDto;
import com.aronalvarenga.rtts.modules.assessment.web.CandidateAttemptDto;
import com.aronalvarenga.rtts.modules.assessment.web.CandidateAssessmentDto;
import com.aronalvarenga.rtts.modules.assessment.web.CandidateAssessmentMapper;
import com.aronalvarenga.rtts.modules.assessment.web.WrittenQuestionGradeRequest;
import com.aronalvarenga.rtts.modules.enrollments.domain.Enrollment;
import com.aronalvarenga.rtts.modules.enrollments.domain.EnrollmentRepository;
import com.aronalvarenga.rtts.modules.enrollments.domain.EnrollmentStatus;
import com.aronalvarenga.rtts.modules.institutes.domain.TrainingInstitute;
import com.aronalvarenga.rtts.modules.institutes.domain.TrainingInstituteRepository;
import com.aronalvarenga.rtts.modules.representatives.domain.Representative;
import com.aronalvarenga.rtts.modules.representatives.domain.RepresentativeRepository;
import com.aronalvarenga.rtts.modules.trainings.domain.Training;
import com.aronalvarenga.rtts.modules.trainings.domain.TrainingRepository;
import jakarta.persistence.EntityManager;
import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.AfterEach;
import org.hibernate.exception.ConstraintViolationException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.web.server.ResponseStatusException;

@DataJpaTest(properties = {
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Import({
    OnlineAssessmentService.class,
    AssessmentAttemptService.class,
    AssessmentGradingService.class,
    DelegatorAssessmentOutcomeService.class,
    RepresentativeTrainingMaterialService.class,
    AssessmentTestClockConfiguration.class
})
class OnlineAssessmentRepositoryTest {

    @Autowired private OnlineAssessmentRepository assessmentRepository;
    @Autowired private AssessmentQuestionRepository questionRepository;
    @Autowired private AssessmentOptionRepository optionRepository;
    @Autowired private AssessmentAttemptRepository attemptRepository;
    @Autowired private AssessmentAttemptAnswerRepository answerRepository;
    @Autowired private AssessmentResultRecordRepository resultRepository;
    @Autowired private AssessmentQuestionGradeRepository gradeRepository;
    @Autowired private AssessmentResultReleaseRepository releaseRepository;
    @Autowired private TrainingRepository trainingRepository;
    @Autowired private TrainingInstituteRepository instituteRepository;
    @Autowired private UserAccountRepository userRepository;
    @Autowired private RepresentativeRepository representativeRepository;
    @Autowired private EnrollmentRepository enrollmentRepository;
    @Autowired private OnlineAssessmentService assessmentService;
    @Autowired private AssessmentAttemptService attemptService;
    @Autowired private AssessmentGradingService gradingService;
    @Autowired private DelegatorAssessmentOutcomeService delegatorOutcomeService;
    @Autowired private DelegatorProfileRepository delegatorProfileRepository;
    @Autowired private AgentDelegationRepository agentDelegationRepository;
    @Autowired private MutableAssessmentClock clock;
    @Autowired private EntityManager entityManager;

    private UserAccount instituteUser;
    private UserAccount representativeUser;
    private Representative representative;
    private Training training;
    private Enrollment enrollment;
    private ExecutorService executor;

    @BeforeEach
    void setUp() {
        clock.setInstant(Instant.parse("2026-01-01T12:00:00Z"));
        instituteUser = userRepository.save(new UserAccount(
            "institute-" + UUID.randomUUID(),
            "hashed",
            UserRole.TRAINING_INSTITUTE,
            "Training Institute"));
        TrainingInstitute institute = new TrainingInstitute("Institute", "institute@example.test");
        institute.setUserId(instituteUser.getId());
        institute = instituteRepository.save(institute);
        training = trainingRepository.save(new Training(
            institute.getId(),
            "Course",
            "Course description",
            LocalDate.now(),
            LocalDate.now().plusDays(7),
            20));

        representativeUser = userRepository.save(new UserAccount(
            "representative-" + UUID.randomUUID(),
            "hashed",
            UserRole.REPRESENTATIVE,
            "Representative"));
        representative = new Representative("Representative", "rep-" + UUID.randomUUID() + "@example.test");
        representative.setUserId(representativeUser.getId());
        representative = representativeRepository.save(representative);
        enrollment = enrollmentRepository.save(new Enrollment(representative.getId(), training.getId()));
        entityManager.flush();
        entityManager.clear();
        instituteUser = userRepository.findByUsername(instituteUser.getUsername()).orElseThrow();
        training = trainingRepository.findById(training.getId()).orElseThrow();
        enrollment = enrollmentRepository.findById(enrollment.getId()).orElseThrow();
    }

    @AfterEach
    void shutdownExecutor() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    @Test
    void assessmentVersionsAreStoredAndCandidateDtoOmitsCorrectAnswerData() {
        OnlineAssessment draft = createCompleteDraft();
        OnlineAssessment published = assessmentService.publish(instituteUser.getId(), draft.getId());
        AssessmentAttempt attempt = attemptRepository.save(new AssessmentAttempt(published, enrollment, 1));

        AssessmentQuestion question = questionRepository
            .findByAssessment_IdOrderByDisplayOrderAsc(published.getId()).getFirst();
        AssessmentOption correctOption = optionRepository
            .findByQuestion_IdOrderByDisplayOrderAsc(question.getId()).getFirst();
        assertThrows(
            IllegalStateException.class,
            () -> question.updateContent("Changed question", 1, BigDecimal.ONE));
        assertThrows(
            IllegalStateException.class,
            () -> correctOption.updateContent("Changed answer", 1, false));
        AssessmentAttemptAnswer answer = answerRepository.save(
            new AssessmentAttemptAnswer(attempt, question, correctOption));
        attempt.submit(java.time.Instant.now());
        AssessmentResultRecord result = resultRepository.save(new AssessmentResultRecord(
            attempt,
            new BigDecimal("2.00"),
            new BigDecimal("2.00"),
            new BigDecimal("100.00"),
            true));

        entityManager.flush();
        entityManager.clear();

        assertEquals(1, assessmentRepository
            .findByTraining_IdAndStatusOrderByCreatedAtDesc(training.getId(), AssessmentStatus.PUBLISHED).size());
        assertEquals(1, answerRepository.findByAttempt_IdOrderByQuestion_DisplayOrderAsc(attempt.getId()).size());
        assertEquals(attempt.getId(), resultRepository
            .findFirstByAttempt_IdOrderByEvaluatedAtDesc(result.getAttempt().getId()).orElseThrow()
            .getAttempt().getId());

        OnlineAssessment stored = assessmentRepository.findById(published.getId()).orElseThrow();
        CandidateAssessmentDto candidateDto = CandidateAssessmentMapper.toDto(stored);
        assertEquals(1, candidateDto.questions().size());
        assertEquals(2, candidateDto.questions().getFirst().options().size());
        assertFalse(java.util.Arrays.stream(CandidateAssessmentDto.CandidateOptionDto.class.getRecordComponents())
            .anyMatch(component -> component.getName().toLowerCase().contains("correct")));
        assertFalse(java.util.Arrays.stream(CandidateAssessmentDto.CandidateQuestionDto.class.getRecordComponents())
            .anyMatch(component -> component.getName().toLowerCase().contains("rubric")));

        ResponseStatusException publishedEdit = assertThrows(
            ResponseStatusException.class,
            () -> assessmentService.updateDraft(
                instituteUser.getId(), published.getId(), "Unsafe edit", null,
                new BigDecimal("50.00"), 60, 1, null, null, false, false));
        assertEquals(409, publishedEdit.getStatusCode().value());

        OnlineAssessment revision = assessmentService.createRevision(instituteUser.getId(), published.getId());
        assertEquals(2, revision.getVersionNumber());
        assertEquals(AssessmentStatus.DRAFT, revision.getStatus());
        assertThrows(IllegalArgumentException.class, () -> CandidateAssessmentMapper.toDto(revision));
        assertEquals(1, revision.getQuestions().size());
        assertEquals(2, revision.getQuestions().getFirst().getOptions().size());
        assessmentService.updateDraftDetails(
            instituteUser.getId(),
            revision.getId(),
            "Revised assessment",
            "Updated instructions",
            new BigDecimal("75.00"));
        assertEquals("Revised assessment", revision.getTitle());
        assertEquals("Final assessment", published.getTitle());
        assertThrows(IllegalStateException.class, published::publish);
    }

    @Test
    void duplicateQuestionOrderViolatesRepositoryConstraint() {
        OnlineAssessment assessment = assessmentRepository.save(new OnlineAssessment(
            UUID.randomUUID(), 1, training, instituteUser, "Assessment", null, new BigDecimal("70.00")));
        questionRepository.saveAndFlush(new AssessmentQuestion(
            assessment, "First question", 1, new BigDecimal("1.00")));

        questionRepository.save(new AssessmentQuestion(
            assessment, "Duplicate order", 1, new BigDecimal("1.00")));
        assertThrows(ConstraintViolationException.class, entityManager::flush);
    }

    @Test
    void assessmentCannotBePublishedWithoutCorrectMcqOption() {
        OnlineAssessment assessment = assessmentService.createDraft(
            instituteUser.getId(), training.getId(), "Incomplete", null, new BigDecimal("60.00"));
        assessmentService.addQuestion(
            instituteUser.getId(), assessment.getId(), "Question", 1, new BigDecimal("1.00"));

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> assessmentService.publish(instituteUser.getId(), assessment.getId()));
        assertEquals(400, exception.getStatusCode().value());
    }

    @Test
    void twoMultipleChoiceQuestionsWithFourOptionsAndOneCorrectEachCanBePublished() {
        OnlineAssessment assessment = assessmentService.createDraft(
            instituteUser.getId(), training.getId(), "Two-question assessment", "Select one answer.",
            new BigDecimal("70.00"), 45, 2, null, null, false, false);

        for (int questionNumber = 1; questionNumber <= 2; questionNumber++) {
            AssessmentQuestion question = assessmentService.addQuestion(
                instituteUser.getId(),
                assessment.getId(),
                "Question " + questionNumber,
                AssessmentQuestionType.MULTIPLE_CHOICE,
                questionNumber,
                new BigDecimal("2.00"),
                null);
            for (int optionNumber = 1; optionNumber <= 4; optionNumber++) {
                assessmentService.addOption(
                    instituteUser.getId(),
                    assessment.getId(),
                    question.getId(),
                    "Option " + optionNumber,
                    optionNumber,
                    optionNumber == 2);
            }
        }

        OnlineAssessment published = assessmentService.publish(instituteUser.getId(), assessment.getId());

        assertEquals(AssessmentStatus.PUBLISHED, published.getStatus());
        assertEquals(2, published.getQuestions().size());
        assertTrue(published.getQuestions().stream().allMatch(question ->
            question.getOptions().size() == 4
                && question.getOptions().stream().filter(AssessmentOption::isCorrect).count() == 1));
    }

    @Test
    void attemptAnswerRejectsOptionFromDifferentQuestion() {
        OnlineAssessment draft = createCompleteDraft();
        AssessmentQuestion otherQuestion = assessmentService.addQuestion(
            instituteUser.getId(), draft.getId(), "Another question", 2, BigDecimal.ONE);
        assessmentService.addOption(instituteUser.getId(), draft.getId(), otherQuestion.getId(), "A", 1, true);
        assessmentService.addOption(instituteUser.getId(), draft.getId(), otherQuestion.getId(), "B", 2, false);
        OnlineAssessment published = assessmentService.publish(instituteUser.getId(), draft.getId());
        AssessmentAttempt attempt = new AssessmentAttempt(published, enrollment, 1);
        AssessmentQuestion question = published.getQuestions().getFirst();
        AssessmentOption foreignOption = published.getQuestions().get(1).getOptions().getFirst();

        assertThrows(
            IllegalArgumentException.class,
            () -> new AssessmentAttemptAnswer(attempt, question, foreignOption));
        assertTrue(attempt.getAnswers().isEmpty());
    }

    @Test
    void assessmentManagementEnforcesInstituteOwnership() {
        UserAccount otherInstituteUser = userRepository.save(new UserAccount(
            "other-" + UUID.randomUUID(),
            "hashed",
            UserRole.TRAINING_INSTITUTE,
            "Other Institute"));

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> assessmentService.createDraft(
                otherInstituteUser.getId(),
                training.getId(),
                "Unauthorized",
                null,
                new BigDecimal("70.00")));

        assertEquals(403, exception.getStatusCode().value());
    }

    @Test
    void writtenQuestionsRequireRubricAndPublishWhenConfigured() {
        OnlineAssessment assessment = assessmentService.createDraft(
            instituteUser.getId(),
            training.getId(),
            "Written assessment",
            null,
            new BigDecimal("50.00"),
            45,
            2,
            java.time.Instant.now(),
            java.time.Instant.now().plusSeconds(3600),
            true,
            true);
        assertThrows(
            ResponseStatusException.class,
            () -> assessmentService.addQuestion(
                instituteUser.getId(), assessment.getId(), "Explain", AssessmentQuestionType.WRITTEN_RESPONSE,
                1, new BigDecimal("5.00"), null));

        assessmentService.addQuestion(
            instituteUser.getId(), assessment.getId(), "Explain the process",
            AssessmentQuestionType.WRITTEN_RESPONSE, 1, new BigDecimal("5.00"), "Award marks for key steps.");

        OnlineAssessment published = assessmentService.publish(instituteUser.getId(), assessment.getId());
        assertEquals(AssessmentStatus.PUBLISHED, published.getStatus());
        assertEquals(45, published.getDurationMinutes());
        assertEquals(2, published.getAttemptLimit());
        assertTrue(published.isRandomizeQuestions());
        assertTrue(published.isRandomizeOptions());
    }

    @Test
    void invalidAvailabilityConfigurationIsRejected() {
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> assessmentService.createDraft(
                instituteUser.getId(),
                training.getId(),
                "Invalid availability",
                null,
                new BigDecimal("70.00"),
                30,
                1,
                java.time.Instant.parse("2026-01-02T00:00:00Z"),
                java.time.Instant.parse("2026-01-01T00:00:00Z"),
                false,
                false));

        assertEquals(400, exception.getStatusCode().value());
    }

    @Test
    void questionOrderCanBeChangedWithoutCollidingWithUniqueOrderConstraint() {
        OnlineAssessment assessment = assessmentService.createDraft(
            instituteUser.getId(), training.getId(), "Ordered", null, new BigDecimal("60.00"));
        AssessmentQuestion first = assessmentService.addQuestion(
            instituteUser.getId(), assessment.getId(), "First", 1, BigDecimal.ONE);
        AssessmentQuestion second = assessmentService.addQuestion(
            instituteUser.getId(), assessment.getId(), "Second", 2, BigDecimal.ONE);

        List<AssessmentQuestion> reordered = assessmentService.reorderQuestions(
            instituteUser.getId(), assessment.getId(), List.of(second.getId(), first.getId()));

        assertEquals(second.getId(), reordered.getFirst().getId());
        assertEquals(first.getId(), reordered.getLast().getId());
    }

    @Test
    void addingQuestionAndOptionAtExistingOrderInsertsAndReordersSafely() {
        OnlineAssessment assessment = assessmentService.createDraft(
            instituteUser.getId(), training.getId(), "Insert", null, new BigDecimal("60.00"));
        AssessmentQuestion first = assessmentService.addQuestion(
            instituteUser.getId(), assessment.getId(), "First", 1, BigDecimal.ONE);
        AssessmentQuestion second = assessmentService.addQuestion(
            instituteUser.getId(), assessment.getId(), "Second", 1, BigDecimal.ONE);
        AssessmentQuestion inserted = questionRepository.findById(second.getId()).orElseThrow();
        assertEquals(1, inserted.getDisplayOrder());
        assertEquals(2, questionRepository.findById(first.getId()).orElseThrow().getDisplayOrder());

        AssessmentOption firstOption = assessmentService.addOption(
            instituteUser.getId(), assessment.getId(), inserted.getId(), "First option", 1, true);
        AssessmentOption secondOption = assessmentService.addOption(
            instituteUser.getId(), assessment.getId(), inserted.getId(), "Second option", 1, false);
        assertEquals(1, optionRepository.findById(secondOption.getId()).orElseThrow().getDisplayOrder());
        assertEquals(2, optionRepository.findById(firstOption.getId()).orElseThrow().getDisplayOrder());
    }

    @Test
    void attemptStartsUsesBackendClockAndPersistsSafeOrderAndAnswersForResumption() {
        OnlineAssessment published = assessmentService.publish(instituteUser.getId(), createCompleteDraft().getId());
        CandidateAttemptDto started = attemptService.start(representativeUser.getId(), published.getId());

        assertEquals(clock.instant(), started.startedAt());
        assertEquals(clock.instant().plusSeconds(60 * 60L), started.deadlineAt());
        assertEquals("IN_PROGRESS", started.status());
        assertEquals(1, started.questions().size());
        CandidateAttemptDto.QuestionDto question = started.questions().getFirst();
        assertEquals(2, question.options().size());
        UUID correctOption = optionRepository.findByQuestion_IdOrderByDisplayOrderAsc(question.id()).getFirst().getId();

        CandidateAttemptDto saved = attemptService.saveAnswer(
            representativeUser.getId(),
            started.id(),
            question.id(),
            new AssessmentAnswerRequest(correctOption, null));
        CandidateAttemptDto resumed = attemptService.resume(representativeUser.getId(), started.id());

        assertEquals(saved.id(), resumed.id());
        assertEquals(correctOption, resumed.questions().getFirst().selectedOptionId());
        assertEquals(60 * 60L, resumed.remainingSeconds());
        assertFalse(java.util.Arrays.stream(CandidateAttemptDto.class.getRecordComponents())
            .anyMatch(component -> component.getName().toLowerCase().contains("correct")
                || component.getName().toLowerCase().contains("rubric")));
        assertFalse(java.util.Arrays.stream(CandidateAttemptDto.QuestionDto.class.getRecordComponents())
            .anyMatch(component -> component.getName().toLowerCase().contains("correct")
                || component.getName().toLowerCase().contains("rubric")));
    }

    @Test
    void submissionIsIdempotentFreezesAnswersAndEnforcesAttemptLimit() {
        OnlineAssessment published = assessmentService.publish(instituteUser.getId(), createCompleteDraft().getId());
        CandidateAttemptDto started = attemptService.start(representativeUser.getId(), published.getId());
        UUID questionId = started.questions().getFirst().id();
        UUID optionId = started.questions().getFirst().options().getFirst().id();
        attemptService.saveAnswer(
            representativeUser.getId(), started.id(), questionId, new AssessmentAnswerRequest(optionId, null));

        CandidateAttemptDto firstSubmit = attemptService.submit(representativeUser.getId(), started.id());
        CandidateAttemptDto duplicateSubmit = attemptService.submit(representativeUser.getId(), started.id());
        assertEquals("SUBMITTED", firstSubmit.status());
        assertEquals(firstSubmit.submittedAt(), duplicateSubmit.submittedAt());
        assertEquals(firstSubmit.id(), duplicateSubmit.id());
        AssessmentResultRecord automaticResult = resultRepository
            .findFirstByAttempt_IdAndFinalResultTrueOrderByGradingRevisionDesc(started.id()).orElseThrow();
        assertTrue(automaticResult.isPassed());
        assertTrue(attemptService.listEligible(representativeUser.getId()).isEmpty());
        assertEquals(1, resultRepository.findByAttempt_IdOrderByEvaluatedAtDesc(started.id()).size());

        ResponseStatusException closedSave = assertThrows(
            ResponseStatusException.class,
            () -> attemptService.saveAnswer(
                representativeUser.getId(), started.id(), questionId, new AssessmentAnswerRequest(null, null)));
        assertEquals(409, closedSave.getStatusCode().value());
        ResponseStatusException limit = assertThrows(
            ResponseStatusException.class,
            () -> attemptService.start(representativeUser.getId(), published.getId()));
        assertEquals(409, limit.getStatusCode().value());
    }

    @Test
    void expiredAttemptFreezesSavedAnswersAndLeavesUnansweredQuestionsBlank() {
        OnlineAssessment published = assessmentService.publish(instituteUser.getId(), createCompleteDraft().getId());
        CandidateAttemptDto started = attemptService.start(representativeUser.getId(), published.getId());
        UUID questionId = started.questions().getFirst().id();
        UUID wrongOptionId = started.questions().getFirst().options().getLast().id();
        attemptService.saveAnswer(
            representativeUser.getId(), started.id(), questionId, new AssessmentAnswerRequest(wrongOptionId, null));

        clock.setInstant(started.deadlineAt());
        CandidateAttemptDto expired = attemptService.resume(representativeUser.getId(), started.id());

        assertEquals("EXPIRED", expired.status());
        assertEquals(started.deadlineAt(), expired.expiredAt());
        assertEquals(wrongOptionId, expired.questions().getFirst().selectedOptionId());
        assertEquals(0, expired.remainingSeconds());
        assertEquals(1, resultRepository.findByAttempt_IdOrderByEvaluatedAtDesc(started.id()).size());
        CandidateAttemptDto lateAutosave = attemptService.saveAnswer(
            representativeUser.getId(),
            started.id(),
            questionId,
            new AssessmentAnswerRequest(started.questions().getFirst().options().getFirst().id(), null));
        assertEquals("EXPIRED", lateAutosave.status());
        assertEquals(wrongOptionId, lateAutosave.questions().getFirst().selectedOptionId());
        CandidateAttemptDto repeatedExpiry = attemptService.submit(representativeUser.getId(), started.id());
        assertEquals("EXPIRED", repeatedExpiry.status());
    }

    @Test
    void deadlineSchedulerMarksAttemptExpiredAndScoresUnansweredMcqAsZero() {
        OnlineAssessment published = assessmentService.publish(instituteUser.getId(), createCompleteDraft().getId());
        CandidateAttemptDto started = attemptService.start(representativeUser.getId(), published.getId());
        clock.setInstant(started.deadlineAt().plusSeconds(1));

        assertTrue(attemptService.expireDueAttempts() >= 1);
        assertEquals(
            AssessmentAttemptStatus.EXPIRED,
            attemptRepository.findById(started.id()).orElseThrow().getStatus());
        AssessmentResultRecord result = resultRepository
            .findFirstByAttempt_IdAndFinalResultTrueOrderByGradingRevisionDesc(started.id()).orElseThrow();
        assertEquals(0, result.getPointsEarned().compareTo(BigDecimal.ZERO));
        assertFalse(result.isPassed());
        assertTrue(answerRepository.findByAttempt_IdOrderByQuestion_DisplayOrderAsc(started.id()).isEmpty());
    }

    @Test
    void writtenResponsesAreAutosavedWithoutReturningRubricsOrAutoGrading() {
        OnlineAssessment draft = assessmentService.createDraft(
            instituteUser.getId(), training.getId(), "Written assessment", null,
            new BigDecimal("70.00"), 30, 1, null, null, false, false);
        AssessmentQuestion question = assessmentService.addQuestion(
            instituteUser.getId(),
            draft.getId(),
            "Explain your reasoning",
            AssessmentQuestionType.WRITTEN_RESPONSE,
            1,
            new BigDecimal("5.00"),
            "Evaluate the reasoning");
        OnlineAssessment published = assessmentService.publish(instituteUser.getId(), draft.getId());
        CandidateAttemptDto started = attemptService.start(representativeUser.getId(), published.getId());

        CandidateAttemptDto saved = attemptService.saveAnswer(
            representativeUser.getId(),
            started.id(),
            question.getId(),
            new AssessmentAnswerRequest(null, "The written response."));
        assertEquals("The written response.", saved.questions().getFirst().responseText());
        assertFalse(java.util.Arrays.stream(CandidateAttemptDto.QuestionDto.class.getRecordComponents())
            .anyMatch(component -> component.getName().toLowerCase().contains("rubric")));

        attemptService.submit(representativeUser.getId(), started.id());
        assertTrue(resultRepository.findByAttempt_IdOrderByEvaluatedAtDesc(started.id()).isEmpty());
    }

    @Test
    void mixedAssessmentExposesOnlyProvisionalObjectiveScoreUntilWrittenGrading() {
        OnlineAssessment draft = assessmentService.createDraft(
            instituteUser.getId(), training.getId(), "Mixed assessment", null,
            new BigDecimal("70.00"), 30, 1, null, null, false, false);
        AssessmentQuestion mcq = assessmentService.addQuestion(
            instituteUser.getId(), draft.getId(), "Choose the correct option",
            AssessmentQuestionType.MULTIPLE_CHOICE, 1, new BigDecimal("2.00"), null);
        assessmentService.addOption(instituteUser.getId(), draft.getId(), mcq.getId(), "Correct", 1, true);
        assessmentService.addOption(instituteUser.getId(), draft.getId(), mcq.getId(), "Incorrect", 2, false);
        assessmentService.addQuestion(
            instituteUser.getId(), draft.getId(), "Explain your answer",
            AssessmentQuestionType.WRITTEN_RESPONSE, 2, new BigDecimal("3.00"), "Evaluate the explanation");
        OnlineAssessment published = assessmentService.publish(instituteUser.getId(), draft.getId());
        CandidateAttemptDto started = attemptService.start(representativeUser.getId(), published.getId());
        UUID mcqId = started.questions().stream()
            .filter(question -> question.questionType().equals(AssessmentQuestionType.MULTIPLE_CHOICE.name()))
            .findFirst().orElseThrow().id();
        UUID correctOptionId = optionRepository.findByQuestion_IdOrderByDisplayOrderAsc(mcqId)
            .getFirst().getId();

        attemptService.saveAnswer(
            representativeUser.getId(), started.id(), mcqId, new AssessmentAnswerRequest(correctOptionId, null));
        CandidateAttemptDto submitted = attemptService.submit(representativeUser.getId(), started.id());

        AssessmentResultRecord provisional = resultRepository
            .findFirstByAttempt_IdOrderByEvaluatedAtDesc(started.id()).orElseThrow();
        assertEquals(new BigDecimal("100.00"), provisional.getScorePercent());
        assertFalse(provisional.isFinalResult());
    }

    @Test
    void writtenGradingQueueRequiresCompleteAuthorizedGradingBeforeRelease() {
        OnlineAssessment draft = assessmentService.createDraft(
            instituteUser.getId(), training.getId(), "Written assessment", null,
            new BigDecimal("70.00"), 30, 1, null, null, false, false);
        AssessmentQuestion question = assessmentService.addQuestion(
            instituteUser.getId(), draft.getId(), "Explain the answer",
            AssessmentQuestionType.WRITTEN_RESPONSE, 1, new BigDecimal("5.00"), "Assess clarity");
        OnlineAssessment published = assessmentService.publish(instituteUser.getId(), draft.getId());
        CandidateAttemptDto started = attemptService.start(representativeUser.getId(), published.getId());
        attemptService.saveAnswer(
            representativeUser.getId(), started.id(), question.getId(),
            new AssessmentAnswerRequest(null, "A complete explanation."));
        attemptService.submit(representativeUser.getId(), started.id());

        assertEquals(1, gradingService.gradingQueue(instituteUser.getId()).size());
        assertEquals(0, gradingService.getWrittenAnswers(instituteUser.getId(), started.id())
            .answers().getFirst().history().size());

        ResponseStatusException pending = assertThrows(
            ResponseStatusException.class,
            () -> gradingService.finalizeGrading(instituteUser.getId(), started.id()));
        assertEquals(409, pending.getStatusCode().value());
        ResponseStatusException unreleased = assertThrows(
            ResponseStatusException.class,
            () -> gradingService.getReleasedCandidateResult(representativeUser.getId(), started.id()));
        assertEquals(404, unreleased.getStatusCode().value());
        ResponseStatusException invalidMarks = assertThrows(
            ResponseStatusException.class,
            () -> gradingService.gradeWrittenAnswer(
                instituteUser.getId(),
                started.id(),
                question.getId(),
                new WrittenQuestionGradeRequest(new BigDecimal("5.01"), "Too many marks")));
        assertEquals(400, invalidMarks.getStatusCode().value());
        ResponseStatusException negativeMarks = assertThrows(
            ResponseStatusException.class,
            () -> gradingService.gradeWrittenAnswer(
                instituteUser.getId(),
                started.id(),
                question.getId(),
                new WrittenQuestionGradeRequest(new BigDecimal("-0.01"), "Negative")));
        assertEquals(400, negativeMarks.getStatusCode().value());

        UserAccount otherInstituteUser = userRepository.save(new UserAccount(
            "other-institute-" + UUID.randomUUID(),
            "hashed",
            UserRole.TRAINING_INSTITUTE,
            "Other institute"));
        TrainingInstitute otherInstitute = new TrainingInstitute(
            "Other institute", "other-institute-" + UUID.randomUUID() + "@example.test");
        otherInstitute.setUserId(otherInstituteUser.getId());
        instituteRepository.save(otherInstitute);
        ResponseStatusException denied = assertThrows(
            ResponseStatusException.class,
            () -> gradingService.getWrittenAnswers(otherInstituteUser.getId(), started.id()));
        assertEquals(403, denied.getStatusCode().value());

        gradingService.gradeWrittenAnswer(
            instituteUser.getId(),
            started.id(),
            question.getId(),
            new WrittenQuestionGradeRequest(new BigDecimal("3.00"), "Good explanation"));
        gradingService.gradeWrittenAnswer(
            instituteUser.getId(),
            started.id(),
            question.getId(),
            new WrittenQuestionGradeRequest(new BigDecimal("4.00"), "Clarification improved the grade"));
        AssessmentResultDto finalized = gradingService.finalizeGrading(instituteUser.getId(), started.id());
        assertEquals(finalized, gradingService.finalizeGrading(instituteUser.getId(), started.id()));

        assertEquals(new BigDecimal("4.00"), finalized.awardedMarks());
        assertEquals(new BigDecimal("5.00"), finalized.maximumMarks());
        assertEquals(new BigDecimal("80.00"), finalized.percentage());
        assertTrue(finalized.passed());
        assertEquals(2, gradingService.getWrittenAnswers(instituteUser.getId(), started.id())
            .answers().getFirst().history().size());
        assertEquals(2, gradeRepository.findByAttempt_IdAndQuestion_IdOrderByGradeRevisionDesc(
            started.id(), question.getId()).size());

        ResponseStatusException notReleased = assertThrows(
            ResponseStatusException.class,
            () -> gradingService.getReleasedCandidateResult(representativeUser.getId(), started.id()));
        assertEquals(404, notReleased.getStatusCode().value());
        AssessmentResultDto released = gradingService.releaseResult(instituteUser.getId(), started.id());
        assertEquals(clock.instant(), released.releasedAt());
        assertEquals(EnrollmentStatus.COMPLETED,
            enrollmentRepository.findById(enrollment.getId()).orElseThrow().getStatus());
        assertEquals(true, enrollmentRepository.findById(enrollment.getId()).orElseThrow().getPassed());
        assertEquals(com.aronalvarenga.rtts.modules.representatives.domain.RepresentativeStatus.TRAINED,
            representativeRepository.findById(representative.getId()).orElseThrow().getStatus());
        assertEquals(released, gradingService.releaseResult(instituteUser.getId(), started.id()));
        assertEquals(released, gradingService.getReleasedCandidateResult(representativeUser.getId(), started.id()));
        assertEquals(1, releaseRepository.findByAttempt_IdOrderByReleaseNumberDesc(started.id()).size());
        assertEquals(1, resultRepository.findByAttempt_IdOrderByEvaluatedAtDesc(started.id()).size());

        gradingService.gradeWrittenAnswer(
            instituteUser.getId(),
            started.id(),
            question.getId(),
            new WrittenQuestionGradeRequest(new BigDecimal("3.00"), "Revised after review"));
        AssessmentResultDto revised = gradingService.finalizeGrading(instituteUser.getId(), started.id());
        assertEquals(new BigDecimal("60.00"), revised.percentage());
        assertFalse(revised.passed());
        AssessmentResultDto revisedRelease = gradingService.releaseResult(instituteUser.getId(), started.id());
        AssessmentResultDto representativeResult =
            gradingService.getReleasedCandidateResult(representativeUser.getId(), started.id());
        assertEquals(revised.awardedMarks(), representativeResult.awardedMarks());
        assertEquals(revised.passed(), representativeResult.passed());
        assertEquals(3, gradeRepository.findByAttempt_IdAndQuestion_IdOrderByGradeRevisionDesc(
            started.id(), question.getId()).size());
        assertEquals(2, resultRepository.findByAttempt_IdOrderByEvaluatedAtDesc(started.id()).size());
        assertEquals(2, releaseRepository.findByAttempt_IdOrderByReleaseNumberDesc(started.id()).size());
        assertEquals(clock.instant(), revisedRelease.releasedAt());
    }

    @Test
    void mixedAssessmentFinalScoreIncludesMcqAndWrittenMarksAndIsHiddenUntilReleased() {
        OnlineAssessment draft = assessmentService.createDraft(
            instituteUser.getId(), training.getId(), "Mixed assessment", null,
            new BigDecimal("70.00"), 30, 1, null, null, false, false);
        AssessmentQuestion mcq = assessmentService.addQuestion(
            instituteUser.getId(), draft.getId(), "Select the answer",
            AssessmentQuestionType.MULTIPLE_CHOICE, 1, new BigDecimal("2.00"), null);
        AssessmentOption correct = assessmentService.addOption(
            instituteUser.getId(), draft.getId(), mcq.getId(), "Correct", 1, true);
        assessmentService.addOption(instituteUser.getId(), draft.getId(), mcq.getId(), "Incorrect", 2, false);
        AssessmentQuestion written = assessmentService.addQuestion(
            instituteUser.getId(), draft.getId(), "Explain",
            AssessmentQuestionType.WRITTEN_RESPONSE, 2, new BigDecimal("3.00"), "Evaluate");
        OnlineAssessment published = assessmentService.publish(instituteUser.getId(), draft.getId());
        CandidateAttemptDto started = attemptService.start(representativeUser.getId(), published.getId());
        attemptService.saveAnswer(
            representativeUser.getId(), started.id(), mcq.getId(),
            new AssessmentAnswerRequest(correct.getId(), null));
        attemptService.submit(representativeUser.getId(), started.id());
        gradingService.gradeWrittenAnswer(
            instituteUser.getId(), started.id(), written.getId(),
            new WrittenQuestionGradeRequest(new BigDecimal("1.50"), "Partial credit"));

        AssessmentResultDto result = gradingService.finalizeGrading(instituteUser.getId(), started.id());
        assertEquals(new BigDecimal("3.50"), result.awardedMarks());
        assertEquals(new BigDecimal("5.00"), result.maximumMarks());
        assertEquals(new BigDecimal("70.00"), result.percentage());
        assertTrue(result.passed());

        assertEquals(2, resultRepository.findByAttempt_IdOrderByEvaluatedAtDesc(started.id()).size());
        assertTrue(releaseRepository.findByAttempt_IdOrderByReleaseNumberDesc(started.id()).isEmpty());
        assertEquals(result, gradingService.getInstituteResult(instituteUser.getId(), started.id()));
        ResponseStatusException unreleased = assertThrows(
            ResponseStatusException.class,
            () -> gradingService.getReleasedCandidateResult(representativeUser.getId(), started.id()));
        assertEquals(404, unreleased.getStatusCode().value());
    }

    @Test
    void representativeHistoryAndActiveDelegatorOutcomesExposeOnlyReleasedResultsAndFeedback() {
        OnlineAssessment draft = assessmentService.createDraft(
            instituteUser.getId(), training.getId(), "Written assessment", null,
            new BigDecimal("50.00"), 30, 1, null, null, false, false);
        AssessmentQuestion written = assessmentService.addQuestion(
            instituteUser.getId(), draft.getId(), "Explain your answer",
            AssessmentQuestionType.WRITTEN_RESPONSE, 1, new BigDecimal("5.00"), "Assess the explanation");
        OnlineAssessment published = assessmentService.publish(instituteUser.getId(), draft.getId());
        CandidateAttemptDto started = attemptService.start(representativeUser.getId(), published.getId());
        attemptService.saveAnswer(
            representativeUser.getId(), started.id(), written.getId(),
            new AssessmentAnswerRequest(null, "My written answer"));
        attemptService.submit(representativeUser.getId(), started.id());
        gradingService.gradeWrittenAnswer(
            instituteUser.getId(), started.id(), written.getId(),
            new WrittenQuestionGradeRequest(new BigDecimal("4.00"), "Clear explanation"));
        gradingService.finalizeGrading(instituteUser.getId(), started.id());
        gradingService.releaseResult(instituteUser.getId(), started.id());

        var history = attemptService.listHistory(representativeUser.getId());
        assertEquals(1, history.size());
        assertEquals("SUBMITTED", history.getFirst().status());
        assertEquals(new BigDecimal("80.00"), history.getFirst().releasedResult().percentage());
        AssessmentResultDto released = gradingService.getReleasedCandidateResult(
            representativeUser.getId(), started.id());
        assertEquals("Clear explanation", released.writtenFeedback().getFirst().feedback());
        var instituteHistory = gradingService.completedAttempts(instituteUser.getId());
        assertEquals(1, instituteHistory.size());
        assertTrue(instituteHistory.getFirst().finalized());
        assertTrue(instituteHistory.getFirst().releasedAt() != null);

        UserAccount delegatorUser = userRepository.save(new UserAccount(
            "delegator-" + UUID.randomUUID(), "hashed", UserRole.DELEGATOR, "Delegator"));
        DelegatorProfile delegator = delegatorProfileRepository.save(new DelegatorProfile(
            delegatorUser.getId(), "Delegator", "delegator-" + UUID.randomUUID() + "@example.test"));
        agentDelegationRepository.save(new AgentDelegation(
            representative.getId(), delegator.getId(), "Assigned representative"));
        UserAccount otherDelegator = userRepository.save(new UserAccount(
            "other-delegator-" + UUID.randomUUID(), "hashed", UserRole.DELEGATOR, "Other delegator"));
        delegatorProfileRepository.save(new DelegatorProfile(
            otherDelegator.getId(), "Other delegator", "other-delegator-" + UUID.randomUUID() + "@example.test"));

        var outcomes = delegatorOutcomeService.listReleasedOutcomes(delegatorUser.getUsername());
        assertEquals(1, outcomes.size());
        assertEquals(representative.getFullName(), outcomes.getFirst().representativeName());
        assertEquals(new BigDecimal("80.00"), outcomes.getFirst().percentage());
        assertTrue(delegatorOutcomeService.listReleasedOutcomes(otherDelegator.getUsername()).isEmpty());
    }

    @Test
    void failedOrCompletedEnrollmentCannotTakePublishedAssessment() {
        OnlineAssessment published = assessmentService.publish(instituteUser.getId(), createCompleteDraft().getId());
        enrollment.setStatus(com.aronalvarenga.rtts.modules.enrollments.domain.EnrollmentStatus.FAILED);
        enrollmentRepository.save(enrollment);

        assertTrue(attemptService.listEligible(representativeUser.getId()).isEmpty());
        ResponseStatusException forbidden = assertThrows(
            ResponseStatusException.class,
            () -> attemptService.start(representativeUser.getId(), published.getId()));
        assertEquals(403, forbidden.getStatusCode().value());
    }

    @Test
    void attemptCannotBeReadByAnotherRepresentative() {
        OnlineAssessment published = assessmentService.publish(instituteUser.getId(), createCompleteDraft().getId());
        CandidateAttemptDto started = attemptService.start(representativeUser.getId(), published.getId());
        UserAccount anotherUser = userRepository.save(new UserAccount(
            "other-representative-" + UUID.randomUUID(),
            "hashed",
            UserRole.REPRESENTATIVE,
            "Other representative"));
        Representative otherRepresentative = new Representative(
            "Other representative", "other-" + UUID.randomUUID() + "@example.test");
        otherRepresentative.setUserId(anotherUser.getId());
        representativeRepository.save(otherRepresentative);

        ResponseStatusException unauthorized = assertThrows(
            ResponseStatusException.class,
            () -> attemptService.saveAnswer(
                anotherUser.getId(),
                started.id(),
                started.questions().getFirst().id(),
                new AssessmentAnswerRequest(null, null)));
        assertEquals(404, unauthorized.getStatusCode().value());
    }

    @Test
    void concurrentStartRequestsReuseOneActiveAttempt() throws Exception {
        OnlineAssessment published = assessmentService.publish(instituteUser.getId(), createCompleteDraft().getId());
        entityManager.flush();
        TestTransaction.flagForCommit();
        TestTransaction.end();
        CountDownLatch startGate = new CountDownLatch(1);
        executor = Executors.newFixedThreadPool(2);
        Future<CandidateAttemptDto> first = executor.submit(() -> {
            startGate.await();
            return attemptService.start(representativeUser.getId(), published.getId());
        });
        Future<CandidateAttemptDto> second = executor.submit(() -> {
            startGate.await();
            return attemptService.start(representativeUser.getId(), published.getId());
        });
        startGate.countDown();

        CandidateAttemptDto firstAttempt = first.get();
        CandidateAttemptDto secondAttempt = second.get();
        assertEquals(firstAttempt.id(), secondAttempt.id());
        assertEquals(1, attemptRepository.countByAssessment_IdAndEnrollment_Id(published.getId(), enrollment.getId()));
    }

    private OnlineAssessment createCompleteDraft() {
        OnlineAssessment assessment = assessmentService.createDraft(
            instituteUser.getId(), training.getId(), "Final assessment", "Choose one answer", new BigDecimal("70.00"));
        AssessmentQuestion question = assessmentService.addQuestion(
            instituteUser.getId(), assessment.getId(), "What is 1 + 1?", 1, new BigDecimal("2.00"));
        assessmentService.addOption(instituteUser.getId(), assessment.getId(), question.getId(), "2", 1, true);
        assessmentService.addOption(instituteUser.getId(), assessment.getId(), question.getId(), "3", 2, false);
        return assessmentRepository.findById(assessment.getId()).orElseThrow();
    }

}
