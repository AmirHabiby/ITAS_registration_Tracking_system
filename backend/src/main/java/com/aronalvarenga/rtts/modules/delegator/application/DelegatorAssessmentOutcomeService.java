package com.aronalvarenga.rtts.modules.delegator.application;

import com.aronalvarenga.rtts.identity.domain.UserAccountRepository;
import com.aronalvarenga.rtts.modules.agentdelegation.domain.AgentDelegationRepository;
import com.aronalvarenga.rtts.modules.agentdelegation.domain.AgentDelegationStatus;
import com.aronalvarenga.rtts.modules.agentdelegation.domain.FirmAgentAssignmentRepository;
import com.aronalvarenga.rtts.modules.agentdelegation.domain.FirmDelegation;
import com.aronalvarenga.rtts.modules.agentdelegation.domain.FirmDelegationRepository;
import com.aronalvarenga.rtts.modules.assessment.domain.AssessmentAttempt;
import com.aronalvarenga.rtts.modules.assessment.domain.AssessmentAttemptRepository;
import com.aronalvarenga.rtts.modules.assessment.domain.AssessmentResultRelease;
import com.aronalvarenga.rtts.modules.assessment.domain.AssessmentResultReleaseRepository;
import com.aronalvarenga.rtts.modules.delegator.domain.DelegatorProfileRepository;
import com.aronalvarenga.rtts.modules.delegator.web.DelegatorAssessmentOutcomeDto;
import com.aronalvarenga.rtts.modules.enrollments.domain.Enrollment;
import com.aronalvarenga.rtts.modules.enrollments.domain.EnrollmentRepository;
import com.aronalvarenga.rtts.modules.representatives.domain.Representative;
import com.aronalvarenga.rtts.modules.representatives.domain.RepresentativeRepository;
import com.aronalvarenga.rtts.modules.trainings.domain.TrainingRepository;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DelegatorAssessmentOutcomeService {

    private final UserAccountRepository userRepository;
    private final DelegatorProfileRepository delegatorRepository;
    private final AgentDelegationRepository delegationRepository;
    private final FirmDelegationRepository firmDelegationRepository;
    private final FirmAgentAssignmentRepository firmAssignmentRepository;
    private final RepresentativeRepository representativeRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AssessmentAttemptRepository attemptRepository;
    private final AssessmentResultReleaseRepository releaseRepository;
    private final TrainingRepository trainingRepository;

    public DelegatorAssessmentOutcomeService(
        UserAccountRepository userRepository,
        DelegatorProfileRepository delegatorRepository,
        AgentDelegationRepository delegationRepository,
        FirmDelegationRepository firmDelegationRepository,
        FirmAgentAssignmentRepository firmAssignmentRepository,
        RepresentativeRepository representativeRepository,
        EnrollmentRepository enrollmentRepository,
        AssessmentAttemptRepository attemptRepository,
        AssessmentResultReleaseRepository releaseRepository,
        TrainingRepository trainingRepository
    ) {
        this.userRepository = userRepository;
        this.delegatorRepository = delegatorRepository;
        this.delegationRepository = delegationRepository;
        this.firmDelegationRepository = firmDelegationRepository;
        this.firmAssignmentRepository = firmAssignmentRepository;
        this.representativeRepository = representativeRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.attemptRepository = attemptRepository;
        this.releaseRepository = releaseRepository;
        this.trainingRepository = trainingRepository;
    }

    @Transactional(readOnly = true)
    public List<DelegatorAssessmentOutcomeDto> listReleasedOutcomes(String username) {
        UUID userId = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"))
            .getId();
        UUID delegatorId = delegatorRepository.findByUserId(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Delegator profile is not available"))
            .getId();
        List<DelegatorAssessmentOutcomeDto> outcomes = new ArrayList<>();
        Set<UUID> authorizedRepresentatives = new LinkedHashSet<>();
        delegationRepository.findByDelegatorProfileIdOrderByDelegatedAtDesc(delegatorId).stream()
            .filter(delegation -> delegation.getStatus() == AgentDelegationStatus.ACTIVE)
            .filter(delegation -> delegation.getRevokedAt() == null)
            .map(delegation -> delegation.getRepresentativeProfileId())
            .filter(java.util.Objects::nonNull)
            .forEach(authorizedRepresentatives::add);
        for (FirmDelegation firmDelegation : firmDelegationRepository
            .findByDelegatorProfileIdOrderByDelegatedAtDesc(delegatorId)) {
            if (firmDelegation.getRevokedAt() != null) {
                continue;
            }
            firmAssignmentRepository.findByFirmDelegationIdAndRevokedAtIsNull(firmDelegation.getId()).stream()
                .map(assignment -> assignment.getRepresentativeProfileId())
                .forEach(authorizedRepresentatives::add);
        }
        authorizedRepresentatives.forEach(representativeId -> addRepresentativeOutcomes(representativeId, outcomes));
        return outcomes.stream()
            .sorted(java.util.Comparator.comparing(DelegatorAssessmentOutcomeDto::releasedAt).reversed())
            .toList();
    }

    private void addRepresentativeOutcomes(
        UUID representativeId,
        List<DelegatorAssessmentOutcomeDto> outcomes
    ) {
        Representative representative = representativeRepository.findById(representativeId).orElse(null);
        if (representative == null) {
            return;
        }
        for (Enrollment enrollment : enrollmentRepository.findByRepresentativeIdOrderByAssessedAtDesc(representativeId)) {
            for (AssessmentAttempt attempt : attemptRepository.findByEnrollment_IdOrderByStartedAtDesc(enrollment.getId())) {
                releaseRepository.findFirstByAttempt_IdOrderByReleaseNumberDesc(attempt.getId())
                    .ifPresent(release -> outcomes.add(toDto(representative, attempt, release)));
            }
        }
    }

    private DelegatorAssessmentOutcomeDto toDto(
        Representative representative,
        AssessmentAttempt attempt,
        AssessmentResultRelease release
    ) {
        var result = release.getResult();
        var training = trainingRepository.findById(attempt.getTrainingId()).orElse(null);
        return new DelegatorAssessmentOutcomeDto(
            attempt.getId(),
            representative.getId(),
            representative.getFullName(),
            attempt.getTrainingId(),
            training == null ? "Training" : training.getTitle(),
            attempt.getAssessment().getId(),
            attempt.getAssessment().getTitle(),
            attempt.getAttemptNumber(),
            result.getPointsEarned(),
            result.getTotalPoints(),
            result.getScorePercent(),
            result.isPassed(),
            release.getReleasedAt());
    }
}
