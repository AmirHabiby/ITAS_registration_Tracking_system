package com.aronalvarenga.rtts.modules.assessment.web;

import com.aronalvarenga.rtts.identity.domain.UserAccountRepository;
import com.aronalvarenga.rtts.modules.assessment.application.OnlineAssessmentService;
import com.aronalvarenga.rtts.modules.assessment.domain.AssessmentOption;
import com.aronalvarenga.rtts.modules.assessment.domain.AssessmentQuestion;
import com.aronalvarenga.rtts.modules.assessment.domain.OnlineAssessment;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/institutes/me")
@PreAuthorize("hasAnyRole('SYSTEM_ADMIN','TRAINING_INSTITUTE')")
@Transactional(readOnly = true)
public class InstituteOnlineAssessmentController {

    private final OnlineAssessmentService assessmentService;
    private final UserAccountRepository userAccountRepository;

    public InstituteOnlineAssessmentController(
        OnlineAssessmentService assessmentService,
        UserAccountRepository userAccountRepository
    ) {
        this.assessmentService = assessmentService;
        this.userAccountRepository = userAccountRepository;
    }

    @GetMapping("/trainings/{trainingId}/online-assessments")
    public List<InstituteAssessmentDto> list(
        @PathVariable UUID trainingId,
        @AuthenticationPrincipal Jwt jwt
    ) {
        UUID actorId = actorId(jwt);
        return assessmentService.listForTraining(actorId, trainingId).stream()
            .map(InstituteAssessmentMapper::toDto)
            .toList();
    }

    @PostMapping("/trainings/{trainingId}/online-assessments")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public InstituteAssessmentDto create(
        @PathVariable UUID trainingId,
        @Valid @RequestBody AssessmentDraftRequest request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        OnlineAssessment assessment = assessmentService.createDraft(
            actorId(jwt),
            trainingId,
            request.title(),
            request.instructions(),
            request.passingScore(),
            request.weekNumber(),
            request.durationMinutes(),
            request.attemptLimit(),
            request.availableFrom(),
            request.availableUntil(),
            request.randomizeQuestions(),
            request.randomizeOptions());
        return InstituteAssessmentMapper.toDto(assessment);
    }

    @GetMapping("/online-assessments/{assessmentId}/preview")
    public InstituteAssessmentDto preview(
        @PathVariable UUID assessmentId,
        @AuthenticationPrincipal Jwt jwt
    ) {
        return InstituteAssessmentMapper.toDto(assessmentService.getForInstitute(actorId(jwt), assessmentId));
    }

    @PutMapping("/online-assessments/{assessmentId}")
    @Transactional
    public InstituteAssessmentDto updateDraft(
        @PathVariable UUID assessmentId,
        @Valid @RequestBody AssessmentDraftRequest request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        OnlineAssessment assessment = assessmentService.updateDraft(
            actorId(jwt),
            assessmentId,
            request.title(),
            request.instructions(),
            request.passingScore(),
            request.weekNumber(),
            request.durationMinutes(),
            request.attemptLimit(),
            request.availableFrom(),
            request.availableUntil(),
            request.randomizeQuestions(),
            request.randomizeOptions());
        return InstituteAssessmentMapper.toDto(assessment);
    }

    @PostMapping("/online-assessments/{assessmentId}/questions")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public InstituteAssessmentDto addQuestion(
        @PathVariable UUID assessmentId,
        @Valid @RequestBody AssessmentQuestionRequest request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        assessmentService.addQuestion(
            actorId(jwt), assessmentId, request.prompt(), request.questionType(),
            request.displayOrder(), request.points(), request.gradingRubric());
        return preview(assessmentId, jwt);
    }

    @PutMapping("/online-assessments/{assessmentId}/questions/order")
    @Transactional
    public InstituteAssessmentDto reorderQuestions(
        @PathVariable UUID assessmentId,
        @Valid @RequestBody AssessmentQuestionOrderRequest request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        assessmentService.reorderQuestions(actorId(jwt), assessmentId, request.questionIdsInOrder());
        return preview(assessmentId, jwt);
    }

