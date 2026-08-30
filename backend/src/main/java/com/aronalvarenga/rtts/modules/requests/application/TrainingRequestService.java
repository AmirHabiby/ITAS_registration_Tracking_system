package com.aronalvarenga.rtts.modules.requests.application;

import com.aronalvarenga.rtts.modules.representatives.application.RepresentativeService;
import com.aronalvarenga.rtts.modules.representatives.domain.RepresentativeRepository;
import com.aronalvarenga.rtts.identity.domain.UserAccountRepository;
import com.aronalvarenga.rtts.modules.enrollments.domain.Enrollment;
import com.aronalvarenga.rtts.modules.enrollments.domain.EnrollmentRepository;
import com.aronalvarenga.rtts.modules.requests.domain.TrainingRequestEntity;
import com.aronalvarenga.rtts.modules.requests.domain.TrainingRequestRepository;
import com.aronalvarenga.rtts.modules.requests.domain.TrainingRequestStatus;
import com.aronalvarenga.rtts.modules.requests.web.ReviewRequest;
import com.aronalvarenga.rtts.modules.requests.web.TrainingRequestDto;
import com.aronalvarenga.rtts.modules.trainings.application.TrainingService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TrainingRequestService {

    private final TrainingRequestRepository trainingRequestRepository;
    private final RepresentativeRepository representativeRepository;
    private final TrainingService trainingService;
    private final RepresentativeService representativeService;
    private final EnrollmentRepository enrollmentRepository;
    private final UserAccountRepository userAccountRepository;

    public TrainingRequestService(
        TrainingRequestRepository trainingRequestRepository,
        RepresentativeRepository representativeRepository,
        TrainingService trainingService,
        RepresentativeService representativeService,
        EnrollmentRepository enrollmentRepository,
        UserAccountRepository userAccountRepository
    ) {
        this.trainingRequestRepository = trainingRequestRepository;
        this.representativeRepository = representativeRepository;
        this.trainingService = trainingService;
        this.representativeService = representativeService;
        this.enrollmentRepository = enrollmentRepository;
        this.userAccountRepository = userAccountRepository;
    }

    @Transactional(readOnly = true)
    public List<TrainingRequestEntity> listAll() {
        return trainingRequestRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<TrainingRequestEntity> listForRepresentative(UUID representativeId) {
        return trainingRequestRepository.findByRepresentativeIdOrderByRequestedAtDesc(representativeId);
    }

    @Transactional
    public TrainingRequestEntity requestTraining(UUID representativeId, TrainingRequestDto request) {
        // Verify the representative exists
        if (!representativeRepository.existsById(representativeId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Representative not found");
        }
        // Verify the training exists
        trainingService.get(request.trainingId());
        
        // Mark representative as IN_TRAINING
        representativeService.markTrainingRequested(representativeId);
        
        // Create and save the training request with the verified representative ID
        return trainingRequestRepository.save(new TrainingRequestEntity(representativeId, request.trainingId()));
    }

    @Transactional
    public TrainingRequestEntity requestTraining(TrainingRequestDto request) {
        // Legacy method - kept for backward compatibility
        return requestTraining(request.representativeId(), request);
    }

    @Transactional
    public TrainingRequestEntity approve(UUID requestId, Jwt jwt, ReviewRequest reviewRequest) {
        TrainingRequestEntity trainingRequest = get(requestId);
        UUID delegatorId = userAccountRepository.findByUsername(jwt.getSubject())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Delegator not found"))
            .getId();
        trainingRequest.setStatus(TrainingRequestStatus.APPROVED);
        trainingRequest.setApprovedByDelegatorId(delegatorId);
        trainingRequest.setApprovedAt(Instant.now());
        trainingRequest.setReviewerUsername(jwt.getSubject());
        trainingRequest.setReviewerNote(reviewRequest.note());
        trainingRequest.setReviewedAt(Instant.now());
        TrainingRequestEntity savedRequest = trainingRequestRepository.save(trainingRequest);
        enrollmentRepository.findByTrainingRequestId(savedRequest.getId())
            .orElseGet(() -> {
                Enrollment enrollment = new Enrollment(savedRequest.getRepresentativeId(), savedRequest.getTrainingId());
                enrollment.setTrainingRequestId(savedRequest.getId());
                enrollment.setStatus(com.aronalvarenga.rtts.modules.enrollments.domain.EnrollmentStatus.ENROLLED);
                return enrollmentRepository.save(enrollment);
            });
        representativeService.markTrainingRequested(savedRequest.getRepresentativeId());
        return savedRequest;
    }

    @Transactional
    public TrainingRequestEntity reject(UUID requestId, Jwt jwt, ReviewRequest reviewRequest) {
        TrainingRequestEntity trainingRequest = get(requestId);
        UUID delegatorId = userAccountRepository.findByUsername(jwt.getSubject())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Delegator not found"))
            .getId();
        trainingRequest.setStatus(TrainingRequestStatus.REJECTED);
        trainingRequest.setRejectedByDelegatorId(delegatorId);
        trainingRequest.setRejectedAt(Instant.now());
        trainingRequest.setReviewerUsername(jwt.getSubject());
        trainingRequest.setReviewerNote(reviewRequest.note());
        trainingRequest.setReviewedAt(Instant.now());
        return trainingRequestRepository.save(trainingRequest);
    }

    @Transactional(readOnly = true)
    public TrainingRequestEntity get(UUID requestId) {
        return trainingRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Training request not found"));
    }
}
