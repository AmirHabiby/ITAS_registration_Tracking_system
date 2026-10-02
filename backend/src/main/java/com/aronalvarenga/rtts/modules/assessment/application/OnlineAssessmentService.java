package com.aronalvarenga.rtts.modules.assessment.application;

import com.aronalvarenga.rtts.identity.domain.UserAccount;
import com.aronalvarenga.rtts.identity.domain.UserAccountRepository;
import com.aronalvarenga.rtts.identity.domain.UserRole;
import com.aronalvarenga.rtts.modules.assessment.domain.AssessmentAttemptRepository;
import com.aronalvarenga.rtts.modules.assessment.domain.AssessmentOption;
import com.aronalvarenga.rtts.modules.assessment.domain.AssessmentOptionRepository;
import com.aronalvarenga.rtts.modules.assessment.domain.AssessmentQuestion;
import com.aronalvarenga.rtts.modules.assessment.domain.AssessmentQuestionRepository;
import com.aronalvarenga.rtts.modules.assessment.domain.AssessmentQuestionType;
import com.aronalvarenga.rtts.modules.assessment.domain.AssessmentStatus;
import com.aronalvarenga.rtts.modules.assessment.domain.OnlineAssessment;
import com.aronalvarenga.rtts.modules.assessment.domain.OnlineAssessmentRepository;
import com.aronalvarenga.rtts.modules.institutes.domain.TrainingInstituteRepository;
import com.aronalvarenga.rtts.modules.trainings.domain.Training;
import com.aronalvarenga.rtts.modules.trainings.domain.TrainingRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.HashSet;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OnlineAssessmentService {

    private final OnlineAssessmentRepository assessmentRepository;
    private final AssessmentQuestionRepository questionRepository;
    private final AssessmentOptionRepository optionRepository;
    private final AssessmentAttemptRepository attemptRepository;
    private final TrainingRepository trainingRepository;
    private final UserAccountRepository userAccountRepository;
    private final TrainingInstituteRepository trainingInstituteRepository;
    private final EntityManager entityManager;

    public OnlineAssessmentService(
        OnlineAssessmentRepository assessmentRepository,
        AssessmentQuestionRepository questionRepository,
        AssessmentOptionRepository optionRepository,
        AssessmentAttemptRepository attemptRepository,
        TrainingRepository trainingRepository,
        UserAccountRepository userAccountRepository,
        TrainingInstituteRepository trainingInstituteRepository,
        EntityManager entityManager
    ) {
        this.assessmentRepository = assessmentRepository;
        this.questionRepository = questionRepository;
        this.optionRepository = optionRepository;
        this.attemptRepository = attemptRepository;
        this.trainingRepository = trainingRepository;
        this.userAccountRepository = userAccountRepository;
        this.trainingInstituteRepository = trainingInstituteRepository;
        this.entityManager = entityManager;
    }

    @Transactional
    public OnlineAssessment createDraft(
        UUID actorUserId,
        UUID trainingId,
        String title,
        String instructions,
        BigDecimal passingScore
    ) {
        return createDraft(actorUserId, trainingId, title, instructions, passingScore, null,
            60, 1, null, null, false, false);
    }

    @Transactional
    public OnlineAssessment createDraft(
        UUID actorUserId,
        UUID trainingId,
        String title,
        String instructions,
        BigDecimal passingScore,
        int durationMinutes,
        int attemptLimit,
        Instant availableFrom,
        Instant availableUntil,
        boolean randomizeQuestions,
        boolean randomizeOptions
    ) {
        return createDraft(actorUserId, trainingId, title, instructions, passingScore, null,
            durationMinutes, attemptLimit, availableFrom, availableUntil, randomizeQuestions, randomizeOptions);
    }

    @Transactional
    public OnlineAssessment createDraft(
        UUID actorUserId,
        UUID trainingId,
        String title,
        String instructions,
        BigDecimal passingScore,
        Integer weekNumber,
        int durationMinutes,
        int attemptLimit,
        Instant availableFrom,
        Instant availableUntil,
        boolean randomizeQuestions,
        boolean randomizeOptions
    ) {
        UserAccount actor = getUser(actorUserId);
        Training training = getTraining(trainingId);
        authorizeTrainingOwner(actor, training);
        validateAssessment(title, instructions, passingScore);
        validateConfiguration(durationMinutes, attemptLimit, availableFrom, availableUntil);

        OnlineAssessment assessment = new OnlineAssessment(
            UUID.randomUUID(),
            1,
            training,
            actor,
            title.trim(),
            instructions,
            passingScore,
            weekNumber);
        assessment.configure(durationMinutes, attemptLimit, availableFrom, availableUntil, randomizeQuestions, randomizeOptions);
        return assessmentRepository.save(assessment);
    }

    @Transactional
    public OnlineAssessment updateDraft(
        UUID actorUserId,
        UUID assessmentId,
        String title,
        String instructions,
        BigDecimal passingScore,
        Integer weekNumber,
        int durationMinutes,
        int attemptLimit,
        Instant availableFrom,
        Instant availableUntil,
        boolean randomizeQuestions,
        boolean randomizeOptions
    ) {
        OnlineAssessment assessment = getAssessment(assessmentId);
        authorizeTrainingOwner(getUser(actorUserId), assessment.getTraining());
        requireDraft(assessment);
        validateAssessment(title, instructions, passingScore);
        validateConfiguration(durationMinutes, attemptLimit, availableFrom, availableUntil);
        assessment.updateDetails(title.trim(), instructions, passingScore, weekNumber);
        assessment.configure(durationMinutes, attemptLimit, availableFrom, availableUntil, randomizeQuestions, randomizeOptions);
        return assessmentRepository.save(assessment);
    }

    @Transactional
    public OnlineAssessment updateDraft(
        UUID actorUserId,
        UUID assessmentId,
        String title,
        String instructions,
        BigDecimal passingScore,
        int durationMinutes,
        int attemptLimit,
        Instant availableFrom,
        Instant availableUntil,
        boolean randomizeQuestions,
        boolean randomizeOptions
    ) {
        OnlineAssessment assessment = getAssessment(assessmentId);
        return updateDraft(actorUserId, assessmentId, title, instructions, passingScore,
            assessment.getWeekNumber(), durationMinutes, attemptLimit, availableFrom, availableUntil,
            randomizeQuestions, randomizeOptions);
    }

    @Transactional
    public OnlineAssessment updateDraftDetails(
        UUID actorUserId,
        UUID assessmentId,
        String title,
        String instructions,
        BigDecimal passingScore
    ) {
        OnlineAssessment assessment = getAssessment(assessmentId);
        return updateDraft(
            actorUserId,
            assessmentId,
            title,
            instructions,
            passingScore,
            assessment.getWeekNumber(),
            assessment.getDurationMinutes(),
            assessment.getAttemptLimit(),
            assessment.getAvailableFrom(),
            assessment.getAvailableUntil(),
            assessment.isRandomizeQuestions(),
            assessment.isRandomizeOptions());
    }

    @Transactional
    public AssessmentQuestion addQuestion(
        UUID actorUserId,
        UUID assessmentId,
        String prompt,
        int displayOrder,
        BigDecimal points
    ) {
        return addQuestion(actorUserId, assessmentId, prompt, AssessmentQuestionType.MULTIPLE_CHOICE,
            displayOrder, points, null);
    }

    @Transactional
    public AssessmentQuestion addQuestion(
        UUID actorUserId,
        UUID assessmentId,
        String prompt,
        AssessmentQuestionType type,
        int displayOrder,
        BigDecimal points,
        String gradingRubric
    ) {
        OnlineAssessment assessment = getAssessment(assessmentId);
        authorizeTrainingOwner(getUser(actorUserId), assessment.getTraining());
        requireDraft(assessment);
        validateQuestion(prompt, type, displayOrder, points, gradingRubric);
        List<AssessmentQuestion> existingQuestions = questionRepository.findByAssessment_IdOrderByDisplayOrderAsc(assessmentId);
        if (displayOrder > existingQuestions.size() + 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Question order must be within the current question count");
        }
        AssessmentQuestion question = new AssessmentQuestion(
            assessment, prompt.trim(), type, existingQuestions.size() + 1, points, gradingRubric);
        assessment.addQuestion(question);
        questionRepository.saveAndFlush(question);
        List<UUID> orderedIds = new java.util.ArrayList<>(
            existingQuestions.stream().map(AssessmentQuestion::getId).toList());
        orderedIds.add(displayOrder - 1, question.getId());
        saveQuestionOrder(assessmentId, orderedIds);
        return questionRepository.findByIdAndAssessment_Id(question.getId(), assessmentId).orElseThrow();
    }

    @Transactional
    public AssessmentQuestion updateQuestion(
        UUID actorUserId,
        UUID assessmentId,
        UUID questionId,
        String prompt,
        AssessmentQuestionType type,
        int displayOrder,
        BigDecimal points,
        String gradingRubric
    ) {
        OnlineAssessment assessment = getAssessment(assessmentId);
        authorizeTrainingOwner(getUser(actorUserId), assessment.getTraining());
        requireDraft(assessment);
        AssessmentQuestion question = questionRepository.findByIdAndAssessment_Id(questionId, assessmentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assessment question not found"));
        validateQuestion(prompt, type, displayOrder, points, gradingRubric);
        if (type == AssessmentQuestionType.WRITTEN_RESPONSE && !question.getOptions().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Delete MCQ options before changing the question to written response");
        }
        List<AssessmentQuestion> orderedQuestions = questionRepository.findByAssessment_IdOrderByDisplayOrderAsc(assessmentId);
        if (displayOrder > orderedQuestions.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Question order must be within the current question count");
        }
        List<UUID> orderedIds = orderedQuestions.stream()
            .map(AssessmentQuestion::getId)
            .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
        orderedIds.remove(questionId);
        orderedIds.add(displayOrder - 1, questionId);
        question.updateContent(prompt.trim(), type, question.getDisplayOrder(), points, gradingRubric);
        questionRepository.save(question);
        saveQuestionOrder(assessmentId, orderedIds);
        return questionRepository.findByIdAndAssessment_Id(questionId, assessmentId).orElseThrow();
    }

    @Transactional
    public void deleteQuestion(UUID actorUserId, UUID assessmentId, UUID questionId) {
        OnlineAssessment assessment = getAssessment(assessmentId);
        authorizeTrainingOwner(getUser(actorUserId), assessment.getTraining());
        requireDraft(assessment);
        if (questionRepository.findByIdAndAssessment_Id(questionId, assessmentId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Assessment question not found");
        }
        questionRepository.deleteByIdAndAssessment_Id(questionId, assessmentId);
    }

    @Transactional
    public List<AssessmentQuestion> reorderQuestions(UUID actorUserId, UUID assessmentId, List<UUID> orderedIds) {
        OnlineAssessment assessment = getAssessment(assessmentId);
        authorizeTrainingOwner(getUser(actorUserId), assessment.getTraining());
        requireDraft(assessment);
        List<AssessmentQuestion> current = questionRepository.findByAssessment_IdOrderByDisplayOrderAsc(assessmentId);
        if (orderedIds == null || orderedIds.size() != current.size()
            || new HashSet<>(orderedIds).size() != orderedIds.size()
            || !new HashSet<>(orderedIds).equals(current.stream().map(AssessmentQuestion::getId).collect(java.util.stream.Collectors.toSet()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Question order must include every question exactly once");
        }

        return saveQuestionOrder(assessmentId, orderedIds);
    }

    private List<AssessmentQuestion> saveQuestionOrder(UUID assessmentId, List<UUID> orderedIds) {
        questionRepository.moveOrdersOutOfRange(assessmentId);
        entityManager.flush();
        entityManager.clear();
        for (int index = 0; index < orderedIds.size(); index++) {
            AssessmentQuestion question = questionRepository.findByIdAndAssessment_Id(orderedIds.get(index), assessmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assessment question not found"));
            question.updateContent(question.getPrompt(), question.getQuestionType(), index + 1,
                question.getPoints(), question.getGradingRubric());
            questionRepository.saveAndFlush(question);
        }
        return questionRepository.findByAssessment_IdOrderByDisplayOrderAsc(assessmentId);
    }

    private List<AssessmentOption> saveOptionOrder(UUID questionId, List<UUID> orderedIds) {
        optionRepository.moveOrdersOutOfRange(questionId);
        entityManager.flush();
        entityManager.clear();
        for (int index = 0; index < orderedIds.size(); index++) {
            AssessmentOption option = optionRepository.findById(orderedIds.get(index))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assessment option not found"));
            option.updateContent(option.getText(), index + 1, option.isCorrect());
            optionRepository.saveAndFlush(option);
        }
        return optionRepository.findByQuestion_IdOrderByDisplayOrderAsc(questionId);
    }

    @Transactional
    public AssessmentOption addOption(
        UUID actorUserId,
        UUID assessmentId,
        UUID questionId,
        String text,
        int displayOrder,
        boolean correct
    ) {
        AssessmentQuestion question = getQuestion(actorUserId, assessmentId, questionId);
        requireDraft(question.getAssessment());
        if (question.getQuestionType() != AssessmentQuestionType.MULTIPLE_CHOICE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Options can only be added to multiple-choice questions");
        }
        validateOption(text, displayOrder);
        List<AssessmentOption> existingOptions = optionRepository.findByQuestion_IdOrderByDisplayOrderAsc(questionId);
        if (displayOrder > existingOptions.size() + 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Option order must be within the current option count");
        }
        AssessmentOption option = new AssessmentOption(question, text.trim(), existingOptions.size() + 1, correct);
        question.addOption(option);
        optionRepository.saveAndFlush(option);
        List<UUID> orderedIds = new java.util.ArrayList<>(
            existingOptions.stream().map(AssessmentOption::getId).toList());
        orderedIds.add(displayOrder - 1, option.getId());
        saveOptionOrder(questionId, orderedIds);
        return optionRepository.findById(option.getId()).orElseThrow();
    }

    @Transactional
    public AssessmentOption updateOption(
        UUID actorUserId,
        UUID assessmentId,
        UUID questionId,
        UUID optionId,
        String text,
        int displayOrder,
        boolean correct
    ) {
        AssessmentQuestion question = getQuestion(actorUserId, assessmentId, questionId);
        requireDraft(question.getAssessment());
        if (question.getQuestionType() != AssessmentQuestionType.MULTIPLE_CHOICE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Options can only be updated for multiple-choice questions");
        }
        AssessmentOption option = optionRepository.findById(optionId)
            .filter(candidate -> candidate.getQuestion().getId().equals(questionId))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assessment option not found"));
        validateOption(text, displayOrder);
        List<AssessmentOption> currentOptions = optionRepository.findByQuestion_IdOrderByDisplayOrderAsc(questionId);
        if (displayOrder > currentOptions.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Option order must be within the current option count");
        }
        List<UUID> orderedIds = currentOptions.stream()
            .map(AssessmentOption::getId)
            .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
        orderedIds.remove(optionId);
        orderedIds.add(displayOrder - 1, optionId);
        option.updateContent(text.trim(), option.getDisplayOrder(), correct);
        optionRepository.save(option);
        saveOptionOrder(questionId, orderedIds);
        return optionRepository.findById(optionId).orElseThrow();
    }

    @Transactional
    public void deleteOption(UUID actorUserId, UUID assessmentId, UUID questionId, UUID optionId) {
        AssessmentQuestion question = getQuestion(actorUserId, assessmentId, questionId);
        requireDraft(question.getAssessment());
        if (optionRepository.findById(optionId).filter(option -> option.getQuestion().getId().equals(questionId)).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Assessment option not found");
        }
        optionRepository.deleteByIdAndQuestion_Id(optionId, questionId);
    }

    @Transactional(readOnly = true)
    public List<OnlineAssessment> listForTraining(UUID actorUserId, UUID trainingId) {
        Training training = getTraining(trainingId);
        authorizeTrainingOwner(getUser(actorUserId), training);
        return assessmentRepository.findByTraining_IdOrderByCreatedAtDesc(trainingId);
    }

    @Transactional(readOnly = true)
    public OnlineAssessment getForInstitute(UUID actorUserId, UUID assessmentId) {
        OnlineAssessment assessment = getAssessment(assessmentId);
        authorizeTrainingOwner(getUser(actorUserId), assessment.getTraining());
        return assessment;
    }

    @Transactional(readOnly = true)
    public List<OnlineAssessment> listVersions(UUID actorUserId, UUID assessmentId) {
        OnlineAssessment assessment = getAssessment(assessmentId);
        authorizeTrainingOwner(getUser(actorUserId), assessment.getTraining());
        return assessmentRepository.findByAssessmentSeriesIdOrderByVersionNumberDesc(assessment.getAssessmentSeriesId());
    }

    @Transactional
    public OnlineAssessment publish(UUID actorUserId, UUID assessmentId) {
        OnlineAssessment assessment = getAssessment(assessmentId);
        authorizeTrainingOwner(getUser(actorUserId), assessment.getTraining());
        requireDraft(assessment);
        try {
            assessment.publish();
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }
        return assessmentRepository.save(assessment);
    }

    @Transactional
    public OnlineAssessment createRevision(UUID actorUserId, UUID publishedAssessmentId) {
        UserAccount actor = getUser(actorUserId);
        OnlineAssessment source = getAssessment(publishedAssessmentId);
        authorizeTrainingOwner(actor, source.getTraining());
        if (source.getStatus() != AssessmentStatus.PUBLISHED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only a published assessment can be revised");
        }
        if (assessmentRepository.existsByAssessmentSeriesIdAndStatus(
            source.getAssessmentSeriesId(),
            AssessmentStatus.DRAFT)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A draft revision already exists for this assessment");
        }

        int nextVersion = assessmentRepository
            .findTopByAssessmentSeriesIdOrderByVersionNumberDesc(source.getAssessmentSeriesId())
            .orElseThrow()
            .getVersionNumber() + 1;
        OnlineAssessment revision = new OnlineAssessment(
            source.getAssessmentSeriesId(), nextVersion, source.getTraining(), actor, source.getTitle(),
            source.getInstructions(), source.getPassingScore(), source.getWeekNumber());
        revision.configure(source.getDurationMinutes(), source.getAttemptLimit(), source.getAvailableFrom(),
            source.getAvailableUntil(), source.isRandomizeQuestions(), source.isRandomizeOptions());

        for (AssessmentQuestion sourceQuestion : questionRepository
            .findByAssessment_IdOrderByDisplayOrderAsc(source.getId())) {
            AssessmentQuestion copiedQuestion = new AssessmentQuestion(
                revision, sourceQuestion.getPrompt(), sourceQuestion.getQuestionType(),
                sourceQuestion.getDisplayOrder(), sourceQuestion.getPoints(), sourceQuestion.getGradingRubric());
            revision.addQuestion(copiedQuestion);
            if (sourceQuestion.getQuestionType() == AssessmentQuestionType.MULTIPLE_CHOICE) {
                for (AssessmentOption sourceOption : optionRepository
                    .findByQuestion_IdOrderByDisplayOrderAsc(sourceQuestion.getId())) {
                    copiedQuestion.addOption(new AssessmentOption(
                        copiedQuestion, sourceOption.getText(), sourceOption.getDisplayOrder(), sourceOption.isCorrect()));
                }
            }
        }
        return assessmentRepository.save(revision);
    }

    @Transactional
    public void deleteDraft(UUID actorUserId, UUID assessmentId) {
        OnlineAssessment assessment = getAssessment(assessmentId);
        authorizeTrainingOwner(getUser(actorUserId), assessment.getTraining());
        requireDraft(assessment);
        if (attemptRepository.countByAssessment_Id(assessmentId) != 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An assessment with attempts cannot be deleted");
        }
        assessmentRepository.delete(assessment);
    }

    private AssessmentQuestion getQuestion(UUID actorUserId, UUID assessmentId, UUID questionId) {
        OnlineAssessment assessment = getAssessment(assessmentId);
        authorizeTrainingOwner(getUser(actorUserId), assessment.getTraining());
        return questionRepository.findByIdAndAssessment_Id(questionId, assessmentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assessment question not found"));
    }

    private OnlineAssessment getAssessment(UUID assessmentId) {
        return assessmentRepository.findById(assessmentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Online assessment not found"));
    }

    private void requireDraft(OnlineAssessment assessment) {
        if (assessment.getStatus() != AssessmentStatus.DRAFT) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Published assessment versions are immutable; create a revision to make changes");
        }
    }

    private Training getTraining(UUID trainingId) {
        return trainingRepository.findById(trainingId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Training not found"));
    }

    private UserAccount getUser(UUID userId) {
        return userAccountRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
    }

    private void authorizeTrainingOwner(UserAccount actor, Training training) {
        if (actor.getRole() == UserRole.SYSTEM_ADMIN) {
            return;
        }
        boolean ownsTraining = actor.getRole() == UserRole.TRAINING_INSTITUTE
            && trainingInstituteRepository.findByUserId(actor.getId())
                .map(institute -> institute.getId().equals(training.getTrainingInstituteProfileId()))
                .orElse(false);
        if (!ownsTraining) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only manage assessments for your own trainings");
        }
    }

    private void validateAssessment(String title, String instructions, BigDecimal passingScore) {
        if (title == null || title.isBlank() || title.length() > 180
            || (instructions != null && instructions.length() > 2000)
            || passingScore == null || passingScore.scale() > 2
            || passingScore.compareTo(BigDecimal.ZERO) < 0
            || passingScore.compareTo(new BigDecimal("100.00")) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid title, instructions, or pass percentage");
        }
    }

    private void validateConfiguration(int duration, int attempts, Instant from, Instant until) {
        if (duration < 1 || duration > 1440 || attempts < 1 || attempts > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Duration must be 1-1440 minutes and attempt limit 1-100");
        }
        if (from != null && until != null && !until.isAfter(from)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Availability end must be after availability start");
        }
    }

    private void validateQuestion(
        String prompt,
        AssessmentQuestionType type,
        int order,
        BigDecimal points,
        String rubric
    ) {
        if (prompt == null || prompt.isBlank() || prompt.length() > 4000 || type == null
            || order < 1 || order > 1000 || !validPoints(points)
            || (rubric != null && rubric.length() > 4000)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid question content, order, points, or rubric");
        }
        if (type == AssessmentQuestionType.MULTIPLE_CHOICE && rubric != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "MCQ questions cannot have a written grading rubric");
        }
        if (type == AssessmentQuestionType.WRITTEN_RESPONSE && (rubric == null || rubric.isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Written questions require a grading rubric");
        }
    }

    private void validateOption(String text, int order) {
        if (text == null || text.isBlank() || text.length() > 2000 || order < 1 || order > 1000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid option text or display order");
        }
    }

    private boolean validPoints(BigDecimal points) {
        return points != null && points.compareTo(BigDecimal.ZERO) > 0
            && points.precision() - points.scale() <= 5 && points.scale() <= 2;
    }
}