    @PutMapping("/online-assessments/{assessmentId}/questions/{questionId}")
    @Transactional
    public InstituteAssessmentDto updateQuestion(
        @PathVariable UUID assessmentId,
        @PathVariable UUID questionId,
        @Valid @RequestBody AssessmentQuestionRequest request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        assessmentService.updateQuestion(
            actorId(jwt), assessmentId, questionId, request.prompt(), request.questionType(),
            request.displayOrder(), request.points(), request.gradingRubric());
        return preview(assessmentId, jwt);
    }

    @DeleteMapping("/online-assessments/{assessmentId}/questions/{questionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void deleteQuestion(
        @PathVariable UUID assessmentId,
        @PathVariable UUID questionId,
        @AuthenticationPrincipal Jwt jwt
    ) {
        assessmentService.deleteQuestion(actorId(jwt), assessmentId, questionId);
    }

    @PostMapping("/online-assessments/{assessmentId}/questions/{questionId}/options")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public InstituteAssessmentDto addOption(
        @PathVariable UUID assessmentId,
        @PathVariable UUID questionId,
        @Valid @RequestBody AssessmentOptionRequest request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        assessmentService.addOption(
            actorId(jwt), assessmentId, questionId, request.text(), request.displayOrder(), request.correct());
        return preview(assessmentId, jwt);
    }

    @PutMapping("/online-assessments/{assessmentId}/questions/{questionId}/options/{optionId}")
    @Transactional
    public InstituteAssessmentDto updateOption(
        @PathVariable UUID assessmentId,
        @PathVariable UUID questionId,
        @PathVariable UUID optionId,
        @Valid @RequestBody AssessmentOptionRequest request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        assessmentService.updateOption(
            actorId(jwt), assessmentId, questionId, optionId,
            request.text(), request.displayOrder(), request.correct());
        return preview(assessmentId, jwt);
    }

    @DeleteMapping("/online-assessments/{assessmentId}/questions/{questionId}/options/{optionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void deleteOption(
        @PathVariable UUID assessmentId,
        @PathVariable UUID questionId,
        @PathVariable UUID optionId,
        @AuthenticationPrincipal Jwt jwt
    ) {
        assessmentService.deleteOption(actorId(jwt), assessmentId, questionId, optionId);
    }

    @PostMapping("/online-assessments/{assessmentId}/publish")
    @Transactional
    public InstituteAssessmentDto publish(
        @PathVariable UUID assessmentId,
        @AuthenticationPrincipal Jwt jwt
    ) {
        return InstituteAssessmentMapper.toDto(assessmentService.publish(actorId(jwt), assessmentId));
    }

    @GetMapping("/online-assessments/{assessmentId}/versions")
    public List<InstituteAssessmentDto> versions(
        @PathVariable UUID assessmentId,
        @AuthenticationPrincipal Jwt jwt
    ) {
        return assessmentService.listVersions(actorId(jwt), assessmentId).stream()
            .map(InstituteAssessmentMapper::toDto)
            .toList();
    }

    @PostMapping("/online-assessments/{assessmentId}/revisions")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public InstituteAssessmentDto createRevision(
        @PathVariable UUID assessmentId,
        @AuthenticationPrincipal Jwt jwt
    ) {
        return InstituteAssessmentMapper.toDto(assessmentService.createRevision(actorId(jwt), assessmentId));
    }

    @DeleteMapping("/online-assessments/{assessmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void deleteDraft(
        @PathVariable UUID assessmentId,
        @AuthenticationPrincipal Jwt jwt
    ) {
        assessmentService.deleteDraft(actorId(jwt), assessmentId);
    }

    private UUID actorId(Jwt jwt) {
        return userAccountRepository.findByUsername(jwt.getSubject())
            .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.UNAUTHORIZED, "Authenticated user not found"))
            .getId();
    }
}
