package com.aronalvarenga.rtts.modules.assessment.web;

import com.aronalvarenga.rtts.identity.domain.UserAccountRepository;
import com.aronalvarenga.rtts.modules.assessment.application.AssessmentAttemptService;
import com.aronalvarenga.rtts.modules.assessment.application.AssessmentGradingService;
import com.aronalvarenga.rtts.modules.assessment.web.AssessmentResultDto;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/representative")
@PreAuthorize("hasRole('REPRESENTATIVE')")
@Transactional(readOnly = true)
public class RepresentativeAssessmentController {

    private final AssessmentAttemptService attemptService;
    private final AssessmentGradingService gradingService;
    private final UserAccountRepository userAccountRepository;

    public RepresentativeAssessmentController(
        AssessmentAttemptService attemptService,
        AssessmentGradingService gradingService,
        UserAccountRepository userAccountRepository
    ) {
        this.attemptService = attemptService;
        this.gradingService = gradingService;
        this.userAccountRepository = userAccountRepository;
    }

    @GetMapping("/online-assessments")
    @Transactional
    public List<CandidateAssessmentAvailabilityDto> listEligible(@AuthenticationPrincipal Jwt jwt) {
        return attemptService.listEligible(actorId(jwt));
    }

    @GetMapping("/online-assessment-attempts")
    public List<CandidateAttemptHistoryDto> history(@AuthenticationPrincipal Jwt jwt) {
        return attemptService.listHistory(actorId(jwt));
    }

    @PostMapping("/online-assessments/{assessmentId}/attempts")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public CandidateAttemptDto start(
        @PathVariable UUID assessmentId,
        @AuthenticationPrincipal Jwt jwt
    ) {
        return attemptService.start(actorId(jwt), assessmentId);
    }

    @GetMapping("/online-assessment-attempts/{attemptId}")
    @Transactional
    public CandidateAttemptDto resume(
        @PathVariable UUID attemptId,
        @AuthenticationPrincipal Jwt jwt
    ) {
        return attemptService.resume(actorId(jwt), attemptId);
    }

    @PutMapping("/online-assessment-attempts/{attemptId}/answers/{questionId}")
    @Transactional
    public CandidateAttemptDto saveAnswer(
        @PathVariable UUID attemptId,
        @PathVariable UUID questionId,
        @Valid @RequestBody AssessmentAnswerRequest request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        return attemptService.saveAnswer(actorId(jwt), attemptId, questionId, request);
    }

    @PostMapping("/online-assessment-attempts/{attemptId}/submit")
    @Transactional
    public CandidateAttemptDto submit(
        @PathVariable UUID attemptId,
        @AuthenticationPrincipal Jwt jwt
    ) {
        return attemptService.submit(actorId(jwt), attemptId);
    }

    @GetMapping("/online-assessment-attempts/{attemptId}/result")
    public AssessmentResultDto getReleasedResult(
        @PathVariable UUID attemptId,
        @AuthenticationPrincipal Jwt jwt
    ) {
        return gradingService.getReleasedCandidateResult(actorId(jwt), attemptId);
    }

    private UUID actorId(Jwt jwt) {
        return userAccountRepository.findByUsername(jwt.getSubject())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"))
            .getId();
    }
}
