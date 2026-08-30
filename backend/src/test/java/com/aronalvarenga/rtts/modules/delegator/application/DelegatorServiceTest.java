package com.aronalvarenga.rtts.modules.delegator.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.aronalvarenga.rtts.identity.domain.UserAccount;
import com.aronalvarenga.rtts.identity.domain.UserAccountRepository;
import com.aronalvarenga.rtts.identity.domain.UserRole;
import com.aronalvarenga.rtts.modules.agentdelegation.domain.AgentDelegation;
import com.aronalvarenga.rtts.modules.agentdelegation.domain.AgentDelegationRepository;
import com.aronalvarenga.rtts.modules.agentdelegation.domain.AgentDelegationStatus;
import com.aronalvarenga.rtts.modules.delegator.web.DelegatorDecisionRequest;
import com.aronalvarenga.rtts.modules.representatives.application.RepresentativeService;
import com.aronalvarenga.rtts.modules.representatives.domain.Representative;
import com.aronalvarenga.rtts.modules.representatives.domain.RepresentativeRepository;
import com.aronalvarenga.rtts.modules.representatives.domain.RepresentativeStatus;
import com.aronalvarenga.rtts.modules.requests.application.TrainingRequestService;
import com.aronalvarenga.rtts.modules.requests.domain.TrainingRequestRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

class DelegatorServiceTest {

    @Test
    void markAsAgent_createsActiveDelegation_forTrainedRepresentative() {
        TrainingRequestRepository trainingRequestRepository = mock(TrainingRequestRepository.class);
        TrainingRequestService trainingRequestService = mock(TrainingRequestService.class);
        RepresentativeRepository representativeRepository = mock(RepresentativeRepository.class);
        RepresentativeService representativeService = mock(RepresentativeService.class);
        AgentDelegationRepository agentDelegationRepository = mock(AgentDelegationRepository.class);
        UserAccountRepository userAccountRepository = mock(UserAccountRepository.class);

        UUID representativeId = UUID.randomUUID();
        UUID delegatorUserId = UUID.randomUUID();
        UUID delegatorProfileId = UUID.randomUUID();

        Representative representative = new Representative("Ana Silva", "ana@example.com");
        representative.setId(representativeId);
        representative.setStatus(RepresentativeStatus.TRAINED);

        UserAccount delegatorUser = new UserAccount("delegator", "hashed", UserRole.DELEGATOR, "Delegator");
        delegatorUser.setId(delegatorUserId);

        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn("delegator");
        when(representativeRepository.findById(representativeId)).thenReturn(Optional.of(representative));
        when(userAccountRepository.findByUsername("delegator")).thenReturn(Optional.of(delegatorUser));
        when(agentDelegationRepository.save(any(AgentDelegation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DelegatorService service = new DelegatorService(
            trainingRequestRepository,
            trainingRequestService,
            representativeRepository,
            representativeService,
            agentDelegationRepository,
            userAccountRepository
        );

        AgentDelegation result = service.markAsAgent(representativeId, jwt, new DelegatorDecisionRequest("Approved"));

        assertNotNull(result);
        assertEquals(representativeId, result.getRepresentativeProfileId());
        assertEquals(delegatorUserId, result.getDelegatorProfileId());
        assertEquals(AgentDelegationStatus.ACTIVE, result.getStatus());
        assertNotNull(result.getDelegatedAt());
    }
}
