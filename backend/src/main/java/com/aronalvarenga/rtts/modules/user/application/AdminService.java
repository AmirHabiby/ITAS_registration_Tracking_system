package com.aronalvarenga.rtts.modules.user.application;

import com.aronalvarenga.rtts.identity.domain.UserAccount;
import com.aronalvarenga.rtts.identity.domain.UserAccountRepository;
import com.aronalvarenga.rtts.identity.domain.UserRole;
import com.aronalvarenga.rtts.modules.delegator.domain.DelegatorProfile;
import com.aronalvarenga.rtts.modules.delegator.domain.DelegatorProfileRepository;
import com.aronalvarenga.rtts.modules.institutes.domain.TrainingInstitute;
import com.aronalvarenga.rtts.modules.institutes.domain.TrainingInstituteRepository;
import com.aronalvarenga.rtts.modules.representatives.domain.Representative;
import com.aronalvarenga.rtts.modules.representatives.domain.RepresentativeRepository;
import com.aronalvarenga.rtts.modules.user.web.AdminCreateDelegatorRequest;
import com.aronalvarenga.rtts.modules.user.web.AdminCreateRepresentativeRequest;
import com.aronalvarenga.rtts.modules.user.web.AdminCreateTrainingInstituteRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminService {

    private final UserAccountRepository userAccountRepository;
    private final RepresentativeRepository representativeRepository;
    private final DelegatorProfileRepository delegatorProfileRepository;
    private final TrainingInstituteRepository trainingInstituteRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(
        UserAccountRepository userAccountRepository,
        RepresentativeRepository representativeRepository,
        DelegatorProfileRepository delegatorProfileRepository,
        TrainingInstituteRepository trainingInstituteRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.userAccountRepository = userAccountRepository;
        this.representativeRepository = representativeRepository;
        this.delegatorProfileRepository = delegatorProfileRepository;
        this.trainingInstituteRepository = trainingInstituteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserAccount createRepresentative(AdminCreateRepresentativeRequest request) {
        UserAccount userAccount = userAccountRepository.save(new UserAccount(request.username(), passwordEncoder.encode(request.password()), UserRole.REPRESENTATIVE, request.displayName()));
        userAccount.setEnabled(request.enabled());
        userAccount = userAccountRepository.save(userAccount);
        Representative representative = new Representative(request.fullName(), request.email());
        representative.setUserId(userAccount.getId());
        representativeRepository.save(representative);
        return userAccount;
    }

    @Transactional
    public UserAccount createDelegator(AdminCreateDelegatorRequest request) {
        UserAccount userAccount = userAccountRepository.save(new UserAccount(request.username(), passwordEncoder.encode(request.password()), UserRole.DELEGATOR, request.displayName()));
        userAccount.setEnabled(request.enabled());
        userAccount = userAccountRepository.save(userAccount);
        delegatorProfileRepository.save(new DelegatorProfile(userAccount.getId(), request.fullName(), request.email()));
        return userAccount;
    }

    @Transactional
    public UserAccount createTrainingInstitute(AdminCreateTrainingInstituteRequest request) {
        UserAccount userAccount = userAccountRepository.save(new UserAccount(request.username(), passwordEncoder.encode(request.password()), UserRole.TRAINING_INSTITUTE, request.displayName()));
        userAccount.setEnabled(request.enabled());
        userAccount = userAccountRepository.save(userAccount);
        TrainingInstitute institute = new TrainingInstitute(request.name(), request.contactEmail());
        institute.setUserId(userAccount.getId());
        trainingInstituteRepository.save(institute);
        return userAccount;
    }

    @Transactional(readOnly = true)
    public List<UserAccount> listUsers() {
        return userAccountRepository.findAll();
    }

    @Transactional
    public UserAccount activateUser(UUID userId) {
        UserAccount userAccount = getUser(userId);
        userAccount.setEnabled(true);
        return userAccountRepository.save(userAccount);
    }

    @Transactional
    public UserAccount deactivateUser(UUID userId) {
        UserAccount userAccount = getUser(userId);
        userAccount.setEnabled(false);
        return userAccountRepository.save(userAccount);
    }

    private UserAccount getUser(UUID userId) {
        return userAccountRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}