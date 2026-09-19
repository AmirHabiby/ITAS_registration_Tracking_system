package com.aronalvarenga.rtts.modules.firm.application;

import com.aronalvarenga.rtts.identity.domain.UserAccount;
import com.aronalvarenga.rtts.identity.domain.UserAccountRepository;
import com.aronalvarenga.rtts.identity.domain.UserRole;
import com.aronalvarenga.rtts.modules.agentdelegation.domain.FirmAgentAssignment;
import com.aronalvarenga.rtts.modules.agentdelegation.domain.FirmAgentAssignmentRepository;
import com.aronalvarenga.rtts.modules.agentdelegation.domain.FirmDelegation;
import com.aronalvarenga.rtts.modules.agentdelegation.domain.FirmDelegationRepository;
import com.aronalvarenga.rtts.modules.common.exception.BadRequestException;
import com.aronalvarenga.rtts.modules.firm.domain.FirmAdminProfile;
import com.aronalvarenga.rtts.modules.firm.domain.FirmAdminProfileRepository;
import com.aronalvarenga.rtts.modules.firm.web.FirmCreateStaffRequest;
import com.aronalvarenga.rtts.modules.representatives.domain.Representative;
import com.aronalvarenga.rtts.modules.representatives.domain.RepresentativeRepository;
import com.aronalvarenga.rtts.modules.representatives.domain.RepresentativeStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FirmAdminService {
    private final FirmAdminProfileRepository profileRepository;
    private final UserAccountRepository userAccountRepository;
    private final RepresentativeRepository representativeRepository;
    private final FirmDelegationRepository firmDelegationRepository;
    private final FirmAgentAssignmentRepository assignmentRepository;
    private final PasswordEncoder passwordEncoder;

    public FirmAdminService(FirmAdminProfileRepository profileRepository, UserAccountRepository userAccountRepository,
        RepresentativeRepository representativeRepository, FirmDelegationRepository firmDelegationRepository,
        FirmAgentAssignmentRepository assignmentRepository, PasswordEncoder passwordEncoder) {
        this.profileRepository = profileRepository;
        this.userAccountRepository = userAccountRepository;
        this.representativeRepository = representativeRepository;
        this.firmDelegationRepository = firmDelegationRepository;
        this.assignmentRepository = assignmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public UUID firmId(Jwt jwt) {
        UUID userId = userAccountRepository.findByUsername(jwt.getSubject())
            .orElseThrow(() -> new BadRequestException("User not found")).getId();
        return profileRepository.findByUserId(userId)
            .orElseThrow(() -> new BadRequestException("Firm admin profile not found")).getFirmId();
    }

    @Transactional(readOnly = true)
    public List<Representative> staff(Jwt jwt) {
        return representativeRepository.findByFirmIdOrderByFullNameAsc(firmId(jwt));
    }

    @Transactional
    public UserAccount createStaff(Jwt jwt, FirmCreateStaffRequest request) {
        if (userAccountRepository.findByUsername(request.username()).isPresent()) {
            throw new BadRequestException("Username already exists");
        }
        UserAccount user = userAccountRepository.save(new UserAccount(request.username(), passwordEncoder.encode(request.password()), UserRole.REPRESENTATIVE, request.displayName()));
        user.setEnabled(request.enabled());
        user = userAccountRepository.save(user);
        Representative representative = new Representative(request.fullName(), request.email());
        representative.setUserId(user.getId());
        representative.setFirmId(firmId(jwt));
        representativeRepository.save(representative);
        return user;
    }

    @Transactional(readOnly = true)
    public FirmDelegation activeDelegation(Jwt jwt) {
        return firmDelegationRepository.findFirstByFirmIdAndRevokedAtIsNullOrderByDelegatedAtDesc(firmId(jwt)).orElse(null);
    }

    @Transactional
    public FirmAgentAssignment assign(Jwt jwt, UUID representativeId, String reason) {
        UUID firmId = firmId(jwt);
        Representative representative = representativeRepository.findById(representativeId)
            .orElseThrow(() -> new BadRequestException("Representative not found"));
        if (!firmId.equals(representative.getFirmId())) throw new BadRequestException("Representative does not belong to your firm");
        if (representative.getStatus() != RepresentativeStatus.TRAINED) throw new BadRequestException("Only trained representatives can be assigned");
        FirmDelegation delegation = activeDelegation(jwt);
        if (delegation == null) throw new BadRequestException("Firm has no active delegation");
        if (assignmentRepository.findByRepresentativeProfileIdAndRevokedAtIsNull(representativeId).isPresent()) {
            throw new BadRequestException("Representative already has an active assignment");
        }
        UUID userId = userAccountRepository.findByUsername(jwt.getSubject())
            .orElseThrow(() -> new BadRequestException("User not found")).getId();
        return assignmentRepository.save(new FirmAgentAssignment(delegation.getId(), representativeId, userId, reason));
    }

    @Transactional
    public FirmAgentAssignment revokeAssignment(Jwt jwt, UUID assignmentId) {
        FirmAgentAssignment assignment = assignmentRepository.findById(assignmentId)
            .orElseThrow(() -> new BadRequestException("Assignment not found"));
        representativeRepository.findById(assignment.getRepresentativeProfileId())
            .filter(rep -> firmId(jwt).equals(rep.getFirmId()))
            .orElseThrow(() -> new BadRequestException("Assignment does not belong to your firm"));
        assignment.setRevokedAt(Instant.now());
        return assignmentRepository.save(assignment);
    }
}