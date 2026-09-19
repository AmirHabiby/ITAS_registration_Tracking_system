package com.aronalvarenga.rtts.modules.dashboard.application;

import com.aronalvarenga.rtts.identity.domain.UserAccountRepository;
import com.aronalvarenga.rtts.modules.agentdelegation.domain.AgentDelegationRepository;
import com.aronalvarenga.rtts.modules.agentdelegation.domain.AgentDelegationStatus;
import com.aronalvarenga.rtts.modules.agentdelegation.domain.FirmAgentAssignmentRepository;
import com.aronalvarenga.rtts.modules.agentdelegation.domain.FirmDelegationRepository;
import com.aronalvarenga.rtts.modules.agentdelegation.domain.FirmRepository;
import com.aronalvarenga.rtts.modules.firm.domain.FirmAdminProfileRepository;
import com.aronalvarenga.rtts.modules.firm.web.FirmDashboardResponseDto;
import com.aronalvarenga.rtts.modules.assessment.domain.AssessmentResultRepository;
import com.aronalvarenga.rtts.modules.dashboard.domain.AuditLogRepository;
import com.aronalvarenga.rtts.modules.dashboard.web.AdminDashboardResponseDto;
import com.aronalvarenga.rtts.modules.dashboard.web.DelegatorDashboardResponseDto;
import com.aronalvarenga.rtts.modules.dashboard.web.InstituteDashboardResponseDto;
import com.aronalvarenga.rtts.modules.dashboard.web.RepresentativeDashboardResponseDto;
import com.aronalvarenga.rtts.modules.enrollments.domain.EnrollmentRepository;
import com.aronalvarenga.rtts.modules.enrollments.domain.EnrollmentStatus;
import com.aronalvarenga.rtts.modules.representatives.domain.RepresentativeRepository;
import com.aronalvarenga.rtts.modules.representatives.domain.RepresentativeStatus;
import com.aronalvarenga.rtts.modules.requests.domain.TrainingRequestRepository;
import com.aronalvarenga.rtts.modules.requests.domain.TrainingRequestStatus;
import com.aronalvarenga.rtts.modules.trainings.domain.TrainingRepository;
import org.springframework.http.HttpStatus;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DashboardService {

    private final UserAccountRepository userAccountRepository;
    private final RepresentativeRepository representativeRepository;
    private final TrainingRepository trainingRepository;
    private final TrainingRequestRepository trainingRequestRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AuditLogRepository auditLogRepository;
    private final AgentDelegationRepository agentDelegationRepository;
    private final FirmAdminProfileRepository firmAdminProfileRepository;
    private final FirmAgentAssignmentRepository firmAgentAssignmentRepository;
    private final FirmDelegationRepository firmDelegationRepository;
    private final FirmRepository firmRepository;

    public DashboardService(
        UserAccountRepository userAccountRepository,
        RepresentativeRepository representativeRepository,
        TrainingRepository trainingRepository,
        TrainingRequestRepository trainingRequestRepository,
        EnrollmentRepository enrollmentRepository,
        AuditLogRepository auditLogRepository,
        AgentDelegationRepository agentDelegationRepository,
        FirmAdminProfileRepository firmAdminProfileRepository,
        FirmAgentAssignmentRepository firmAgentAssignmentRepository,
        FirmDelegationRepository firmDelegationRepository,
        FirmRepository firmRepository
    ) {
        this.userAccountRepository = userAccountRepository;
        this.representativeRepository = representativeRepository;
        this.trainingRepository = trainingRepository;
        this.trainingRequestRepository = trainingRequestRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.auditLogRepository = auditLogRepository;
        this.agentDelegationRepository = agentDelegationRepository;
        this.firmAdminProfileRepository = firmAdminProfileRepository;
        this.firmAgentAssignmentRepository = firmAgentAssignmentRepository;
        this.firmDelegationRepository = firmDelegationRepository;
        this.firmRepository = firmRepository;
    }

    @Transactional(readOnly = true)
    public AdminDashboardResponseDto adminDashboard() {
        return new AdminDashboardResponseDto(
            representativeRepository.count(),
            userAccountRepository.countByRole(com.aronalvarenga.rtts.identity.domain.UserRole.DELEGATOR),
            userAccountRepository.countByRole(com.aronalvarenga.rtts.identity.domain.UserRole.TRAINING_INSTITUTE),
            trainingRepository.count(),
            representativeRepository.countByStatus(RepresentativeStatus.TRAINED),
            agentDelegationRepository.countByStatus(AgentDelegationStatus.ACTIVE),
            trainingRequestRepository.countByStatus(TrainingRequestStatus.PENDING),
            trainingRequestRepository.countByStatus(TrainingRequestStatus.APPROVED),
            trainingRequestRepository.countByStatus(TrainingRequestStatus.REJECTED)
        );
    }

    @Transactional(readOnly = true)
    public DelegatorDashboardResponseDto delegatorDashboard() {
        return new DelegatorDashboardResponseDto(
            trainingRequestRepository.countByStatus(TrainingRequestStatus.PENDING),
            trainingRequestRepository.countByStatus(TrainingRequestStatus.APPROVED),
            trainingRequestRepository.countByStatus(TrainingRequestStatus.REJECTED),
            representativeRepository.countByStatus(RepresentativeStatus.TRAINED),
            auditLogRepository.count()
        );
    }

    @Transactional(readOnly = true)
    public InstituteDashboardResponseDto instituteDashboard() {
        return new InstituteDashboardResponseDto(
            trainingRepository.countByActiveTrue(),
            enrollmentRepository.count(),
            enrollmentRepository.countByStatus(EnrollmentStatus.COMPLETED),
            enrollmentRepository.countByStatus(EnrollmentStatus.FAILED),
            enrollmentRepository.countByStatus(EnrollmentStatus.RETAKE_REQUIRED)
        );
    }

    @Transactional(readOnly = true)
    public RepresentativeDashboardResponseDto representativeDashboard(UUID userId) {
        var representative = representativeRepository.findByUserId(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Representative not found"))
            ;
        UUID representativeId = representative.getId();
        return new RepresentativeDashboardResponseDto(
            trainingRepository.countByActiveTrue(),
            trainingRequestRepository.findAll().stream().filter(request -> request.getRepresentativeId().equals(representativeId) && request.getStatus() == TrainingRequestStatus.PENDING).count(),
            trainingRequestRepository.findAll().stream().filter(request -> request.getRepresentativeId().equals(representativeId) && request.getStatus() == TrainingRequestStatus.APPROVED).count(),
            enrollmentRepository.findByRepresentativeIdOrderByAssessedAtDesc(representativeId).stream().filter(enrollment -> enrollment.getStatus() == EnrollmentStatus.COMPLETED).count(),
            representative.getStatus(),
            representative.getFirmId() == null ? null : firmRepository.findById(representative.getFirmId()).map(firm -> firm.getName()).orElse(null)
        );
    }

    @Transactional(readOnly = true)
    public FirmDashboardResponseDto firmAdminDashboard(UUID userId) {
        UUID firmId = firmAdminProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Firm admin profile not found"))
            .getFirmId();
        long total = representativeRepository.countByFirmId(firmId);
        long trained = representativeRepository.countByFirmIdAndStatus(firmId, RepresentativeStatus.TRAINED);
        long inTraining = representativeRepository.countByFirmIdAndStatus(firmId, RepresentativeStatus.IN_TRAINING);
        long assigned = representativeRepository.findByFirmIdOrderByFullNameAsc(firmId).stream()
            .filter(rep -> firmAgentAssignmentRepository.findByRepresentativeProfileIdAndRevokedAtIsNull(rep.getId()).isPresent())
            .count();
        return new FirmDashboardResponseDto(total, trained, inTraining, assigned, trained - assigned,
            firmDelegationRepository.findFirstByFirmIdAndRevokedAtIsNullOrderByDelegatedAtDesc(firmId).isPresent());
    }
}