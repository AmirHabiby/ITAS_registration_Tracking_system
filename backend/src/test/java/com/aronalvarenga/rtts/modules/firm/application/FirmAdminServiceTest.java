package com.aronalvarenga.rtts.modules.firm.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import com.aronalvarenga.rtts.identity.domain.UserAccount;
import com.aronalvarenga.rtts.identity.domain.UserAccountRepository;
import com.aronalvarenga.rtts.identity.domain.UserRole;
import com.aronalvarenga.rtts.modules.agentdelegation.domain.FirmAgentAssignmentRepository;
import com.aronalvarenga.rtts.modules.agentdelegation.domain.FirmDelegationRepository;
import com.aronalvarenga.rtts.modules.firm.domain.FirmAdminProfile;
import com.aronalvarenga.rtts.modules.firm.domain.FirmAdminProfileRepository;
import com.aronalvarenga.rtts.modules.firm.web.FirmCreateStaffRequest;
import com.aronalvarenga.rtts.modules.representatives.domain.Representative;
import com.aronalvarenga.rtts.modules.representatives.domain.RepresentativeRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;

class FirmAdminServiceTest {
    @Test
    void createStaff_assignsRepresentativeRoleAndAuthenticatedFirm() {
        FirmAdminProfileRepository profiles = mock(FirmAdminProfileRepository.class);
        UserAccountRepository users = mock(UserAccountRepository.class);
        RepresentativeRepository representatives = mock(RepresentativeRepository.class);
        FirmDelegationRepository delegations = mock(FirmDelegationRepository.class);
        FirmAgentAssignmentRepository assignments = mock(FirmAgentAssignmentRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        Jwt jwt = mock(Jwt.class);
        UUID userId = UUID.randomUUID();
        UUID firmId = UUID.randomUUID();
        UserAccount admin = new UserAccount("firm-admin", "hash", UserRole.FIRM_ADMIN, "Firm Admin");
        admin.setId(userId);
        FirmAdminProfile profile = new FirmAdminProfile(userId, firmId, "Firm Admin", "admin@firm.example");
        when(jwt.getSubject()).thenReturn("firm-admin");
        when(users.findByUsername("firm-admin")).thenReturn(Optional.of(admin));
        when(profiles.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(users.findByUsername("staff")).thenReturn(Optional.empty());
        when(encoder.encode("password123")).thenReturn("encoded");
        when(users.save(any(UserAccount.class))).thenAnswer(invocation -> {
            UserAccount value = invocation.getArgument(0);
            if (value.getId() == null) value.setId(UUID.randomUUID());
            return value;
        });
        when(representatives.save(any(Representative.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FirmAdminService service = new FirmAdminService(profiles, users, representatives, delegations, assignments, encoder);
        UserAccount result = service.createStaff(jwt, new FirmCreateStaffRequest("staff", "password123", "Staff", "Staff Member", "staff@firm.example", true));

        assertEquals(UserRole.REPRESENTATIVE, result.getRole());
        ArgumentCaptor<Representative> captor = ArgumentCaptor.forClass(Representative.class);
        verify(representatives).save(captor.capture());
        Representative saved = captor.getValue();
        assertEquals(firmId, saved.getFirmId());
    }
}