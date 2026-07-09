package com.aronalvarenga.rtts.modules.trainings.application;

import com.aronalvarenga.rtts.modules.institutes.domain.TrainingInstituteRepository;
import com.aronalvarenga.rtts.modules.trainings.domain.Training;
import com.aronalvarenga.rtts.modules.trainings.domain.TrainingRepository;
import com.aronalvarenga.rtts.modules.trainings.domain.TrainingStatus;
import com.aronalvarenga.rtts.modules.trainings.web.TrainingRequestDto;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TrainingService {

    private final TrainingRepository trainingRepository;
    private final TrainingInstituteRepository trainingInstituteRepository;

    public TrainingService(TrainingRepository trainingRepository, TrainingInstituteRepository trainingInstituteRepository) {
        this.trainingRepository = trainingRepository;
        this.trainingInstituteRepository = trainingInstituteRepository;
    }

    @Transactional(readOnly = true)
    public List<Training> listAvailable() {
        return trainingRepository.findByActiveTrueOrderByStartDateAsc();
    }

    @Transactional(readOnly = true)
    public List<Training> listForInstitute(UUID userId) {
        UUID trainingInstituteProfileId = trainingInstituteRepository.findByUserId(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Training institute not found"))
            .getId();
        return trainingRepository.findByTrainingInstituteProfileIdOrderByStartDateAsc(trainingInstituteProfileId);
    }

    @Transactional(readOnly = true)
    public Training getForInstitute(UUID userId, UUID trainingId) {
        UUID trainingInstituteProfileId = trainingInstituteRepository.findByUserId(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Training institute not found"))
            .getId();
        Training training = get(trainingId);
        if (!trainingInstituteProfileId.equals(training.getTrainingInstituteProfileId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only access your own trainings");
        }
        return training;
    }

    @Transactional
    public Training create(TrainingRequestDto request) {
        if (!trainingInstituteRepository.existsById(request.instituteId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Institute not found");
        }
        Training training = new Training(
            request.instituteId(),
            request.title(),
            request.description(),
            request.startDate(),
            request.endDate(),
            request.capacity());
        training.setStatus(TrainingStatus.PUBLISHED);
        return trainingRepository.save(training);
    }

    @Transactional(readOnly = true)
    public Training get(UUID trainingId) {
        return trainingRepository.findById(trainingId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Training not found"));
    }

    @Transactional
    public Training createForInstitute(UUID userId, TrainingRequestDto request) {
        UUID trainingInstituteProfileId = trainingInstituteRepository.findByUserId(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Training institute not found"))
            .getId();
        if (!trainingInstituteProfileId.equals(request.instituteId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only create trainings for your own institute");
        }
        return create(request);
    }
}
