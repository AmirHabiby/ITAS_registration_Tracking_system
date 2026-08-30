package com.aronalvarenga.rtts.modules.representatives.application;

import com.aronalvarenga.rtts.modules.representatives.domain.Representative;
import com.aronalvarenga.rtts.modules.representatives.domain.RepresentativeRepository;
import com.aronalvarenga.rtts.modules.representatives.domain.RepresentativeStatus;
import com.aronalvarenga.rtts.modules.representatives.web.RepresentativeRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RepresentativeService {

    private final RepresentativeRepository representativeRepository;

    public RepresentativeService(RepresentativeRepository representativeRepository) {
        this.representativeRepository = representativeRepository;
    }

    @Transactional(readOnly = true)
    public List<Representative> list() {
        return representativeRepository.findAll();
    }

    @Transactional
    public Representative create(RepresentativeRequest request) {
        return representativeRepository.save(new Representative(request.fullName(), request.email()));
    }

    @Transactional
    public Representative markTrainingRequested(UUID representativeId) {
        Representative representative = getOrThrow(representativeId);
        representative.setStatus(RepresentativeStatus.IN_TRAINING);
        return representativeRepository.save(representative);
    }

    @Transactional
    public Representative markTrained(UUID representativeId) {
        Representative representative = getOrThrow(representativeId);
        representative.setStatus(RepresentativeStatus.TRAINED);
        return representativeRepository.save(representative);
    }

    @Transactional
    public Representative markAgent(UUID representativeId) {
        Representative representative = getOrThrow(representativeId);
        if (representative.getStatus() != RepresentativeStatus.TRAINED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only trained representatives can be delegated as agent");
        }
        return representative;
    }

    @Transactional(readOnly = true)
    public Representative get(UUID representativeId) {
        return getOrThrow(representativeId);
    }

    private Representative getOrThrow(UUID representativeId) {
        return representativeRepository.findById(representativeId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Representative not found"));
    }
}
