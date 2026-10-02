package com.aronalvarenga.rtts.modules.assessment.application;

import com.aronalvarenga.rtts.identity.domain.UserAccount;
import com.aronalvarenga.rtts.identity.domain.UserAccountRepository;
import com.aronalvarenga.rtts.identity.domain.UserRole;
import com.aronalvarenga.rtts.modules.assessment.domain.AssessmentAttempt;
import com.aronalvarenga.rtts.modules.assessment.domain.AssessmentAttemptAnswer;
import com.aronalvarenga.rtts.modules.assessment.domain.AssessmentAttemptAnswerRepository;
import com.aronalvarenga.rtts.modules.assessment.domain.AssessmentAttemptRepository;
import com.aronalvarenga.rtts.modules.assessment.domain.AssessmentAttemptStatus;
import com.aronalvarenga.rtts.modules.assessment.domain.AssessmentQuestion;
import com.aronalvarenga.rtts.modules.assessment.domain.AssessmentQuestionGrade;
import com.aronalvarenga.rtts.modules.assessment.domain.AssessmentQuestionGradeRepository;
import com.aronalvarenga.rtts.modules.assessment.domain.AssessmentQuestionType;
import com.aronalvarenga.rtts.modules.assessment.domain.AssessmentResultRecord;
import com.aronalvarenga.rtts.modules.assessment.domain.AssessmentResultRecordRepository;
import com.aronalvarenga.rtts.modules.assessment.domain.AssessmentResultRelease;
import com.aronalvarenga.rtts.modules.assessment.domain.AssessmentResultReleaseRepository;
import com.aronalvarenga.rtts.modules.assessment.web.AssessmentResultDto;
import com.aronalvarenga.rtts.modules.assessment.web.InstituteGradingQueueItemDto;
import com.aronalvarenga.rtts.modules.assessment.web.InstituteCompletedAttemptDto;
import com.aronalvarenga.rtts.modules.assessment.web.InstituteWrittenGradingDto;
import com.aronalvarenga.rtts.modules.assessment.web.WrittenQuestionGradeRequest;
import com.aronalvarenga.rtts.modules.enrollments.domain.EnrollmentRepository;
import com.aronalvarenga.rtts.modules.enrollments.domain.EnrollmentStatus;
import com.aronalvarenga.rtts.modules.institutes.domain.TrainingInstituteRepository;
import com.aronalvarenga.rtts.modules.representatives.domain.Representative;
import com.aronalvarenga.rtts.modules.representatives.domain.RepresentativeRepository;
import com.aronalvarenga.rtts.modules.representatives.domain.RepresentativeStatus;
import com.aronalvarenga.rtts.modules.trainings.domain.Training;
import com.aronalvarenga.rtts.modules.trainings.domain.TrainingRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AssessmentGradingService {

    private final AssessmentAttemptRepository attemptRepository;
    private final AssessmentAttemptAnswerRepository answerRepository;
    private final AssessmentQuestionGradeRepository gradeRepository;
    private final AssessmentResultRecordRepository resultRepository;
    private final AssessmentResultReleaseRepository releaseRepository;
    private final UserAccountRepository userRepository;
    private final TrainingInstituteRepository instituteRepository;
    private final TrainingRepository trainingRepository;
    private final RepresentativeRepository representativeRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final Clock clock;

    public AssessmentGradingService(
        AssessmentAttemptRepository attemptRepository,
        AssessmentAttemptAnswerRepository answerRepository,
        AssessmentQuestionGradeRepository gradeRepository,
        AssessmentResultRecordRepository resultRepository,
        AssessmentResultReleaseRepository releaseRepository,
        UserAccountRepository userRepository,
        TrainingInstituteRepository instituteRepository,
        TrainingRepository trainingRepository,
        RepresentativeRepository representativeRepository,
        EnrollmentRepository enrollmentRepository,
        Clock clock
    ) {
        this.attemptRepository = attemptRepository;
        this.answerRepository = answerRepository;
        this.gradeRepository = gradeRepository;
        this.resultRepository = resultRepository;
        this.releaseRepository = releaseRepository;
        this.userRepository = userRepository;
        this.instituteRepository = instituteRepository;
        this.trainingRepository = trainingRepository;
        this.representativeRepository = representativeRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<InstituteGradingQueueItemDto> gradingQueue(UUID graderUserId) {
        UserAccount grader = authorizeGrader(graderUserId);
        List<InstituteGradingQueueItemDto> queue = new ArrayList<>();
        for (AssessmentAttempt attempt : attemptRepository.findByStatusInOrderByStartedAtDesc(
            List.of(AssessmentAttemptStatus.SUBMITTED, AssessmentAttemptStatus.EXPIRED))) {
            if (!ownsTraining(grader, attempt.getAssessment().getTraining().getId())) {
                continue;
            }
            List<AssessmentQuestion> writtenQuestions = attempt.getAssessment().getQuestions().stream()
                .filter(question -> question.getQuestionType() == AssessmentQuestionType.WRITTEN_RESPONSE)
                .toList();
            if (writtenQuestions.isEmpty()) {
                continue;
            }
            int gradedCount = 0;
            for (AssessmentQuestion question : writtenQuestions) {
                if (gradeRepository.findFirstByAttempt_IdAndQuestion_IdOrderByGradeRevisionDesc(
                    attempt.getId(), question.getId()).isPresent()) {
                    gradedCount++;
                }
            }
            Representative representative = representativeRepository
                .findById(attempt.getEnrollment().getRepresentativeId()).orElse(null);
            queue.add(new InstituteGradingQueueItemDto(
                attempt.getId(),
                attempt.getAssessment().getId(),
                attempt.getAssessment().getTitle(),
                representative == null ? "Unavailable representative" : representative.getFullName(),
                attempt.getAttemptNumber(),
                attempt.getStatus().name(),
                attempt.getSubmittedAt(),
                attempt.getExpiredAt(),
                writtenQuestions.size(),
                gradedCount));
        }
        return List.copyOf(queue);
    }

    @Transactional(readOnly = true)
    public List<InstituteCompletedAttemptDto> completedAttempts(UUID viewerUserId) {
        UserAccount viewer = authorizeGrader(viewerUserId);
        List<InstituteCompletedAttemptDto> completed = new ArrayList<>();
        for (AssessmentAttempt attempt : attemptRepository.findByStatusInOrderByStartedAtDesc(
            List.of(AssessmentAttemptStatus.SUBMITTED, AssessmentAttemptStatus.EXPIRED))) {
            if (!ownsTraining(viewer, attempt.getTrainingId())) {
                continue;
            }
            List<AssessmentQuestion> writtenQuestions = attempt.getAssessment().getQuestions().stream()
                .filter(question -> question.getQuestionType() == AssessmentQuestionType.WRITTEN_RESPONSE)
                .toList();
            int gradedCount = (int) writtenQuestions.stream()
                .filter(question -> gradeRepository.findFirstByAttempt_IdAndQuestion_IdOrderByGradeRevisionDesc(
                    attempt.getId(), question.getId()).isPresent())
                .count();
            AssessmentResultRelease release = releaseRepository
                .findFirstByAttempt_IdOrderByReleaseNumberDesc(attempt.getId()).orElse(null);
            AssessmentResultRecord finalResult = resultRepository
                .findFirstByAttempt_IdAndFinalResultTrueOrderByGradingRevisionDesc(attempt.getId()).orElse(null);
            AssessmentResultRecord displayedResult = release == null ? finalResult : release.getResult();
            Representative representative = representativeRepository
                .findById(attempt.getEnrollment().getRepresentativeId()).orElse(null);
            completed.add(new InstituteCompletedAttemptDto(
                attempt.getId(),
                attempt.getAssessment().getId(),
                attempt.getAssessment().getTitle(),
                representative == null ? "Unavailable representative" : representative.getFullName(),
                attempt.getAttemptNumber(),
                attempt.getStatus().name(),
                attempt.getSubmittedAt(),
                attempt.getExpiredAt(),
                writtenQuestions.size(),
                gradedCount,
                finalResult != null,
                release == null ? null : release.getReleasedAt(),
                displayedResult == null ? null : displayedResult.getPointsEarned(),
                displayedResult == null ? null : displayedResult.getTotalPoints(),
                displayedResult == null ? null : displayedResult.getScorePercent(),
                displayedResult == null ? null : displayedResult.isPassed()));
        }
        return List.copyOf(completed);
    }

    @Transactional(readOnly = true)
    public InstituteWrittenGradingDto getWrittenAnswers(UUID graderUserId, UUID attemptId) {
        AssessmentAttempt attempt = ownedAttempt(graderUserId, attemptId);
        requireClosed(attempt);
        List<InstituteWrittenGradingDto.WrittenAnswerDto> answers = new ArrayList<>();
        for (AssessmentQuestion question : attempt.getAssessment().getQuestions()) {
            if (question.getQuestionType() != AssessmentQuestionType.WRITTEN_RESPONSE) {
                continue;
            }
            AssessmentAttemptAnswer answer = answerRepository
                .findByAttempt_IdAndQuestion_Id(attemptId, question.getId()).orElse(null);
            List<AssessmentQuestionGrade> history = gradeRepository
                .findByAttempt_IdAndQuestion_IdOrderByGradeRevisionDesc(attemptId, question.getId());
            AssessmentQuestionGrade latest = history.isEmpty() ? null : history.getFirst();
            answers.add(new InstituteWrittenGradingDto.WrittenAnswerDto(
                question.getId(),
                question.getPrompt(),
                answer == null ? null : answer.getResponseText(),
                question.getGradingRubric(),
                question.getPoints(),
                latest == null ? null : latest.getAwardedMarks(),
                latest == null ? null : latest.getFeedback(),
                latest == null ? null : latest.getGrader().getId(),
                latest == null ? null : latest.getGradedAt(),
                history.stream().map(grade -> new InstituteWrittenGradingDto.GradeHistoryDto(
                    grade.getAwardedMarks(),
                    grade.getFeedback(),
                    grade.getGrader().getId(),
                    grade.getGradedAt())).toList()));
        }
        return new InstituteWrittenGradingDto(attemptId, attempt.getStatus().name(), List.copyOf(answers));
    }

    @Transactional
    public InstituteWrittenGradingDto gradeWrittenAnswer(
        UUID graderUserId,
        UUID attemptId,
        UUID questionId,
        WrittenQuestionGradeRequest request
    ) {
        AssessmentAttempt attempt = ownedAttemptForUpdate(graderUserId, attemptId);
        requireClosed(attempt);
        AssessmentQuestion question = attempt.getAssessment().getQuestions().stream()
            .filter(item -> item.getId().equals(questionId))
            .findFirst()
            .orElseThrow(() -> notFound("Question does not belong to this attempt"));
        if (question.getQuestionType() != AssessmentQuestionType.WRITTEN_RESPONSE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only written questions can be graded manually");
        }
        if (request.awardedMarks().compareTo(BigDecimal.ZERO) < 0
            || request.awardedMarks().compareTo(question.getPoints()) > 0) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Awarded marks cannot exceed the question's maximum marks");
        }
        UserAccount grader = authorizeGrader(graderUserId);
        gradeRepository.save(new AssessmentQuestionGrade(
            attempt,
            question,
            grader,
            request.awardedMarks(),
            request.feedback(),
            clock.instant(),
            gradeRepository.countByAttempt_Id(attemptId) + 1));
        return getWrittenAnswers(graderUserId, attemptId);
    }

    @Transactional
    public AssessmentResultDto finalizeGrading(UUID graderUserId, UUID attemptId) {
        AssessmentAttempt attempt = ownedAttemptForUpdate(graderUserId, attemptId);
        requireClosed(attempt);
        List<AssessmentQuestion> questions = attempt.getAssessment().getQuestions();
        long gradingRevision = gradeRepository.countByAttempt_Id(attemptId);
        AssessmentResultRecord existingFinal = resultRepository
            .findFirstByAttempt_IdAndFinalResultTrueOrderByGradingRevisionDesc(attemptId).orElse(null);
        if (existingFinal != null && existingFinal.getGradingRevision() == gradingRevision) {
            Instant releasedAt = releaseRepository.findByAttempt_IdAndResult_Id(attemptId, existingFinal.getId())
                .map(AssessmentResultRelease::getReleasedAt).orElse(null);
            return toDto(attempt, existingFinal, releasedAt);
        }
        BigDecimal maximumMarks = BigDecimal.ZERO;
        BigDecimal awardedMarks = BigDecimal.ZERO;
        Map<UUID, AssessmentAttemptAnswer> answers = new HashMap<>();
        answerRepository.findByAttempt_IdOrderByQuestion_DisplayOrderAsc(attemptId)
            .forEach(answer -> answers.put(answer.getQuestion().getId(), answer));

        Set<UUID> missingGrades = new HashSet<>();
        for (AssessmentQuestion question : questions) {
            maximumMarks = maximumMarks.add(question.getPoints());
            if (question.getQuestionType() == AssessmentQuestionType.MULTIPLE_CHOICE) {
                AssessmentAttemptAnswer answer = answers.get(question.getId());
                if (answer != null && answer.getSelectedOption() != null
                    && answer.getSelectedOption().isCorrect()) {
                    awardedMarks = awardedMarks.add(question.getPoints());
                }
            } else {
                AssessmentQuestionGrade grade = gradeRepository
                    .findFirstByAttempt_IdAndQuestion_IdOrderByGradeRevisionDesc(attemptId, question.getId())
                    .orElse(null);
                if (grade == null) {
                    missingGrades.add(question.getId());
                } else {
                    awardedMarks = awardedMarks.add(grade.getAwardedMarks());
                }
            }
        }
        if (!missingGrades.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT, "Every written question must be graded before finalization");
        }
        if (maximumMarks.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Assessment has no possible marks");
        }
        BigDecimal percentage = awardedMarks.multiply(new BigDecimal("100"))
            .divide(maximumMarks, 2, RoundingMode.HALF_UP);
        boolean passed = percentage.compareTo(attempt.getAssessment().getPassingScore()) >= 0;
        AssessmentResultRecord finalResult = resultRepository.save(new AssessmentResultRecord(
            attempt,
            awardedMarks,
            maximumMarks,
            percentage,
            passed,
            clock.instant(),
            true,
            gradingRevision));
        return toDto(attempt, finalResult, null);
    }

    @Transactional
    public AssessmentResultDto releaseResult(UUID releaserUserId, UUID attemptId) {
        AssessmentAttempt attempt = ownedAttemptForUpdate(releaserUserId, attemptId);
        AssessmentResultRecord finalResult = resultRepository
            .findFirstByAttempt_IdAndFinalResultTrueOrderByGradingRevisionDesc(attemptId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.CONFLICT, "A complete final result must exist before release"));
        AssessmentResultRelease priorRelease = releaseRepository
            .findByAttempt_IdAndResult_Id(attemptId, finalResult.getId())
            .orElse(null);
        if (priorRelease != null) {
            markTrainedAfterPassingFinal(attempt, finalResult, priorRelease.getReleasedAt());
            return toDto(attempt, finalResult, priorRelease.getReleasedAt());
        }
        UserAccount releaser = authorizeGrader(releaserUserId);
        Instant releasedAt = clock.instant();
        releaseRepository.save(new AssessmentResultRelease(
            attempt,
            finalResult,
            releaser,
            releasedAt,
            Math.toIntExact(releaseRepository.countByAttempt_Id(attemptId) + 1)));
        markTrainedAfterPassingFinal(attempt, finalResult, releasedAt);
        return toDto(attempt, finalResult, releasedAt);
    }

    private void markTrainedAfterPassingFinal(
        AssessmentAttempt attempt,
        AssessmentResultRecord result,
        Instant assessedAt
    ) {
        if (attempt.getAssessment().getWeekNumber() != null || !result.isPassed()) {
            return;
        }
        var enrollment = attempt.getEnrollment();
        enrollment.setStatus(EnrollmentStatus.COMPLETED);
        enrollment.setAssessmentScore(result.getScorePercent());
        enrollment.setPassed(true);
        enrollment.setAssessmentNote(attempt.getAssessment().getTitle());
        enrollment.setAssessedAt(assessedAt);
        enrollmentRepository.save(enrollment);

        Representative representative = representativeRepository.findById(enrollment.getRepresentativeId())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Representative profile not found for this enrollment"));
        representative.setStatus(RepresentativeStatus.TRAINED);
        representativeRepository.save(representative);
    }

    @Transactional(readOnly = true)
    public AssessmentResultDto getInstituteResult(UUID viewerUserId, UUID attemptId) {
        AssessmentAttempt attempt = ownedAttempt(viewerUserId, attemptId);
        AssessmentResultRecord result = resultRepository
            .findFirstByAttempt_IdAndFinalResultTrueOrderByGradingRevisionDesc(attemptId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Final result not found"));
        Instant releasedAt = releaseRepository.findByAttempt_IdAndResult_Id(attemptId, result.getId())
            .map(AssessmentResultRelease::getReleasedAt).orElse(null);
        return toDto(attempt, result, releasedAt);
    }

    @Transactional(readOnly = true)
    public AssessmentResultDto getReleasedCandidateResult(UUID representativeUserId, UUID attemptId) {
        AssessmentAttempt attempt = attemptRepository.findById(attemptId)
            .orElseThrow(() -> notFound("Assessment attempt not found"));
        UserAccount user = userRepository.findById(representativeUserId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
        if (user.getRole() != UserRole.REPRESENTATIVE) {
            throw forbidden("Representative access is required");
        }
        Representative representative = representativeRepository.findByUserId(representativeUserId)
            .orElseThrow(() -> forbidden("Representative profile is not available"));
        if (!attempt.getEnrollment().getRepresentativeId().equals(representative.getId())) {
            throw notFound("Assessment attempt not found");
        }
        AssessmentResultRelease release = releaseRepository.findFirstByAttempt_IdOrderByReleaseNumberDesc(attemptId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Result has not been released"));
        return toDto(attempt, release.getResult(), release.getReleasedAt());
    }

    private AssessmentResultDto toDto(
        AssessmentAttempt attempt,
        AssessmentResultRecord result,
        Instant releasedAt
    ) {
        Map<UUID, AssessmentQuestionGrade> gradesForResult = new HashMap<>();
        gradeRepository.findByAttempt_IdOrderByGradeRevisionDesc(attempt.getId()).stream()
            .filter(grade -> grade.getGradeRevision() <= result.getGradingRevision())
            .forEach(grade -> gradesForResult.putIfAbsent(grade.getQuestion().getId(), grade));
        List<AssessmentResultDto.QuestionFeedbackDto> writtenFeedback = releasedAt == null
            ? List.of()
            : attempt.getAssessment().getQuestions().stream()
                .filter(question -> question.getQuestionType() == AssessmentQuestionType.WRITTEN_RESPONSE)
                .map(question -> {
                    AssessmentQuestionGrade grade = gradesForResult.get(question.getId());
                    return grade == null ? null : new AssessmentResultDto.QuestionFeedbackDto(
                        question.getId(),
                        question.getPrompt(),
                        grade.getAwardedMarks(),
                        question.getPoints(),
                        grade.getFeedback());
                })
                .filter(java.util.Objects::nonNull)
                .toList();
        return new AssessmentResultDto(
            attempt.getId(),
            attempt.getAttemptNumber(),
            result.getPointsEarned(),
            result.getTotalPoints(),
            result.getScorePercent(),
            result.isPassed(),
            result.getEvaluatedAt(),
            releasedAt,
            writtenFeedback);
    }

    private AssessmentAttempt ownedAttempt(UUID actorUserId, UUID attemptId) {
        AssessmentAttempt attempt = attemptRepository.findById(attemptId)
            .orElseThrow(() -> notFound("Assessment attempt not found"));
        authorizeTrainingOwnership(actorUserId, attempt);
        return attempt;
    }

    private AssessmentAttempt ownedAttemptForUpdate(UUID actorUserId, UUID attemptId) {
        AssessmentAttempt attempt = attemptRepository.findByIdForUpdate(attemptId)
            .orElseThrow(() -> notFound("Assessment attempt not found"));
        authorizeTrainingOwnership(actorUserId, attempt);
        return attempt;
    }

    private void authorizeTrainingOwnership(UUID actorUserId, AssessmentAttempt attempt) {
        UserAccount actor = authorizeGrader(actorUserId);
        UUID trainingId = attempt.getAssessment().getTraining().getId();
        if (!ownsTraining(actor, trainingId)) {
            throw forbidden("You can only grade assessments belonging to your institute");
        }
    }

    private UserAccount authorizeGrader(UUID userId) {
        UserAccount user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
        if (user.getRole() != UserRole.TRAINING_INSTITUTE && user.getRole() != UserRole.SYSTEM_ADMIN) {
            throw forbidden("Institute assessor access is required");
        }
        return user;
    }

    private boolean ownsTraining(UserAccount actor, UUID trainingId) {
        if (actor.getRole() == UserRole.SYSTEM_ADMIN) {
            return true;
        }
        UUID instituteId = instituteRepository.findByUserId(actor.getId())
            .map(institute -> institute.getId())
            .orElse(null);
        if (instituteId == null) {
            return false;
        }
        return trainingRepository.findById(trainingId)
            .map(Training::getTrainingInstituteProfileId)
            .filter(instituteId::equals)
            .isPresent();
    }

    private void requireClosed(AssessmentAttempt attempt) {
        if (attempt.getStatus() == AssessmentAttemptStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An active attempt cannot be graded");
        }
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }

    private ResponseStatusException forbidden(String message) {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, message);
    }
}
