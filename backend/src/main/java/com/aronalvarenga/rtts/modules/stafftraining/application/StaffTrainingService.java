package com.aronalvarenga.rtts.modules.stafftraining.application;

import com.aronalvarenga.rtts.modules.trainings.domain.Training;
import com.aronalvarenga.rtts.modules.trainings.domain.TrainingAccessType;
import com.aronalvarenga.rtts.modules.trainings.domain.TrainingRepository;
import com.aronalvarenga.rtts.modules.trainings.domain.TrainingStatus;
import com.aronalvarenga.rtts.modules.stafftraining.web.StaffTrainingAccessRequest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class StaffTrainingService {

    private final TrainingRepository trainingRepository;
    private final PasswordEncoder passwordEncoder;

    public StaffTrainingService(TrainingRepository trainingRepository, PasswordEncoder passwordEncoder) {
        this.trainingRepository = trainingRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<Training> listStaffTrainings() {
        return trainingRepository.findByActiveTrueAndAccessTypeAndStatusOrderByStartDateAsc(
            TrainingAccessType.STAFF,
            TrainingStatus.PUBLISHED);
    }

    @Transactional(readOnly = true)
    public Training access(StaffTrainingAccessRequest request) {
        return listStaffTrainings().stream()
            .filter(training -> training.getStaffAccessPasswordHash() != null)
            .filter(training -> passwordEncoder.matches(
                request.trainingPassword(), training.getStaffAccessPasswordHash()))
            .findFirst()
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Invalid staff training password"));
    }
}
