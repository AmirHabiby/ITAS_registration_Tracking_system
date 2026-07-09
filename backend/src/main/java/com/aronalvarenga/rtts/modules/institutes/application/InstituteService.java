package com.aronalvarenga.rtts.modules.institutes.application;

import com.aronalvarenga.rtts.modules.institutes.domain.TrainingInstitute;
import com.aronalvarenga.rtts.modules.institutes.domain.TrainingInstituteRepository;
import com.aronalvarenga.rtts.modules.institutes.web.InstituteRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class InstituteService {

    private final TrainingInstituteRepository trainingInstituteRepository;

    public InstituteService(TrainingInstituteRepository trainingInstituteRepository) {
        this.trainingInstituteRepository = trainingInstituteRepository;
    }

    @Transactional(readOnly = true)
    public List<TrainingInstitute> list() {
        return trainingInstituteRepository.findAll();
    }

    @Transactional
    public TrainingInstitute create(InstituteRequest request) {
        return trainingInstituteRepository.save(new TrainingInstitute(request.name(), request.contactEmail()));
    }

    @Transactional(readOnly = true)
    public TrainingInstitute get(UUID instituteId) {
        return trainingInstituteRepository.findById(instituteId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Institute not found"));
    }
}
