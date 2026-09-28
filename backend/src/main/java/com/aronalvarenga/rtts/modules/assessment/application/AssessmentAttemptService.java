package com.aronalvarenga.rtts.modules.assessment.application;

import com.aronalvarenga.rtts.identity.domain.UserAccount;
import com.aronalvarenga.rtts.identity.domain.UserAccountRepository;
import com.aronalvarenga.rtts.identity.domain.UserRole;
import com.aronalvarenga.rtts.modules.assessment.domain.AssessmentAttempt;
import com.aronalvarenga.rtts.modules.assessment.domain.AssessmentAttemptAnswer;
import com.aronalvarenga.rtts.modules.assessment.domain.AssessmentAttemptAnswerRepository;
import com.aronalvarenga.rtts.modules.assessment.domain.AssessmentAttemptRepository;
import com.aronalvarenga.rtts.modules.assessment.domain.AssessmentAttemptStatus;
import com.aronalvarenga.rtts.modules.assessment.domain.AssessmentOption;
import com.aronalvarenga.rtts.modules.assessment.domain.AssessmentOptionRepository;
import com.aronalvarenga.rtts.modules.assessment.domain.AssessmentQuestion;
import com.aronalvarenga.rtts.modules.assessment.domain.AssessmentQuestionType;
import com.aronalvarenga.rtts.modules.assessment.domain.AssessmentResultRecord;
import com.aronalvarenga.rtts.modules.assessment.domain.AssessmentResultRecordRepository;
import com.aronalvarenga.rtts.modules.assessment.domain.AssessmentResultRelease;
import com.aronalvarenga.rtts.modules.assessment.domain.AssessmentResultReleaseRepository;
import com.aronalvarenga.rtts.modules.assessment.domain.AssessmentStatus;
import com.aronalvarenga.rtts.modules.assessment.domain.AttemptOptionSnapshot;
import com.aronalvarenga.rtts.modules.assessment.domain.AttemptOptionSnapshotRepository;
import com.aronalvarenga.rtts.modules.assessment.domain.AttemptQuestionSnapshot;
import com.aronalvarenga.rtts.modules.assessment.domain.AttemptQuestionSnapshotRepository;
import com.aronalvarenga.rtts.modules.assessment.domain.OnlineAssessment;
import com.aronalvarenga.rtts.modules.assessment.domain.OnlineAssessmentRepository;
import com.aronalvarenga.rtts.modules.assessment.web.AssessmentAnswerRequest;
import com.aronalvarenga.rtts.modules.assessment.web.AssessmentResultDto;
import com.aronalvarenga.rtts.modules.assessment.web.CandidateAssessmentAvailabilityDto;
import com.aronalvarenga.rtts.modules.assessment.web.CandidateAttemptHistoryDto;
import com.aronalvarenga.rtts.modules.assessment.web.CandidateAttemptDto;
import com.aronalvarenga.rtts.modules.enrollments.domain.Enrollment;
import com.aronalvarenga.rtts.modules.enrollments.domain.EnrollmentRepository;
import com.aronalvarenga.rtts.modules.enrollments.domain.EnrollmentStatus;
import com.aronalvarenga.rtts.modules.representatives.domain.Representative;
import com.aronalvarenga.rtts.modules.representatives.domain.RepresentativeRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AssessmentAttemptService {

    private static final List<EnrollmentStatus> ELIGIBLE_ENROLLMENT_STATUSES =
        List.of(EnrollmentStatus.ENROLLED, EnrollmentStatus.ONGOING, EnrollmentStatus.RETAKE_REQUIRED);

    private final OnlineAssessmentRepository assessmentRepository;
    private final AssessmentAttemptRepository attemptRepository;
    private final AssessmentAttemptAnswerRepository answerRepository;
    private final AssessmentOptionRepository optionRepository;
    private final AssessmentResultRecordRepository resultRepository;
    private final AssessmentResultReleaseRepository releaseRepository;
    private final AttemptQuestionSnapshotRepository questionSnapshotRepository;
    private final AttemptOptionSnapshotRepository optionSnapshotRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final RepresentativeRepository representativeRepository;
    private final UserAccountRepository userAccountRepository;
    private final Clock clock;
    private final SecureRandom secureRandom = new SecureRandom();

    public AssessmentAttemptService(
        OnlineAssessmentRepository assessmentRepository,
        AssessmentAttemptRepository attemptRepository,
        AssessmentAttemptAnswerRepository answerRepository,
        AssessmentOptionRepository optionRepository,
        AssessmentResultRecordRepository resultRepository,
        AssessmentResultReleaseRepository releaseRepository,
        AttemptQuestionSnapshotRepository questionSnapshotRepository,
        AttemptOptionSnapshotRepository optionSnapshotRepository,
        EnrollmentRepository enrollmentRepository,
        RepresentativeRepository representativeRepository,
        UserAccountRepository userAccountRepository,
        Clock clock
    ) {
        this.assessmentRepository = assessmentRepository;
        this.attemptRepository = attemptRepository;
        this.answerRepository = answerRepository;
        this.optionRepository = optionRepository;
        this.resultRepository = resultRepository;
        this.releaseRepository = releaseRepository;
        this.questionSnapshotRepository = questionSnapshotRepository;
        this.optionSnapshotRepository = optionSnapshotRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.representativeRepository = representativeRepository;
        this.userAccountRepository = userAccountRepository;
        this.clock = clock;
    }

    @Transactional
    public List<CandidateAssessmentAvailabilityDto> listEligible(UUID userId) {
        Representative representative = getRepresentative(userId);
        List<Enrollment> enrollments = enrollmentRepository
            .findByRepresentativeIdAndStatusInOrderByAssessedAtDesc(
                representative.getId(), ELIGIBLE_ENROLLMENT_STATUSES);
        if (enrollments.isEmpty()) {
            return List.of();
        }
        List<UUID> trainingIds = enrollments.stream().map(Enrollment::getTrainingId).distinct().toList();
        Instant now = clock.instant();
        List<CandidateAssessmentAvailabilityDto> eligible = new ArrayList<>();
        for (OnlineAssessment assessment : assessmentRepository
            .findByTraining_IdInAndStatusOrderByCreatedAtDesc(trainingIds, AssessmentStatus.PUBLISHED)) {
            if (!isAvailable(assessment, now)) {
                continue;
            }
            Enrollment enrollment = enrollmentRepository
                .findByRepresentativeIdAndTrainingId(representative.getId(), assessment.getTraining().getId())
                .orElseThrow();
            expireActiveIfPastDeadline(assessment.getId(), enrollment.getId(), now);
            AssessmentAttempt active = attemptRepository
                .findFirstByAssessment_IdAndEnrollment_IdAndStatus(
                    assessment.getId(), enrollment.getId(), AssessmentAttemptStatus.IN_PROGRESS)
                .orElse(null);
            long attemptsUsed = attemptRepository.countByAssessment_IdAndEnrollment_Id(
                assessment.getId(), enrollment.getId());
            if (active == null && attemptsUsed >= assessment.getAttemptLimit()) {
                continue;
            }
            eligible.add(new CandidateAssessmentAvailabilityDto(
                assessment.getId(),
                assessment.getTraining().getId(),
                assessment.getTitle(),
                assessment.getInstructions(),
                assessment.getVersionNumber(),
                assessment.getDurationMinutes(),
                assessment.getAttemptLimit(),
                attemptsUsed,
                active == null ? null : active.getId(),
                assessment.getAvailableFrom(),
                assessment.getAvailableUntil()));
        }
        return List.copyOf(eligible);
    }

    @Transactional(readOnly = true)
    public List<CandidateAttemptHistoryDto> listHistory(UUID userId) {
        Representative representative = getRepresentative(userId);
        List<CandidateAttemptHistoryDto> history = new ArrayList<>();
        for (Enrollment enrollment : enrollmentRepository.findByRepresentativeIdOrderByAssessedAtDesc(
            representative.getId())) {
            for (AssessmentAttempt attempt : attemptRepository.findByEnrollment_IdOrderByStartedAtDesc(enrollment.getId())) {
                AssessmentResultDto releasedResult = releaseRepository
                    .findFirstByAttempt_IdOrderByReleaseNumberDesc(attempt.getId())
                    .map(release -> toHistoryResult(attempt, release))
                    .orElse(null);
                history.add(new CandidateAttemptHistoryDto(
                    attempt.getId(),
                    attempt.getAssessment().getId(),
                    attempt.getTrainingId(),
                    attempt.getAssessment().getTraining().getTitle(),
                    attempt.getAssessment().getTitle(),
                    attempt.getAssessment().getVersionNumber(),
                    attempt.getAttemptNumber(),
                    attempt.getStatus().name(),
                    attempt.getStartedAt(),
                    attempt.getSubmittedAt(),
                    attempt.getExpiredAt(),
                    releasedResult));
            }
        }
        return history.stream()
            .sorted(java.util.Comparator.comparing(CandidateAttemptHistoryDto::startedAt).reversed())
            .toList();
    }

    private AssessmentResultDto toHistoryResult(AssessmentAttempt attempt, AssessmentResultRelease release) {
        AssessmentResultRecord result = release.getResult();
        return new AssessmentResultDto(
            attempt.getId(),
            attempt.getAttemptNumber(),
            result.getPointsEarned(),
            result.getTotalPoints(),
            result.getScorePercent(),
            result.isPassed(),
            result.getEvaluatedAt(),
            release.getReleasedAt(),
            List.of());
    }

    @Transactional
    public CandidateAttemptDto start(UUID userId, UUID assessmentId) {
        Representative representative = getRepresentative(userId);
        OnlineAssessment assessment = assessmentRepository.findByIdForUpdate(assessmentId)
            .orElseThrow(() -> notFound("Assessment not found"));
        if (assessment.getStatus() != AssessmentStatus.PUBLISHED) {
            throw notFound("Assessment not found");
        }
        Enrollment enrollment = enrollmentRepository
            .findByRepresentativeIdAndTrainingId(representative.getId(), assessment.getTraining().getId())
            .map(found -> enrollmentRepository.findByIdForUpdate(found.getId()).orElseThrow())
            .orElseThrow(() -> forbidden("You are not enrolled in this training"));
        requireEligible(enrollment);

        Instant now = clock.instant();
        expireActiveIfPastDeadline(assessmentId, enrollment.getId(), now);
        AssessmentAttempt active = attemptRepository
            .findFirstByAssessment_IdAndEnrollment_IdAndStatus(
                assessmentId, enrollment.getId(), AssessmentAttemptStatus.IN_PROGRESS)
            .orElse(null);
        if (active != null) {
            return toDto(active, now);
        }
        if (!isAvailable(assessment, now)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Assessment is not currently available");
        }

        long usedAttempts = attemptRepository.countByAssessment_IdAndEnrollment_Id(assessmentId, enrollment.getId());
        if (usedAttempts >= assessment.getAttemptLimit()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Assessment attempt limit has been reached");
        }

        AssessmentAttempt attempt = attemptRepository.saveAndFlush(
            new AssessmentAttempt(assessment, enrollment, Math.toIntExact(usedAttempts + 1), now));
        persistOrderSnapshot(attempt, assessment);
        return toDto(attempt, now);
    }

    @Transactional
    public CandidateAttemptDto resume(UUID userId, UUID attemptId) {
        AssessmentAttempt attempt = ownedAttemptForUpdate(userId, attemptId);
        expireIfPastDeadline(attempt, clock.instant());
        return toDto(attempt, clock.instant());
    }

    @Transactional
    public CandidateAttemptDto saveAnswer(
        UUID userId,
        UUID attemptId,
        UUID questionId,
        AssessmentAnswerRequest request
    ) {
        AssessmentAttempt attempt = ownedAttemptForUpdate(userId, attemptId);
        Instant now = clock.instant();
        expireIfPastDeadline(attempt, now);
        if (attempt.getStatus() == AssessmentAttemptStatus.EXPIRED) {
            return toDto(attempt, now);
        }
        requireInProgress(attempt);

        AttemptQuestionSnapshot questionSnapshot = questionSnapshotRepository
            .findByAttempt_IdOrderByDisplayOrderAsc(attemptId).stream()
            .filter(snapshot -> snapshot.getQuestion().getId().equals(questionId))
            .findFirst()
            .orElseThrow(() -> notFound("Question is not part of this attempt"));
        AssessmentQuestion question = questionSnapshot.getQuestion();
        AssessmentOption selectedOption = validateAnswerShape(attemptId, question, request);

        AssessmentAttemptAnswer answer = answerRepository
            .findByAttempt_IdAndQuestion_Id(attemptId, questionId)
            .orElseGet(() -> new AssessmentAttemptAnswer(attempt, question, null));
        answer.update(selectedOption, request.responseText(), now);
        answerRepository.save(answer);
        return toDto(attempt, now);
    }

    @Transactional
    public CandidateAttemptDto submit(UUID userId, UUID attemptId) {
        AssessmentAttempt attempt = ownedAttemptForUpdate(userId, attemptId);
        Instant now = clock.instant();
        expireIfPastDeadline(attempt, now);
        if (attempt.getStatus() != AssessmentAttemptStatus.IN_PROGRESS) {
            return toDto(attempt, now);
        }
        attempt.submit(now);
        attemptRepository.save(attempt);
        recordObjectiveResult(attempt, now);
        return toDto(attempt, now);
    }

    @Transactional
    public int expireDueAttempts() {
        Instant now = clock.instant();
        List<AssessmentAttempt> dueAttempts = attemptRepository
            .findByStatusAndDeadlineAtLessThanEqual(AssessmentAttemptStatus.IN_PROGRESS, now);
        int expired = 0;
        for (AssessmentAttempt due : dueAttempts) {
            AssessmentAttempt locked = attemptRepository.findByIdForUpdate(due.getId()).orElse(null);
            if (locked != null && expireIfPastDeadline(locked, now)) {
                expired++;
            }
        }
        return expired;
    }

    private void persistOrderSnapshot(AssessmentAttempt attempt, OnlineAssessment assessment) {
        List<AssessmentQuestion> questions = new ArrayList<>(assessment.getQuestions());
        if (assessment.isRandomizeQuestions()) {
            Collections.shuffle(questions, secureRandom);
        }
        List<AttemptQuestionSnapshot> questionSnapshots = new ArrayList<>();
        List<AttemptOptionSnapshot> optionSnapshots = new ArrayList<>();
        int questionOrder = 1;
        for (AssessmentQuestion question : questions) {
            questionSnapshots.add(new AttemptQuestionSnapshot(attempt, question, questionOrder++));
            List<com.aronalvarenga.rtts.modules.assessment.domain.AssessmentOption> options =
                new ArrayList<>(question.getOptions());
            if (assessment.isRandomizeOptions()) {
                Collections.shuffle(options, secureRandom);
            }
            int optionOrder = 1;
            for (AssessmentOption option : options) {
                optionSnapshots.add(new AttemptOptionSnapshot(attempt, question, option, optionOrder++));
            }
        }
        questionSnapshotRepository.saveAll(questionSnapshots);
        optionSnapshotRepository.saveAll(optionSnapshots);
    }

    private AssessmentOption validateAnswerShape(
        UUID attemptId,
        AssessmentQuestion question,
        AssessmentAnswerRequest request
    ) {
        if (question.getQuestionType() == AssessmentQuestionType.MULTIPLE_CHOICE) {
            if (request.responseText() != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "MCQ answers must not include written text");
            }
            if (request.selectedOptionId() == null) {
                return null;
            }
            boolean belongsToSnapshot = optionSnapshotRepository
                .findByAttempt_IdAndQuestion_IdOrderByDisplayOrderAsc(attemptId, question.getId()).stream()
                .anyMatch(snapshot -> snapshot.getOption().getId().equals(request.selectedOptionId()));
            if (!belongsToSnapshot) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected option is not part of this question");
            }
            return optionRepository.findById(request.selectedOptionId())
                .orElseThrow(() -> notFound("Selected option not found"));
        }
        if (request.selectedOptionId() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Written answers must not include an MCQ option");
        }
        return null;
    }

    private AssessmentAttempt ownedAttemptForUpdate(UUID userId, UUID attemptId) {
        Representative representative = getRepresentative(userId);
        AssessmentAttempt attempt = attemptRepository.findByIdForUpdate(attemptId)
            .orElseThrow(() -> notFound("Assessment attempt not found"));
        if (!attempt.getEnrollment().getRepresentativeId().equals(representative.getId())) {
            throw notFound("Assessment attempt not found");
        }
        return attempt;
    }

    private Representative getRepresentative(UUID userId) {
        UserAccount user = userAccountRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
        if (user.getRole() != UserRole.REPRESENTATIVE) {
            throw forbidden("Representative access is required");
        }
        return representativeRepository.findByUserId(userId)
            .orElseThrow(() -> forbidden("Representative profile is not available"));
    }

    private void requireEligible(Enrollment enrollment) {
        if (!ELIGIBLE_ENROLLMENT_STATUSES.contains(enrollment.getStatus())) {
            throw forbidden("Enrollment is not eligible to take this assessment");
        }
    }

    private boolean isAvailable(OnlineAssessment assessment, Instant now) {
        return (assessment.getAvailableFrom() == null || !now.isBefore(assessment.getAvailableFrom()))
            && (assessment.getAvailableUntil() == null || now.isBefore(assessment.getAvailableUntil()));
    }

    private void expireActiveIfPastDeadline(UUID assessmentId, UUID enrollmentId, Instant now) {
        attemptRepository.findFirstByAssessment_IdAndEnrollment_IdAndStatus(
            assessmentId, enrollmentId, AssessmentAttemptStatus.IN_PROGRESS)
            .ifPresent(attempt -> attemptRepository.findByIdForUpdate(attempt.getId())
                .ifPresent(lockedAttempt -> expireIfPastDeadline(lockedAttempt, now)));
    }

    private boolean expireIfPastDeadline(AssessmentAttempt attempt, Instant now) {
        if (attempt.getStatus() == AssessmentAttemptStatus.IN_PROGRESS && !now.isBefore(attempt.getDeadlineAt())) {
            attempt.expire(now);
            attemptRepository.save(attempt);
            recordObjectiveResult(attempt, now);
            return true;
        }
        return false;
    }

    private void recordObjectiveResult(AssessmentAttempt attempt, Instant evaluatedAt) {
        List<AssessmentQuestion> questions = attempt.getAssessment().getQuestions();
        List<AssessmentQuestion> objectiveQuestions = questions.stream()
            .filter(q -> q.getQuestionType() == AssessmentQuestionType.MULTIPLE_CHOICE)
            .toList();
        if (objectiveQuestions.isEmpty()) {
            return;
        }
        Map<UUID, AssessmentAttemptAnswer> savedAnswers = new HashMap<>();
        answerRepository.findByAttempt_IdOrderByQuestion_DisplayOrderAsc(attempt.getId())
            .forEach(answer -> savedAnswers.put(answer.getQuestion().getId(), answer));
        BigDecimal totalPoints = BigDecimal.ZERO;
        BigDecimal earnedPoints = BigDecimal.ZERO;
        for (AssessmentQuestion question : objectiveQuestions) {
            totalPoints = totalPoints.add(question.getPoints());
            AssessmentAttemptAnswer answer = savedAnswers.get(question.getId());
            if (answer != null && answer.getSelectedOption() != null && answer.getSelectedOption().isCorrect()) {
                earnedPoints = earnedPoints.add(question.getPoints());
            }
        }
        BigDecimal percent = earnedPoints.multiply(new BigDecimal("100"))
            .divide(totalPoints, 2, RoundingMode.HALF_UP);
        resultRepository.save(new AssessmentResultRecord(
            attempt,
            earnedPoints,
            totalPoints,
            percent,
            percent.compareTo(attempt.getAssessment().getPassingScore()) >= 0,
            evaluatedAt,
            objectiveQuestions.size() == questions.size()));
    }

    private void requireInProgress(AssessmentAttempt attempt) {
        if (attempt.getStatus() != AssessmentAttemptStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Assessment attempt is no longer active");
        }
    }

    private CandidateAttemptDto toDto(AssessmentAttempt attempt, Instant now) {
        Map<UUID, AssessmentAttemptAnswer> answers = new HashMap<>();
        answerRepository.findByAttempt_IdOrderByQuestion_DisplayOrderAsc(attempt.getId())
            .forEach(answer -> answers.put(answer.getQuestion().getId(), answer));
        List<CandidateAttemptDto.QuestionDto> questions = questionSnapshotRepository
            .findByAttempt_IdOrderByDisplayOrderAsc(attempt.getId()).stream()
            .map(questionSnapshot -> {
                AssessmentQuestion question = questionSnapshot.getQuestion();
                AssessmentAttemptAnswer answer = answers.get(question.getId());
                List<CandidateAttemptDto.OptionDto> options = optionSnapshotRepository
                    .findByAttempt_IdAndQuestion_IdOrderByDisplayOrderAsc(attempt.getId(), question.getId())
                    .stream()
                    .map(optionSnapshot -> new CandidateAttemptDto.OptionDto(
                        optionSnapshot.getOption().getId(),
                        optionSnapshot.getOption().getText(),
                        optionSnapshot.getDisplayOrder()))
                    .toList();
                return new CandidateAttemptDto.QuestionDto(
                    question.getId(),
                    question.getPrompt(),
                    question.getQuestionType().name(),
                    question.getPoints(),
                    questionSnapshot.getDisplayOrder(),
                    options,
                    answer == null || answer.getSelectedOption() == null
                        ? null : answer.getSelectedOption().getId(),
                    answer == null ? null : answer.getResponseText());
            })
            .toList();
        long secondsRemaining = attempt.getStatus() == AssessmentAttemptStatus.IN_PROGRESS
            ? Math.max(0, Duration.between(now, attempt.getDeadlineAt()).toSeconds())
            : 0;
        return new CandidateAttemptDto(
            attempt.getId(),
            attempt.getAssessment().getId(),
            attempt.getAssessment().getTitle(),
            attempt.getAssessment().getVersionNumber(),
            attempt.getStatus().name(),
            attempt.getStartedAt(),
            attempt.getDeadlineAt(),
            attempt.getSubmittedAt(),
            attempt.getExpiredAt(),
            secondsRemaining,
            questions);
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }

    private ResponseStatusException forbidden(String message) {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, message);
    }
}
