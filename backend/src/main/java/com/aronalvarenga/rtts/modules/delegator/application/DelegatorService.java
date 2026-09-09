package com.aronalvarenga.rtts.modules.delegator.application;

import com.aronalvarenga.rtts.identity.domain.UserAccountRepository;
import com.aronalvarenga.rtts.modules.agentdelegation.domain.AgentDelegation;
import com.aronalvarenga.rtts.modules.agentdelegation.domain.AgentDelegationRepository;
import com.aronalvarenga.rtts.modules.agentdelegation.domain.AgentDelegationStatus;
import com.aronalvarenga.rtts.modules.common.exception.BadRequestException;
import com.aronalvarenga.rtts.modules.delegator.domain.DelegatorProfileRepository;
import com.aronalvarenga.rtts.modules.delegator.web.DelegatorDecisionRequest;
import com.aronalvarenga.rtts.modules.enrollments.domain.EnrollmentRepository;
import com.aronalvarenga.rtts.modules.representatives.application.RepresentativeService;
import com.aronalvarenga.rtts.modules.representatives.domain.Representative;
import com.aronalvarenga.rtts.modules.representatives.domain.RepresentativeRepository;
import com.aronalvarenga.rtts.modules.representatives.domain.RepresentativeStatus;
import com.aronalvarenga.rtts.modules.requests.application.TrainingRequestService;
import com.aronalvarenga.rtts.modules.requests.domain.TrainingRequestEntity;
import com.aronalvarenga.rtts.modules.requests.domain.TrainingRequestRepository;
import com.aronalvarenga.rtts.modules.requests.domain.TrainingRequestStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DelegatorService {

    private final TrainingRequestRepository trainingRequestRepository;
    private final TrainingRequestService trainingRequestService;
    private final RepresentativeRepository representativeRepository;
    private final RepresentativeService representativeService;
    private final AgentDelegationRepository agentDelegationRepository;
    private final UserAccountRepository userAccountRepository;
    private final DelegatorProfileRepository delegatorProfileRepository;

    public DelegatorService(
        TrainingRequestRepository trainingRequestRepository,
        TrainingRequestService trainingRequestService,
        RepresentativeRepository representativeRepository,
        RepresentativeService representativeService,
        AgentDelegationRepository agentDelegationRepository,
        UserAccountRepository userAccountRepository,
        DelegatorProfileRepository delegatorProfileRepository
    ) {
        this.trainingRequestRepository = trainingRequestRepository;
        this.trainingRequestService = trainingRequestService;
        this.representativeRepository = representativeRepository;
        this.representativeService = representativeService;
        this.agentDelegationRepository = agentDelegationRepository;
        this.userAccountRepository = userAccountRepository;
        this.delegatorProfileRepository = delegatorProfileRepository;
    }

    @Transactional(readOnly = true)
    public List<TrainingRequestEntity> listTrainingRequests() {
        return trainingRequestRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<TrainingRequestEntity> listPendingTrainingRequests() {
        return trainingRequestRepository.findByStatusOrderByRequestedAtDesc(TrainingRequestStatus.PENDING);
    }

    @Transactional
    public TrainingRequestEntity approve(UUID requestId, Jwt jwt, DelegatorDecisionRequest request) {
        return trainingRequestService.approve(requestId, jwt, new com.aronalvarenga.rtts.modules.requests.web.ReviewRequest(request.note() == null ? "Approved" : request.note()));
    }

    @Transactional
    public TrainingRequestEntity reject(UUID requestId, Jwt jwt, DelegatorDecisionRequest request) {
        return trainingRequestService.reject(requestId, jwt, new com.aronalvarenga.rtts.modules.requests.web.ReviewRequest(request.note() == null ? "Rejected" : request.note()));
    }

    @Transactional(readOnly = true)
    public List<Representative> trainedRepresentatives() {
        return representativeRepository.findByStatusOrderByFullNameAsc(RepresentativeStatus.TRAINED);
    }

    @Transactional
    public AgentDelegation markAsAgent(UUID representativeId, Jwt jwt, DelegatorDecisionRequest request) {
        Representative representative = representativeRepository.findById(representativeId)
            .orElseThrow(() -> new BadRequestException("Representative not found"));
        if (representative.getStatus() != RepresentativeStatus.TRAINED) {
            throw new BadRequestException("Only trained representatives can be delegated as agent");
        }
        if (agentDelegationRepository.existsByRepresentativeProfileIdAndStatus(representativeId, AgentDelegationStatus.ACTIVE)) {
            throw new BadRequestException("Representative already has an active delegation");
        }
        UUID userId = userAccountRepository.findByUsername(jwt.getSubject())
            .orElseThrow(() -> new BadRequestException("Delegator not found"))
            .getId();
        UUID delegatorId = delegatorProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new BadRequestException("Delegator profile not found"))
            .getId();
        representativeService.markAgent(representativeId);
        AgentDelegation delegation = new AgentDelegation(representativeId, delegatorId, request == null ? null : request.note());
        delegation.setStatus(AgentDelegationStatus.ACTIVE);
        return agentDelegationRepository.save(delegation);
    }

    @Transactional(readOnly = true)
    public List<AgentDelegation> agents() {
        return agentDelegationRepository.findAllByOrderByDelegatedAtDesc();
    }
}