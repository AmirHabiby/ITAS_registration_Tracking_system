package com.aronalvarenga.rtts.modules.delegator.application;

import com.aronalvarenga.rtts.identity.domain.UserAccountRepository;
import com.aronalvarenga.rtts.modules.agentdelegation.domain.AgentDelegation;
import com.aronalvarenga.rtts.modules.agentdelegation.domain.AgentDelegationRepository;
import com.aronalvarenga.rtts.modules.agentdelegation.domain.AgentDelegationStatus;
import com.aronalvarenga.rtts.modules.agentdelegation.domain.Firm;
import com.aronalvarenga.rtts.modules.agentdelegation.domain.FirmDelegation;
import com.aronalvarenga.rtts.modules.agentdelegation.domain.FirmDelegationRepository;
import com.aronalvarenga.rtts.modules.agentdelegation.domain.FirmAgentAssignment;
import com.aronalvarenga.rtts.modules.agentdelegation.domain.FirmAgentAssignmentRepository;
import com.aronalvarenga.rtts.modules.agentdelegation.domain.FirmRepository;
import com.aronalvarenga.rtts.modules.common.exception.BadRequestException;
import com.aronalvarenga.rtts.modules.delegator.domain.DelegatorProfileRepository;
import com.aronalvarenga.rtts.modules.delegator.web.DelegatorDecisionRequest;
import com.aronalvarenga.rtts.modules.delegator.web.DelegatorTrainedOptionResponseDto;
import com.aronalvarenga.rtts.modules.enrollments.domain.EnrollmentRepository;
import com.aronalvarenga.rtts.modules.firm.domain.FirmAdminProfile;
import com.aronalvarenga.rtts.modules.firm.domain.FirmAdminProfileRepository;
import com.aronalvarenga.rtts.modules.representatives.application.RepresentativeService;
import com.aronalvarenga.rtts.modules.representatives.domain.Representative;
import com.aronalvarenga.rtts.modules.representatives.domain.RepresentativeRepository;
import com.aronalvarenga.rtts.modules.representatives.domain.RepresentativeStatus;
import com.aronalvarenga.rtts.modules.requests.application.TrainingRequestService;
import com.aronalvarenga.rtts.modules.requests.domain.TrainingRequestEntity;
import com.aronalvarenga.rtts.modules.requests.domain.TrainingRequestRepository;
import com.aronalvarenga.rtts.modules.requests.domain.TrainingRequestStatus;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.time.Instant;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.aronalvarenga.rtts.notifications.EmailService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final FirmRepository firmRepository;
    private final FirmDelegationRepository firmDelegationRepository;
    private final FirmAgentAssignmentRepository firmAgentAssignmentRepository;
    private final FirmAdminProfileRepository firmAdminProfileRepository;
    private final EmailService emailService;

    @Autowired
    public DelegatorService(
        TrainingRequestRepository trainingRequestRepository,
        TrainingRequestService trainingRequestService,
        RepresentativeRepository representativeRepository,
        RepresentativeService representativeService,
        AgentDelegationRepository agentDelegationRepository,
        UserAccountRepository userAccountRepository,
        DelegatorProfileRepository delegatorProfileRepository,
        FirmRepository firmRepository,
        FirmDelegationRepository firmDelegationRepository,
        FirmAgentAssignmentRepository firmAgentAssignmentRepository,
        FirmAdminProfileRepository firmAdminProfileRepository,
        EmailService emailService
    ) {
        this.trainingRequestRepository = trainingRequestRepository;
        this.trainingRequestService = trainingRequestService;
        this.representativeRepository = representativeRepository;
        this.representativeService = representativeService;
        this.agentDelegationRepository = agentDelegationRepository;
        this.userAccountRepository = userAccountRepository;
        this.delegatorProfileRepository = delegatorProfileRepository;
        this.firmRepository = firmRepository;
        this.firmDelegationRepository = firmDelegationRepository;
        this.firmAgentAssignmentRepository = firmAgentAssignmentRepository;
        this.firmAdminProfileRepository = firmAdminProfileRepository;
        this.emailService = emailService;
    }

    public DelegatorService(
        TrainingRequestRepository trainingRequestRepository,
        TrainingRequestService trainingRequestService,
        RepresentativeRepository representativeRepository,
        RepresentativeService representativeService,
        AgentDelegationRepository agentDelegationRepository,
        UserAccountRepository userAccountRepository,
        DelegatorProfileRepository delegatorProfileRepository
    ) {
        this(trainingRequestRepository, trainingRequestService, representativeRepository, representativeService,
            agentDelegationRepository, userAccountRepository, delegatorProfileRepository, null, null, null, null, null);
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

    @Transactional(readOnly = true)
    public List<DelegatorTrainedOptionResponseDto> trainedOptions() {
        List<Representative> trained = trainedRepresentatives();
        Map<UUID, Firm> firmsById = firmRepository.findAllById(
                trained.stream()
                    .map(Representative::getFirmId)
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toSet()))
            .stream()
            .collect(Collectors.toMap(Firm::getId, Function.identity()));

        List<DelegatorTrainedOptionResponseDto> options = trained.stream()
            .filter(representative -> representative.getFirmId() == null)
            .map(representative -> new DelegatorTrainedOptionResponseDto(
                representative.getId(), representative.getFullName(), representative.getEmail(),
                "REPRESENTATIVE", representative.getStatus().name(), null, 0, false))
            .collect(Collectors.toList());

        trained.stream()
            .map(Representative::getFirmId)
            .filter(java.util.Objects::nonNull)
            .distinct()
            .map(firmsById::get)
            .filter(firm -> firm != null && firm.isActive())
            .sorted(java.util.Comparator.comparing(Firm::getName))
            .forEach(firm -> options.add(new DelegatorTrainedOptionResponseDto(
                firm.getId(), firm.getName(), firm.getEmail(), "FIRM", "TRAINED",
                firm.getId(), trained.stream().filter(representative -> firm.getId().equals(representative.getFirmId())).count(),
                firmDelegationRepository.findFirstByFirmIdAndRevokedAtIsNullOrderByDelegatedAtDesc(firm.getId()).isPresent())));

        return options;
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
    public List<AgentDelegation> agents(Jwt jwt) {
        UUID userId = userAccountRepository.findByUsername(jwt.getSubject())
            .orElseThrow(() -> new BadRequestException("Delegator not found"))
            .getId();
        UUID delegatorId = delegatorProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new BadRequestException("Delegator profile not found"))
            .getId();
        return agentDelegationRepository.findByDelegatorProfileIdOrderByDelegatedAtDesc(delegatorId);
    }

    @Transactional(readOnly = true)
    public List<Firm> firms() {
        return firmRepository.findByActiveTrueOrderByNameAsc();
    }

    @Transactional
    public FirmDelegation delegateFirm(UUID firmId, Jwt jwt, DelegatorDecisionRequest request) {
        Firm firm = firmRepository.findById(firmId).filter(Firm::isActive)
            .orElseThrow(() -> new BadRequestException("Active firm not found"));
        UUID userId = userAccountRepository.findByUsername(jwt.getSubject())
            .orElseThrow(() -> new BadRequestException("Delegator not found")).getId();
        UUID delegatorId = delegatorProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new BadRequestException("Delegator profile not found")).getId();
        if (firmDelegationRepository.findFirstByFirmIdAndRevokedAtIsNullOrderByDelegatedAtDesc(firmId).isPresent()) {
            throw new BadRequestException("Firm already has an active delegation");
        }
        String taskDescription = request == null || request.note() == null || request.note().isBlank()
            ? "agent responsibilities"
            : request.note();
        FirmDelegation delegation = firmDelegationRepository.save(new FirmDelegation(firmId, delegatorId, taskDescription));
        FirmAdminProfile firmAdmin = firmAdminProfileRepository.findByFirmId(firm.getId())
            .orElseThrow(() -> new BadRequestException("Firm admin profile not found"));
        emailService.sendEmail(
            firmAdmin.getEmail(),
            "Firm delegation assigned",
            "Hello " + firmAdmin.getFullName() + "\n"
                + "Your firm has been delegated to the task " + taskDescription + ". "
                + "You're kindly required to assign one of your trained staff."
        );
        return delegation;
    }

    @Transactional
    public FirmDelegation revokeFirm(UUID firmId, Jwt jwt) {
        UUID userId = userAccountRepository.findByUsername(jwt.getSubject())
            .orElseThrow(() -> new BadRequestException("Delegator not found")).getId();
        UUID delegatorId = delegatorProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new BadRequestException("Delegator profile not found")).getId();
        FirmDelegation delegation = firmDelegationRepository.findFirstByFirmIdAndRevokedAtIsNullOrderByDelegatedAtDesc(firmId)
            .orElseThrow(() -> new BadRequestException("Active firm delegation not found"));
        if (!delegation.getDelegatorProfileId().equals(delegatorId)) throw new BadRequestException("Delegation belongs to another delegator");
        delegation.setRevokedAt(Instant.now());
        for (FirmAgentAssignment assignment : firmAgentAssignmentRepository.findByFirmDelegationIdAndRevokedAtIsNull(delegation.getId())) {
            assignment.setRevokedAt(Instant.now());
            firmAgentAssignmentRepository.save(assignment);
        }
        return firmDelegationRepository.save(delegation);
    }
}