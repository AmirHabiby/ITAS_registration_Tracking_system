package com.aronalvarenga.rtts.config;

import com.aronalvarenga.rtts.identity.domain.UserAccount;
import com.aronalvarenga.rtts.identity.domain.UserAccountRepository;
import com.aronalvarenga.rtts.identity.domain.UserRole;
import com.aronalvarenga.rtts.modules.delegator.domain.DelegatorProfile;
import com.aronalvarenga.rtts.modules.delegator.domain.DelegatorProfileRepository;
import com.aronalvarenga.rtts.modules.institutes.domain.TrainingInstitute;
import com.aronalvarenga.rtts.modules.institutes.domain.TrainingInstituteRepository;
import com.aronalvarenga.rtts.modules.representatives.domain.Representative;
import com.aronalvarenga.rtts.modules.representatives.domain.RepresentativeRepository;
import com.aronalvarenga.rtts.modules.representatives.domain.RepresentativeStatus;
import com.aronalvarenga.rtts.modules.requests.domain.TrainingRequestEntity;
import com.aronalvarenga.rtts.modules.requests.domain.TrainingRequestRepository;
import com.aronalvarenga.rtts.modules.trainings.domain.Training;
import com.aronalvarenga.rtts.modules.trainings.domain.TrainingRepository;
import com.aronalvarenga.rtts.modules.trainings.domain.TrainingStatus;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seed(
        UserAccountRepository userAccountRepository,
        RepresentativeRepository representativeRepository,
        TrainingInstituteRepository trainingInstituteRepository,
        DelegatorProfileRepository delegatorProfileRepository,
        TrainingRepository trainingRepository,
        TrainingRequestRepository trainingRequestRepository,
        PasswordEncoder passwordEncoder
    ) {
        return args -> {
            if (userAccountRepository.count() == 0) {
                userAccountRepository.save(new UserAccount("admin", passwordEncoder.encode("admin123"), UserRole.SYSTEM_ADMIN, "System Admin"));
                userAccountRepository.save(new UserAccount("delegator", passwordEncoder.encode("delegator123"), UserRole.DELEGATOR, "Delegator One"));
                userAccountRepository.save(new UserAccount("institute", passwordEncoder.encode("institute123"), UserRole.TRAINING_INSTITUTE, "Institute Admin"));
                userAccountRepository.save(new UserAccount("representative", passwordEncoder.encode("rep123"), UserRole.REPRESENTATIVE, "Representative User"));
            }

            UserAccount repUser = userAccountRepository.findByUsername("representative")
                .orElseThrow(() -> new IllegalStateException("Representative user not found"));
            UserAccount instituteUser = userAccountRepository.findByUsername("institute")
                .orElseThrow(() -> new IllegalStateException("Institute user not found"));
            UserAccount delegatorUser = userAccountRepository.findByUsername("delegator")
                .orElseThrow(() -> new IllegalStateException("Delegator user not found"));

            Representative representative = representativeRepository.findAll().stream().findFirst()
                .orElseGet(() -> {
                    Representative newRep = new Representative("Ana Silva", "ana.silva@example.com");
                    newRep.setUserId(repUser.getId());
                    return representativeRepository.save(newRep);
                });
            representative.setStatus(RepresentativeStatus.IN_TRAINING);
            representativeRepository.save(representative);

            TrainingInstitute institute = trainingInstituteRepository.findAll().stream().findFirst()
                .orElseGet(() -> {
                    TrainingInstitute newInst = new TrainingInstitute("National Training Institute", "training@example.com");
                    newInst.setUserId(instituteUser.getId());
                    return trainingInstituteRepository.save(newInst);
                });

            if (delegatorProfileRepository.count() == 0) {
                delegatorProfileRepository.save(new DelegatorProfile(delegatorUser.getId(), "Delegator One", "delegator@example.com"));
            }

            Training training = trainingRepository.findAll().stream().findFirst().orElseGet(() -> {
                Training newTraining = new Training(institute.getId(), "Field Safety and Service Excellence", "Core onboarding program for new agents.", LocalDate.now().plusDays(7), LocalDate.now().plusDays(14), 20);
                newTraining.setStatus(TrainingStatus.PUBLISHED);
                return trainingRepository.save(newTraining);
            });

            if (trainingRequestRepository.count() == 0) {
                trainingRequestRepository.save(new TrainingRequestEntity(representative.getId(), training.getId()));
            }
        };
    }
}
