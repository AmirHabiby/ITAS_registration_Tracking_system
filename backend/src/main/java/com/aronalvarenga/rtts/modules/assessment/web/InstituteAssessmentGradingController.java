package com.aronalvarenga.rtts.modules.assessment.web;

import com.aronalvarenga.rtts.identity.domain.UserAccountRepository;
import com.aronalvarenga.rtts.modules.assessment.application.AssessmentGradingService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/institutes/me")
@PreAuthorize("hasAnyRole('SYSTEM_ADMIN','TRAINING_INSTITUTE')")
@Transactional(readOnly = true)
public class InstituteAssessmentGradingController {

    private final AssessmentGradingService gradingService;
    private final UserAccountRepository userRepository;

    public InstituteAssessmentGradingController(
        AssessmentGradingService gradingService,
        UserAccountRepository userRepository
    ) {
        this.gradingService = gradingService;
        this.userRepository = userRepository;
    }

    @GetMapping("/online-assessments/grading-queue")
    public List<InstituteGradingQueueItemDto> gradingQueue(@AuthenticationPrincipal Jwt jwt) {
        return gradingService.gradingQueue(actorId(jwt));
    }

    @GetMapping("/online-assessments/completed-attempts")
    public List<InstituteCompletedAttemptDto> completedAttempts(@AuthenticationPrincipal Jwt jwt) {
        return gradingService.completedAttempts(actorId(jwt));
    }

    @GetMapping("/online-assessment-attempts/{attemptId}/written-grading")
    public InstituteWrittenGradingDto getWrittenAnswers(
        @PathVariable UUID attemptId,
        @AuthenticationPrincipal Jwt jwt
    ) {
        return gradingService.getWrittenAnswers(actorId(jwt), attemptId);
    }

    @PostMapping("/online-assessment-attempts/{attemptId}/written-answers/{questionId}/grades")
    @Transactional
    public InstituteWrittenGradingDto gradeWrittenAnswer(
        @PathVariable UUID attemptId,
        @PathVariable UUID questionId,
        @Valid @RequestBody WrittenQuestionGradeRequest request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        return gradingService.gradeWrittenAnswer(actorId(jwt), attemptId, questionId, request);
    }

    @PostMapping("/online-assessment-attempts/{attemptId}/finalize-grading")
    @Transactional
    public AssessmentResultDto finalizeGrading(
        @PathVariable UUID attemptId,
        @AuthenticationPrincipal Jwt jwt
    ) {
        return gradingService.finalizeGrading(actorId(jwt), attemptId);
    }

    @PostMapping("/online-assessment-attempts/{attemptId}/release-result")
    @Transactional
    @ResponseStatus(HttpStatus.OK)
    public AssessmentResultDto releaseResult(
        @PathVariable UUID attemptId,
        @AuthenticationPrincipal Jwt jwt
    ) {
        return gradingService.releaseResult(actorId(jwt), attemptId);
    }

    @GetMapping("/online-assessment-attempts/{attemptId}/result")
    public AssessmentResultDto getResult(
        @PathVariable UUID attemptId,
        @AuthenticationPrincipal Jwt jwt
    ) {
        return gradingService.getInstituteResult(actorId(jwt), attemptId);
    }

    private UUID actorId(Jwt jwt) {
        return userRepository.findByUsername(jwt.getSubject())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"))
            .getId();
    }
}
